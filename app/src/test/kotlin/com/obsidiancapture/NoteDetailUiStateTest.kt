package com.obsidiancapture

import com.obsidiancapture.ui.detail.NoteDetailUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteDetailUiStateTest {

    @Test
    fun `default state is loading`() {
        val state = NoteDetailUiState()
        assertTrue(state.isLoading)
    }

    @Test
    fun `default state has no note`() {
        val state = NoteDetailUiState()
        assertNull(state.note)
    }

    @Test
    fun `default state is not editing`() {
        val state = NoteDetailUiState()
        assertFalse(state.isEditing)
    }

    @Test
    fun `default state should not navigate back`() {
        val state = NoteDetailUiState()
        assertFalse(state.navigateBack)
    }

    @Test
    fun `edit fields start empty`() {
        val state = NoteDetailUiState()
        assertEquals("", state.editTitle)
        assertEquals("", state.editBody)
        assertEquals("", state.editTags)
    }
}
