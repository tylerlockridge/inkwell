package io.inkwell

import io.inkwell.data.local.entity.ChecklistItems
import io.inkwell.data.local.entity.NoteEntity
import io.inkwell.data.remote.dto.CaptureMetadata
import io.inkwell.data.remote.dto.CaptureMetadataItem
import io.inkwell.data.remote.dto.NoteFrontmatter
import io.inkwell.data.remote.dto.NoteDetailResponse
import io.inkwell.sync.InboxSyncEngine
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Tests for [InboxSyncEngine.resolveCaptureMetadata] — the I7 logic that
 * consumes server `captureMetadata` and normalizes ideas locally.
 */
class SyncCaptureMetadataTest {

    private val engine = InboxSyncEngine(mockk(), mockk(), mockk())
    private val now = Instant.now().toString()

    private fun detail(
        kind: String = "one_shot",
        captureMetadata: CaptureMetadata? = null,
    ) = NoteDetailResponse(
        uid = "test-uid",
        frontmatter = NoteFrontmatter(
            uid = "test-uid",
            kind = kind,
            created = now,
            updated = now,
        ),
        body = "test body",
        captureMetadata = captureMetadata,
    )

    private fun existing(
        captureType: String? = null,
        listName: String? = null,
        listItemsJson: String? = null,
        persistent: Boolean = false,
    ) = NoteEntity(
        uid = "test-uid",
        title = "Test",
        body = "body",
        captureType = captureType,
        listName = listName,
        listItemsJson = listItemsJson,
        persistent = persistent,
        created = now,
        updated = now,
    )

    // --- Server provides captureMetadata ---

    @Test
    fun `task metadata consumed directly`() {
        val result = engine.resolveCaptureMetadata(
            detail(captureMetadata = CaptureMetadata(captureType = "task")),
            existing = null,
        )
        assertEquals("task", result.captureType)
    }

    @Test
    fun `note metadata consumed directly`() {
        val result = engine.resolveCaptureMetadata(
            detail(kind = "note", captureMetadata = CaptureMetadata(captureType = "note")),
            existing = null,
        )
        assertEquals("note", result.captureType)
    }

    @Test
    fun `list metadata consumed with items`() {
        val result = engine.resolveCaptureMetadata(
            detail(captureMetadata = CaptureMetadata(
                captureType = "list_item",
                listName = "Groceries",
                items = listOf(
                    CaptureMetadataItem("Milk", false),
                    CaptureMetadataItem("Eggs", true),
                ),
                persistent = true,
            )),
            existing = null,
        )
        assertEquals("list_item", result.captureType)
        assertEquals("Groceries", result.listName)
        assertTrue(result.persistent)

        // Verify items were serialized to structured format
        val items = ChecklistItems.parse(result.listItemsJson)
        assertEquals(2, items.size)
        assertEquals("Milk", items[0].text)
        assertEquals(false, items[0].checked)
        assertEquals("Eggs", items[1].text)
        assertEquals(true, items[1].checked)
    }

    @Test
    fun `brainstorming idea normalized from task to idea`() {
        val result = engine.resolveCaptureMetadata(
            detail(
                kind = "brainstorming",
                captureMetadata = CaptureMetadata(captureType = "task"),
            ),
            existing = null,
        )
        assertEquals("idea", result.captureType)
    }

    @Test
    fun `non-brainstorming task stays as task`() {
        val result = engine.resolveCaptureMetadata(
            detail(
                kind = "one_shot",
                captureMetadata = CaptureMetadata(captureType = "task"),
            ),
            existing = null,
        )
        assertEquals("task", result.captureType)
    }

    @Test
    fun `complex task stays as task`() {
        val result = engine.resolveCaptureMetadata(
            detail(
                kind = "complex",
                captureMetadata = CaptureMetadata(captureType = "task"),
            ),
            existing = null,
        )
        assertEquals("task", result.captureType)
    }

    // --- Local checked state preserved during sync ---

    @Test
    fun `local checked state preserved when server items match`() {
        val localJson = ChecklistItems.serialize(listOf(
            io.inkwell.data.local.entity.ChecklistItem("Milk", true),
            io.inkwell.data.local.entity.ChecklistItem("Eggs", false),
        ))
        val result = engine.resolveCaptureMetadata(
            detail(captureMetadata = CaptureMetadata(
                captureType = "list_item",
                items = listOf(
                    CaptureMetadataItem("Milk", false),
                    CaptureMetadataItem("Eggs", false),
                ),
            )),
            existing = existing(listItemsJson = localJson),
        )
        val items = ChecklistItems.parse(result.listItemsJson)
        assertTrue("Local checked state for Milk should be preserved", items[0].checked)
    }

    // --- No captureMetadata from server (fallback) ---

    @Test
    fun `fallback preserves existing local metadata`() {
        val result = engine.resolveCaptureMetadata(
            detail(),
            existing = existing(
                captureType = "idea",
                listName = null,
                listItemsJson = null,
                persistent = false,
            ),
        )
        assertEquals("idea", result.captureType)
    }

    @Test
    fun `fallback with no existing returns nulls`() {
        val result = engine.resolveCaptureMetadata(detail(), existing = null)
        assertNull(result.captureType)
        assertNull(result.listName)
        assertNull(result.listItemsJson)
        assertEquals(false, result.persistent)
    }
}
