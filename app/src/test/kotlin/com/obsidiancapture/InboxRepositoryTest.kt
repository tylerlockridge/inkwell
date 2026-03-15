package com.obsidiancapture

import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.local.dao.NoteDao
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.remote.CaptureApiService
import com.obsidiancapture.data.remote.dto.InboxItem
import com.obsidiancapture.data.remote.dto.InboxResponse
import com.obsidiancapture.data.remote.dto.NoteDetailResponse
import com.obsidiancapture.data.remote.dto.NoteFrontmatter
import com.obsidiancapture.data.repository.InboxRepository
import com.obsidiancapture.data.repository.SyncResult
import com.obsidiancapture.sync.SyncScheduler
import com.obsidiancapture.data.remote.dto.NoteUpdateResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InboxRepositoryTest {

    private lateinit var apiService: CaptureApiService
    private lateinit var noteDao: NoteDao
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var syncScheduler: SyncScheduler
    private lateinit var repository: InboxRepository

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        noteDao = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)
        syncScheduler = mockk(relaxed = true)
        repository = InboxRepository(apiService, noteDao, preferencesManager, syncScheduler)

        every { preferencesManager.serverUrl } returns flowOf("https://example.com")
    }

    private fun makeInboxItem(uid: String, updatedAt: String = "2026-02-08T12:00:00Z") = InboxItem(
        uid = uid, path = "inbox/$uid.md", status = "open", kind = "one_shot", updated_at = updatedAt
    )

    private fun makeNoteDetail(uid: String, updatedAt: String = "2026-02-08T12:00:00Z") = NoteDetailResponse(
        uid = uid,
        frontmatter = NoteFrontmatter(
            uid = uid, title = "Note $uid", kind = "one_shot", status = "open",
            created = "2026-02-08T10:00:00Z", updated = updatedAt,
        ),
        body = "Body of $uid",
    )

    private fun makeLocalNote(
        uid: String,
        updated: String = "2026-02-08T10:00:00Z",
        pendingSync: Boolean = false,
    ) = NoteEntity(
        uid = uid, title = "Local $uid", body = "local body",
        created = "2026-02-08T10:00:00Z", updated = updated,
        pendingSync = pendingSync,
    )

    @Test
    fun `blank url returns NoServer`() = runTest {
        every { preferencesManager.serverUrl } returns flowOf("")
        val result = repository.syncInbox()
        assertTrue(result is SyncResult.NoServer)
    }

    @Test
    fun `3 newer items synced returns count 3`() = runTest {
        val items = listOf(
            makeInboxItem("a", "2026-02-08T12:00:00Z"),
            makeInboxItem("b", "2026-02-08T12:00:00Z"),
            makeInboxItem("c", "2026-02-08T12:00:00Z"),
        )
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(items, 3, "")
        coEvery { noteDao.getAllByUids(any()) } returns emptyList() // no local notes
        coEvery { apiService.getNote(any(), "a") } returns makeNoteDetail("a")
        coEvery { apiService.getNote(any(), "b") } returns makeNoteDetail("b")
        coEvery { apiService.getNote(any(), "c") } returns makeNoteDetail("c")

        val result = repository.syncInbox()
        assertEquals(SyncResult.Success(3), result)
        coVerify(exactly = 3) { noteDao.upsert(any()) }
    }

    @Test
    fun `1 local newer is skipped`() = runTest {
        val items = listOf(
            makeInboxItem("a", "2026-02-08T12:00:00Z"),
            makeInboxItem("b", "2026-02-08T12:00:00Z"),
        )
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(items, 2, "")
        // "a" has local newer timestamp — bulk lookup returns it
        coEvery { noteDao.getAllByUids(any()) } returns listOf(
            makeLocalNote("a", updated = "2026-02-08T14:00:00Z"),
        )
        coEvery { apiService.getNote(any(), "b") } returns makeNoteDetail("b")

        val result = repository.syncInbox()
        assertEquals(SyncResult.Success(1), result)
        coVerify(exactly = 0) { apiService.getNote(any(), "a") }
    }

    @Test
    fun `1 pending is skipped`() = runTest {
        val items = listOf(
            makeInboxItem("a", "2026-02-08T12:00:00Z"),
            makeInboxItem("b", "2026-02-08T12:00:00Z"),
        )
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(items, 2, "")
        // "a" has pendingSync — bulk lookup returns it
        coEvery { noteDao.getAllByUids(any()) } returns listOf(
            makeLocalNote("a", pendingSync = true),
        )
        coEvery { apiService.getNote(any(), "b") } returns makeNoteDetail("b")

        val result = repository.syncInbox()
        assertEquals(SyncResult.Success(1), result)
        coVerify(exactly = 0) { apiService.getNote(any(), "a") }
    }

    @Test
    fun `1 fetch fail others ok`() = runTest {
        val items = listOf(
            makeInboxItem("a", "2026-02-08T12:00:00Z"),
            makeInboxItem("b", "2026-02-08T12:00:00Z"),
        )
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(items, 2, "")
        coEvery { noteDao.getAllByUids(any()) } returns emptyList()
        coEvery { apiService.getNote(any(), "a") } throws RuntimeException("network error")
        coEvery { apiService.getNote(any(), "b") } returns makeNoteDetail("b")

        val result = repository.syncInbox()
        assertEquals(SyncResult.Success(1), result)
    }

    @Test
    fun `network error returns Error`() = runTest {
        coEvery { apiService.getInbox(any(), limit = any()) } throws RuntimeException("Connection refused")

        val result = repository.syncInbox()
        assertTrue(result is SyncResult.Error)
    }

    @Test
    fun `all synced empty inbox returns Success 0`() = runTest {
        coEvery { apiService.getInbox(any(), limit = any()) } returns InboxResponse(emptyList(), 0, "")

        val result = repository.syncInbox()
        assertEquals(SyncResult.Success(0), result)
    }

    // --- US-009: updateNoteStatus DB-first pattern ---

    @Test
    fun `updateNoteStatus - valid config calls DB then API then markSynced`() = runTest {
        coEvery { apiService.updateNote(any(), any(), any()) } returns NoteUpdateResponse("uid-1", "2026-02-08T12:00:00Z")

        val result = repository.updateNoteStatus("uid-1", "done")
        assertTrue(result)
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            noteDao.updateStatus("uid-1", "done", any())
            apiService.updateNote("https://example.com", "uid-1", any())
            noteDao.markSynced("uid-1", any())
        }
    }

    @Test
    fun `updateNoteStatus - blank url calls DB then triggers upload`() = runTest {
        every { preferencesManager.serverUrl } returns flowOf("")

        val result = repository.updateNoteStatus("uid-1", "done")
        assertTrue(result)
        coVerify { noteDao.updateStatus("uid-1", "done", any()) }
        coVerify(exactly = 0) { apiService.updateNote(any(), any(), any()) }
        coVerify { syncScheduler.triggerImmediateUpload() }
    }

    @Test
    fun `updateNoteStatus - API failure returns true and triggers upload`() = runTest {
        coEvery { apiService.updateNote(any(), any(), any()) } throws RuntimeException("Server error")

        val result = repository.updateNoteStatus("uid-1", "done")
        assertTrue(result)
        coVerify { noteDao.updateStatus("uid-1", "done", any()) }
        coVerify { syncScheduler.triggerImmediateUpload() }
        coVerify(exactly = 0) { noteDao.markSynced(any(), any()) }
    }

    @Test
    fun `updateNoteStatus - DB write happens before API call`() = runTest {
        coEvery { apiService.updateNote(any(), any(), any()) } returns NoteUpdateResponse("uid-1", "2026-02-08T12:00:00Z")

        repository.updateNoteStatus("uid-1", "done")
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            noteDao.updateStatus(any(), any(), any())
            apiService.updateNote(any(), any(), any())
        }
    }

    @Test
    fun `updateNoteContent - same DB-first pattern`() = runTest {
        coEvery { apiService.updateNote(any(), any(), any()) } returns NoteUpdateResponse("uid-1", "2026-02-08T12:00:00Z")

        val result = repository.updateNoteContent("uid-1", "Title", "Body", listOf("tag1"))
        assertTrue(result)
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            noteDao.updateContent("uid-1", "Title", "Body", any(), any())
            apiService.updateNote("https://example.com", "uid-1", any())
            noteDao.markSynced("uid-1", any())
        }
    }
}
