package com.obsidiancapture

import com.obsidiancapture.ui.inbox.InboxTab
import com.obsidiancapture.ui.inbox.InboxUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InboxUiStateTest {

    @Test
    fun `default state has All tab selected`() {
        val state = InboxUiState()
        assertEquals(InboxTab.All, state.selectedTab)
    }

    @Test
    fun `default state is not refreshing`() {
        val state = InboxUiState()
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `default state has no search`() {
        val state = InboxUiState()
        assertFalse(state.isSearchActive)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `default state has empty notes list`() {
        val state = InboxUiState()
        assertTrue(state.notes.isEmpty())
    }

    @Test
    fun `default state has no snackbar`() {
        val state = InboxUiState()
        assertNull(state.snackbarMessage)
    }

    @Test
    fun `all three tabs exist`() {
        assertEquals(3, InboxTab.entries.size)
        assertEquals("All", InboxTab.All.name)
        assertEquals("Review", InboxTab.Review.name)
        assertEquals("Pending", InboxTab.Pending.name)
    }
}
