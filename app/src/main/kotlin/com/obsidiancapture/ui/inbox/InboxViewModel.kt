package com.obsidiancapture.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.repository.InboxRepository
import com.obsidiancapture.data.repository.SyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val inboxRepository: InboxRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    // Tracked so switching tabs/search cancels the previous collector instead of leaking it
    private var tabObserverJob: Job? = null
    private var searchObserverJob: Job? = null

    init {
        tabObserverJob = viewModelScope.launch { observeTab(InboxTab.All) }
        observeTabCounts()
        viewModelScope.launch { inboxRepository.syncInbox() }

        // Reactively watch server configuration (updates when Google Sign-In saves token)
        viewModelScope.launch {
            combine(
                preferencesManager.serverUrl,
                preferencesManager.authToken,
                preferencesManager.lastSyncedAt,
            ) { url, token, lastSynced ->
                Triple(url.isNotBlank() && token.isNotBlank(), lastSynced.ifBlank { null }, Unit)
            }.collect { (configured, lastSynced, _) ->
                _uiState.update {
                    it.copy(isServerConfigured = configured, lastSyncedAt = lastSynced)
                }
            }
        }
    }

    private fun observeTabCounts() {
        viewModelScope.launch {
            inboxRepository.pendingSyncCount.collect { count ->
                _uiState.update { it.copy(pendingSyncCount = count) }
            }
        }
        viewModelScope.launch {
            inboxRepository.getInboxNotes().collect { notes ->
                _uiState.update { it.copy(allCount = notes.size) }
            }
        }
        viewModelScope.launch {
            inboxRepository.getReviewQueue().collect { notes ->
                _uiState.update { it.copy(reviewCount = notes.size) }
            }
        }
    }

    fun onTabChange(tab: InboxTab) {
        _uiState.update { it.copy(selectedTab = tab, searchQuery = "", isSearchActive = false) }
        searchObserverJob?.cancel()
        tabObserverJob?.cancel()
        tabObserverJob = viewModelScope.launch { observeTab(tab) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isNotBlank()) {
            searchObserverJob?.cancel()
            searchObserverJob = viewModelScope.launch {
                inboxRepository.searchNotes(query).collect { notes ->
                    _uiState.update { it.copy(notes = notes) }
                }
            }
        } else {
            searchObserverJob?.cancel()
            tabObserverJob?.cancel()
            tabObserverJob = viewModelScope.launch { observeTab(_uiState.value.selectedTab) }
        }
    }

    fun onSearchToggle() {
        val newActive = !_uiState.value.isSearchActive
        _uiState.update { it.copy(isSearchActive = newActive, searchQuery = "") }
        if (!newActive) {
            searchObserverJob?.cancel()
            tabObserverJob?.cancel()
            tabObserverJob = viewModelScope.launch { observeTab(_uiState.value.selectedTab) }
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val result = inboxRepository.syncInbox()
            val message = when (result) {
                is SyncResult.Success -> "Synced ${result.count} notes"
                is SyncResult.NoServer -> "No server configured"
                is SyncResult.Error -> result.message
            }
            _uiState.update { it.copy(isRefreshing = false, snackbarMessage = message) }
        }
    }

    fun onMarkDone(uid: String) {
        viewModelScope.launch {
            inboxRepository.updateNoteStatus(uid, "done")
            _uiState.update { it.copy(snackbarMessage = "Marked done") }
        }
    }

    fun onMarkDropped(uid: String) {
        viewModelScope.launch {
            inboxRepository.updateNoteStatus(uid, "dropped")
            _uiState.update { it.copy(snackbarMessage = "Dropped") }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private suspend fun observeTab(tab: InboxTab) {
        val flow = when (tab) {
            InboxTab.All -> inboxRepository.getInboxNotes()
            InboxTab.Review -> inboxRepository.getReviewQueue()
            InboxTab.Pending -> inboxRepository.getPendingSyncNotes()
        }
        flow.collect { notes ->
            _uiState.update { it.copy(notes = notes) }
        }
    }
}
