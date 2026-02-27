package com.obsidiancapture

import com.obsidiancapture.ui.capture.CaptureUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureUiStateTest {

    @Test
    fun `isValid returns false when unifiedText is empty`() {
        val state = CaptureUiState(unifiedText = "")
        assertFalse(state.isValid)
    }

    @Test
    fun `isValid returns false when unifiedText is blank`() {
        val state = CaptureUiState(unifiedText = "   ")
        assertFalse(state.isValid)
    }

    @Test
    fun `isValid returns true when unifiedText has content`() {
        val state = CaptureUiState(unifiedText = "Buy groceries")
        assertTrue(state.isValid)
    }

    @Test
    fun `parsedTitle returns first line`() {
        val state = CaptureUiState(unifiedText = "My Title\nSome body text")
        assertEquals("My Title", state.parsedTitle)
    }

    @Test
    fun `parsedTitle returns null when text is blank`() {
        val state = CaptureUiState(unifiedText = "")
        assertNull(state.parsedTitle)
    }

    @Test
    fun `parsedTitle returns first line when no newline`() {
        val state = CaptureUiState(unifiedText = "Just a title")
        assertEquals("Just a title", state.parsedTitle)
    }

    @Test
    fun `parsedBody returns text after first line`() {
        val state = CaptureUiState(unifiedText = "My Title\nBody line 1\nBody line 2")
        assertEquals("Body line 1\nBody line 2", state.parsedBody)
    }

    @Test
    fun `parsedBody returns full text when no newline`() {
        val state = CaptureUiState(unifiedText = "Single line")
        assertEquals("Single line", state.parsedBody)
    }

    @Test
    fun `default state has expected values`() {
        val state = CaptureUiState()
        assertFalse(state.batchMode)
        assertFalse(state.isSubmitting)
        assertTrue(state.selectedTags.isEmpty())
        assertEquals("one_shot", state.kind)
        assertEquals(0, state.batchCount)
        assertEquals(0, state.pendingSyncCount)
        assertNull(state.calendar)
        assertNull(state.priority)
        assertNull(state.date)
        assertNull(state.snackbarMessage)
        assertNull(state.activeToolbarPanel)
    }
}
