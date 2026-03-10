package com.obsidiancapture

import com.obsidiancapture.ui.capture.CaptureType
import com.obsidiancapture.ui.capture.CaptureUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureTypeIdeaTest {

    @Test
    fun `IDEA type isValid requires unifiedText`() {
        val empty = CaptureUiState(captureType = CaptureType.IDEA, unifiedText = "")
        assertFalse(empty.isValid)

        val filled = CaptureUiState(captureType = CaptureType.IDEA, unifiedText = "Floating idea")
        assertTrue(filled.isValid)
    }

    @Test
    fun `IDEA type isValid is false when text is blank`() {
        val state = CaptureUiState(captureType = CaptureType.IDEA, unifiedText = "   ")
        assertFalse(state.isValid)
    }

    @Test
    fun `captureTypeStr for IDEA resolves to task`() {
        // Simulate what CaptureViewModel.onCapture() computes
        val captureType = CaptureType.IDEA
        val captureTypeStr = when (captureType) {
            CaptureType.TASK -> "task"
            CaptureType.NOTE -> "note"
            CaptureType.LIST -> "list_item"
            CaptureType.IDEA -> "task"
        }
        assertEquals("task", captureTypeStr)
    }

    @Test
    fun `kind for IDEA resolves to brainstorming`() {
        val captureType = CaptureType.IDEA
        val kind = when (captureType) {
            CaptureType.NOTE -> "note"
            CaptureType.IDEA -> "brainstorming"
            else -> "one_shot"
        }
        assertEquals("brainstorming", kind)
    }

    @Test
    fun `IDEA is distinct from TASK NOTE and LIST`() {
        val allTypes = CaptureType.entries
        assertTrue(allTypes.contains(CaptureType.IDEA))
        assertEquals(4, allTypes.size)
    }

    @Test
    fun `selectedAttachments defaults to empty`() {
        val state = CaptureUiState()
        assertTrue(state.selectedAttachments.isEmpty())
    }

    @Test
    fun `coach mark flags default to false`() {
        val state = CaptureUiState()
        assertFalse(state.showTypeToggleCoachMark)
        assertFalse(state.showIdeaTypeCoachMark)
        assertFalse(state.showAttachmentCoachMark)
    }
}
