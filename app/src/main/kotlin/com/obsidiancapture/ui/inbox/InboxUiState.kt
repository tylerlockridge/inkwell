package com.obsidiancapture.ui.inbox

import com.obsidiancapture.data.local.entity.NoteEntity

enum class InboxTab { All, Review, Pending }

data class InboxUiState(
    val notes: List<NoteEntity> = emptyList(),
    val selectedTab: InboxTab = InboxTab.All,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isRefreshing: Boolean = false,
    val pendingSyncCount: Int = 0,
    val allCount: Int = 0,
    val reviewCount: Int = 0,
    val snackbarMessage: String? = null,
    val isServerConfigured: Boolean = false,
    val lastSyncedAt: String? = null,
)
