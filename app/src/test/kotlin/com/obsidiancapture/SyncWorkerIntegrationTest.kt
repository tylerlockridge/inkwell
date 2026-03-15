package com.obsidiancapture

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.local.dao.NoteDao
import com.obsidiancapture.data.remote.CaptureApiService
import com.obsidiancapture.data.remote.dto.InboxItem
import com.obsidiancapture.data.remote.dto.InboxResponse
import com.obsidiancapture.data.remote.dto.NoteDetailResponse
import com.obsidiancapture.data.remote.dto.NoteFrontmatter
import com.obsidiancapture.sync.InboxSyncEngine
import com.obsidiancapture.sync.SyncWorker
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SyncWorkerIntegrationTest {

    private lateinit var context: Context
    private lateinit var noteDao: NoteDao
    private lateinit var apiService: CaptureApiService
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var syncEngine: InboxSyncEngine
    private lateinit var workerParams: WorkerParameters

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        noteDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)

        every { preferencesManager.serverUrl } returns flowOf("https://example.com")
        every { preferencesManager.lastSyncedAt } returns flowOf("")

        coEvery { apiService.getDeletedInbox(any(), any()) } returns InboxResponse(
            items = emptyList(), totalCount = 0, syncToken = ""
        )

        every { noteDao.getInboxNotes() } returns flowOf(emptyList())
        every { noteDao.getPendingSyncCount() } returns flowOf(0)
        coEvery { noteDao.getAllByUids(any()) } returns emptyList()

        syncEngine = InboxSyncEngine(noteDao, apiService, preferencesManager)

        every { noteDao.getInboxNotesCount() } returns flowOf(0)

        mockkObject(com.obsidiancapture.widget.WidgetStateUpdater)
        coEvery { com.obsidiancapture.widget.WidgetStateUpdater.updateCounts(any(), any(), any()) } returns Unit
    }

    private fun buildWorker(): SyncWorker {
        return SyncWorker(context, workerParams, noteDao, preferencesManager, syncEngine)
    }

    private fun makeInboxItem(uid: String, updatedAt: String = "2026-02-08T12:00:00Z") = InboxItem(
        uid = uid, path = "inbox/$uid.md", status = "open", kind = "one_shot", updated_at = updatedAt
    )

    private fun makeNoteDetail(uid: String) = NoteDetailResponse(
        uid = uid,
        frontmatter = NoteFrontmatter(
            uid = uid, title = "Note $uid", kind = "one_shot", status = "open",
            created = "2026-02-08T10:00:00Z", updated = "2026-02-08T12:00:00Z",
        ),
        body = "Body of $uid",
    )

    @Test
    fun `1 of 3 items fetch fails returns Result success`() = runTest {
        val items = listOf(makeInboxItem("a"), makeInboxItem("b"), makeInboxItem("c"))
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(
            items = items, totalCount = 3, syncToken = ""
        )
        coEvery { apiService.getNote(any(), "a") } returns makeNoteDetail("a")
        coEvery { apiService.getNote(any(), "b") } throws RuntimeException("network error")
        coEvery { apiService.getNote(any(), "c") } returns makeNoteDetail("c")

        val result = buildWorker().doWork()
        assertEquals(Result.success(), result)
    }

    @Test
    fun `all items fail returns Result retry`() = runTest {
        val items = listOf(makeInboxItem("a"), makeInboxItem("b"), makeInboxItem("c"))
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(
            items = items, totalCount = 3, syncToken = ""
        )
        coEvery { apiService.getNote(any(), any()) } throws RuntimeException("network down")

        val result = buildWorker().doWork()
        assertEquals(Result.retry(), result)
    }

    @Test
    fun `all succeed returns Result success and upserts 3`() = runTest {
        val items = listOf(makeInboxItem("a"), makeInboxItem("b"), makeInboxItem("c"))
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(
            items = items, totalCount = 3, syncToken = ""
        )
        coEvery { apiService.getNote(any(), "a") } returns makeNoteDetail("a")
        coEvery { apiService.getNote(any(), "b") } returns makeNoteDetail("b")
        coEvery { apiService.getNote(any(), "c") } returns makeNoteDetail("c")

        val result = buildWorker().doWork()
        assertEquals(Result.success(), result)
        coVerify { noteDao.upsertAll(match { it.size == 3 }) }
    }

    @Test
    fun `empty inbox returns Result success`() = runTest {
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(
            items = emptyList(), totalCount = 0, syncToken = ""
        )

        val result = buildWorker().doWork()
        assertEquals(Result.success(), result)
    }

    // --- US-003: Tombstone sweep tests ---

    @Test
    fun `tombstone sweep passes correct UIDs to deleteByUidsIfSynced`() = runTest {
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(
            items = emptyList(), totalCount = 0, syncToken = ""
        )
        val deletedItems = listOf(
            makeInboxItem("del-1"),
            makeInboxItem("del-2"),
            makeInboxItem("del-3"),
        )
        coEvery { apiService.getDeletedInbox(any(), any()) } returns InboxResponse(
            items = deletedItems, totalCount = 3, syncToken = ""
        )
        val uidSlot = slot<List<String>>()
        coEvery { noteDao.deleteByUidsIfSynced(capture(uidSlot)) } returns Unit

        buildWorker().doWork()

        assertEquals(listOf("del-1", "del-2", "del-3"), uidSlot.captured)
    }

    @Test
    fun `empty deletion list does not call deleteByUidsIfSynced`() = runTest {
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(
            items = emptyList(), totalCount = 0, syncToken = ""
        )
        coEvery { apiService.getDeletedInbox(any(), any()) } returns InboxResponse(
            items = emptyList(), totalCount = 0, syncToken = ""
        )

        buildWorker().doWork()

        coVerify(exactly = 0) { noteDao.deleteByUidsIfSynced(any()) }
    }

    @Test
    fun `deleteByUidsIfSynced exception is caught and logged`() = runTest {
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(
            items = emptyList(), totalCount = 0, syncToken = ""
        )
        coEvery { apiService.getDeletedInbox(any(), any()) } returns InboxResponse(
            items = listOf(makeInboxItem("del-1")), totalCount = 1, syncToken = ""
        )
        coEvery { noteDao.deleteByUidsIfSynced(any()) } throws RuntimeException("DB error")

        val result = buildWorker().doWork()
        // Tombstone sweep failure is non-fatal
        assertEquals(Result.success(), result)
    }

    @Test
    fun `CancellationException inside async propagates without being caught`() = runTest {
        // CancellationException must not be swallowed by the outer catch — it should
        // propagate so WorkManager can stop cleanly instead of rescheduling.
        val items = listOf(makeInboxItem("a"))
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(
            items = items, totalCount = 1, syncToken = ""
        )
        coEvery { apiService.getNote(any(), "a") } throws kotlinx.coroutines.CancellationException("cancelled")

        try {
            buildWorker().doWork()
            fail("Expected CancellationException to propagate")
        } catch (_: kotlinx.coroutines.CancellationException) {
            // Expected — cancellation is properly respected
        }
    }
}
