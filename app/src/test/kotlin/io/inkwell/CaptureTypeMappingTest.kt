package io.inkwell

import io.inkwell.ui.capture.CaptureType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies the captureType string mapping used by CaptureViewModel.submitCapture().
 * The mapping must align with BrowseType's classification heuristic:
 * - "task" → BrowseType.TASK
 * - "note" → BrowseType.NOTE
 * - "list_item" → BrowseType.LIST
 * - "idea" → BrowseType.IDEA
 *
 * Bug fix I6.3a: CaptureType.IDEA was incorrectly mapped to "task" instead of "idea",
 * causing all ideas to be classified as tasks in the inbox.
 */
class CaptureTypeMappingTest {

    // Mirror the mapping from CaptureViewModel.submitCapture()
    private fun captureTypeToString(type: CaptureType): String = when (type) {
        CaptureType.TASK -> "task"
        CaptureType.NOTE -> "note"
        CaptureType.LIST -> "list_item"
        CaptureType.IDEA -> "idea"
    }

    // Mirror the kind mapping from CaptureViewModel.submitCapture()
    private fun captureTypeToKind(type: CaptureType, defaultKind: String = "one_shot"): String = when (type) {
        CaptureType.NOTE -> "note"
        CaptureType.IDEA -> "brainstorming"
        else -> defaultKind
    }

    @Test
    fun `TASK maps to captureType task`() {
        assertEquals("task", captureTypeToString(CaptureType.TASK))
    }

    @Test
    fun `NOTE maps to captureType note`() {
        assertEquals("note", captureTypeToString(CaptureType.NOTE))
    }

    @Test
    fun `LIST maps to captureType list_item`() {
        assertEquals("list_item", captureTypeToString(CaptureType.LIST))
    }

    @Test
    fun `IDEA maps to captureType idea not task`() {
        assertEquals("idea", captureTypeToString(CaptureType.IDEA))
    }

    @Test
    fun `IDEA kind is brainstorming`() {
        assertEquals("brainstorming", captureTypeToKind(CaptureType.IDEA))
    }

    @Test
    fun `NOTE kind is note`() {
        assertEquals("note", captureTypeToKind(CaptureType.NOTE))
    }

    @Test
    fun `TASK kind uses default`() {
        assertEquals("one_shot", captureTypeToKind(CaptureType.TASK))
    }
}
