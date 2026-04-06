package io.inkwell

import io.inkwell.data.local.entity.BrowseType
import io.inkwell.ui.inbox.InboxTab
import io.inkwell.ui.inbox.InboxUiState
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
    fun `all six tabs exist in correct order`() {
        assertEquals(6, InboxTab.entries.size)
        assertEquals("All", InboxTab.All.name)
        assertEquals("Tasks", InboxTab.Tasks.name)
        assertEquals("Notes", InboxTab.Notes.name)
        assertEquals("Lists", InboxTab.Lists.name)
        assertEquals("Ideas", InboxTab.Ideas.name)
        assertEquals("Pending", InboxTab.Pending.name)
    }

    @Test
    fun `tab browseType mapping`() {
        assertNull(InboxTab.All.browseType)
        assertEquals(BrowseType.TASK, InboxTab.Tasks.browseType)
        assertEquals(BrowseType.NOTE, InboxTab.Notes.browseType)
        assertEquals(BrowseType.LIST, InboxTab.Lists.browseType)
        assertEquals(BrowseType.IDEA, InboxTab.Ideas.browseType)
        assertNull(InboxTab.Pending.browseType)
    }

    @Test
    fun `countForTab returns correct counts`() {
        val state = InboxUiState(
            allCount = 10,
            taskCount = 4,
            noteCount = 3,
            listCount = 2,
            ideaCount = 1,
            pendingSyncCount = 5,
        )
        assertEquals(10, state.countForTab(InboxTab.All))
        assertEquals(4, state.countForTab(InboxTab.Tasks))
        assertEquals(3, state.countForTab(InboxTab.Notes))
        assertEquals(2, state.countForTab(InboxTab.Lists))
        assertEquals(1, state.countForTab(InboxTab.Ideas))
        assertEquals(5, state.countForTab(InboxTab.Pending))
    }
}
