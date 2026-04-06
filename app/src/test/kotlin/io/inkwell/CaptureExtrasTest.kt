package io.inkwell

import io.inkwell.ui.capture.CaptureColor
import io.inkwell.ui.capture.CaptureUiState
import io.inkwell.ui.capture.ToolbarPanel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for I10 Capture-time Slice 3 metadata (pinned, sourceUrl, color).
 */
class CaptureExtrasTest {

    // --- CaptureUiState defaults ---

    @Test
    fun `default state has no extras metadata`() {
        val state = CaptureUiState()
        assertFalse(state.pinned)
        assertEquals("", state.sourceUrl)
        assertNull(state.color)
        assertFalse(state.hasExtrasMetadata)
    }

    // --- hasExtrasMetadata ---

    @Test
    fun `hasExtrasMetadata true when pinned`() {
        val state = CaptureUiState(pinned = true)
        assertTrue(state.hasExtrasMetadata)
    }

    @Test
    fun `hasExtrasMetadata true when sourceUrl set`() {
        val state = CaptureUiState(sourceUrl = "https://example.com")
        assertTrue(state.hasExtrasMetadata)
    }

    @Test
    fun `hasExtrasMetadata true when color set`() {
        val state = CaptureUiState(color = CaptureColor.BLUE)
        assertTrue(state.hasExtrasMetadata)
    }

    @Test
    fun `hasExtrasMetadata false for blank sourceUrl`() {
        val state = CaptureUiState(sourceUrl = "")
        assertFalse(state.hasExtrasMetadata)
    }

    // --- CaptureColor enum ---

    @Test
    fun `CaptureColor has 6 entries`() {
        assertEquals(6, CaptureColor.entries.size)
    }

    @Test
    fun `CaptureColor hex values are valid`() {
        CaptureColor.entries.forEach { color ->
            assertTrue("${color.label} hex should start with #", color.hex.startsWith("#"))
            assertEquals("${color.label} hex should be 7 chars", 7, color.hex.length)
        }
    }

    @Test
    fun `CaptureColor labels are non-blank`() {
        CaptureColor.entries.forEach { color ->
            assertTrue("${color.name} label should be non-blank", color.label.isNotBlank())
        }
    }

    // --- ToolbarPanel.Extras ---

    @Test
    fun `ToolbarPanel includes Extras`() {
        val panels = ToolbarPanel.entries.map { it.name }
        assertTrue("ToolbarPanel should include Extras", "Extras" in panels)
    }

    // --- State mutations (mirroring ViewModel logic) ---

    @Test
    fun `pinned toggle flips state`() {
        val state = CaptureUiState(pinned = false)
        val toggled = state.copy(pinned = !state.pinned)
        assertTrue(toggled.pinned)
        val toggledBack = toggled.copy(pinned = !toggled.pinned)
        assertFalse(toggledBack.pinned)
    }

    @Test
    fun `sourceUrl change updates state`() {
        val state = CaptureUiState()
        val updated = state.copy(sourceUrl = "https://example.com/article")
        assertEquals("https://example.com/article", updated.sourceUrl)
    }

    @Test
    fun `color change updates state`() {
        val state = CaptureUiState()
        val updated = state.copy(color = CaptureColor.RED)
        assertEquals(CaptureColor.RED, updated.color)
        assertEquals("#E53935", updated.color!!.hex)
    }

    @Test
    fun `color clear resets to null`() {
        val state = CaptureUiState(color = CaptureColor.GREEN)
        val cleared = state.copy(color = null)
        assertNull(cleared.color)
        assertFalse(cleared.hasExtrasMetadata)
    }

    // --- Batch mode preserves extras ---

    @Test
    fun `batch reset preserves extras`() {
        val state = CaptureUiState(
            unifiedText = "Some text",
            pinned = true,
            sourceUrl = "https://example.com",
            color = CaptureColor.BLUE,
            batchMode = true,
        )
        // Simulate batch reset (only clear text/list/attachments)
        val reset = state.copy(
            unifiedText = "",
            listName = "",
            listItems = "",
            persistent = false,
            selectedAttachments = emptyList(),
            batchCount = state.batchCount + 1,
            isSubmitting = false,
        )
        assertTrue("pinned should be preserved in batch", reset.pinned)
        assertEquals("sourceUrl should be preserved in batch", "https://example.com", reset.sourceUrl)
        assertEquals("color should be preserved in batch", CaptureColor.BLUE, reset.color)
    }

    // --- Non-batch reset clears extras ---

    @Test
    fun `full reset clears extras`() {
        val fresh = CaptureUiState()
        assertFalse(fresh.pinned)
        assertEquals("", fresh.sourceUrl)
        assertNull(fresh.color)
    }
}
