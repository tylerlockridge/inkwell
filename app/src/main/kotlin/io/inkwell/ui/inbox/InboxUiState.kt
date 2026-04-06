package io.inkwell.ui.inbox

import io.inkwell.data.local.entity.BrowseType
import io.inkwell.data.local.entity.NoteEntity

enum class InboxTab {
    All,
    Tasks,
    Notes,
    Lists,
    Ideas,
    Pending,
    ;

    /** The [BrowseType] this tab filters on, or null for All/Pending which use different logic. */
    val browseType: BrowseType?
        get() = when (this) {
            Tasks -> BrowseType.TASK
            Notes -> BrowseType.NOTE
            Lists -> BrowseType.LIST
            Ideas -> BrowseType.IDEA
            All, Pending -> null
        }
}

data class InboxUiState(
    val notes: List<NoteEntity> = emptyList(),
    val selectedTab: InboxTab = InboxTab.All,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isRefreshing: Boolean = false,
    val allCount: Int = 0,
    val taskCount: Int = 0,
    val noteCount: Int = 0,
    val listCount: Int = 0,
    val ideaCount: Int = 0,
    val pendingSyncCount: Int = 0,
    val snackbarMessage: String? = null,
    val isServerConfigured: Boolean = false,
    val lastSyncedAt: String? = null,
) {
    fun countForTab(tab: InboxTab): Int = when (tab) {
        InboxTab.All -> allCount
        InboxTab.Tasks -> taskCount
        InboxTab.Notes -> noteCount
        InboxTab.Lists -> listCount
        InboxTab.Ideas -> ideaCount
        InboxTab.Pending -> pendingSyncCount
    }
}
