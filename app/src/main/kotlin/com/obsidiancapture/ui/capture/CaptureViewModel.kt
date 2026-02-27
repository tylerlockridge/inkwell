package com.obsidiancapture.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.repository.CaptureRepository
import com.obsidiancapture.data.repository.CaptureResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            captureRepository.pendingSyncCount.collect { count ->
                _uiState.update { it.copy(pendingSyncCount = count) }
            }
        }

        // Reactively watch server configuration (updates when Google Sign-In saves token)
        viewModelScope.launch {
            combine(preferencesManager.serverUrl, preferencesManager.authToken) { url, token ->
                url.isNotBlank() && token.isNotBlank()
            }.collect { configured ->
                _uiState.update { it.copy(isServerConfigured = configured) }
            }
        }

        // Load saved preferences
        viewModelScope.launch {
            val savedCalendar = preferencesManager.lastCalendar.first()
            val savedKind = preferencesManager.lastKind.first()
            val savedPriority = preferencesManager.lastPriority.first()
            _uiState.update { state ->
                state.copy(
                    calendar = savedCalendar,
                    kind = savedKind,
                    priority = savedPriority,
                )
            }

            // Fetch smart defaults in background (after prefs loaded)
            val defaults = captureRepository.getCaptureDefaults()
            if (defaults != null) {
                _uiState.update { state ->
                    state.copy(
                        suggestedTags = defaults.suggestedTags,
                        calendar = state.calendar,
                    )
                }
            }
        }
    }

    fun onUnifiedTextChange(text: String) {
        _uiState.update { it.copy(unifiedText = text) }
    }

    fun onTagToggle(tag: String) {
        _uiState.update { state ->
            val tags = state.selectedTags.toMutableSet()
            if (tags.contains(tag)) tags.remove(tag) else tags.add(tag)
            state.copy(selectedTags = tags)
        }
    }

    fun onKindChange(kind: String) {
        _uiState.update { it.copy(kind = kind) }
    }

    fun onCalendarChange(calendar: String?) {
        _uiState.update { it.copy(calendar = calendar) }
    }

    fun onPriorityChange(priority: String?) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onDateChange(date: String?) {
        _uiState.update { it.copy(date = date) }
    }

    fun onQuickDate(option: String) {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val dateStr = when (option) {
            "today" -> LocalDate.now().format(formatter)
            "tomorrow" -> LocalDate.now().plusDays(1).format(formatter)
            else -> null
        }
        _uiState.update { it.copy(date = dateStr) }
    }

    fun onStartTimeChange(startTime: String?) {
        _uiState.update { it.copy(startTime = startTime) }
    }

    fun onEndTimeChange(endTime: String?) {
        _uiState.update { it.copy(endTime = endTime) }
    }

    fun onBatchModeToggle() {
        _uiState.update { it.copy(batchMode = !it.batchMode) }
    }

    fun onMetadataExpandToggle() {
        _uiState.update { state ->
            state.copy(
                isMetadataExpanded = !state.isMetadataExpanded,
                activeToolbarPanel = if (state.isMetadataExpanded) null else state.activeToolbarPanel,
            )
        }
    }

    fun onToolbarPanelToggle(panel: ToolbarPanel) {
        _uiState.update { state ->
            state.copy(
                activeToolbarPanel = if (state.activeToolbarPanel == panel) null else panel,
                isMetadataExpanded = true,
            )
        }
    }

    fun onDismissBanner() {
        _uiState.update { it.copy(isBannerDismissed = true) }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onCapture() {
        val state = _uiState.value
        if (!state.isValid || state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            val result = captureRepository.capture(
                body = state.parsedBody,
                title = state.parsedTitle,
                tags = state.selectedTags.toList().ifEmpty { null },
                kind = state.kind,
                date = state.date,
                startTime = state.startTime,
                endTime = state.endTime,
                calendar = state.calendar,
                priority = state.priority,
            )

            when (result) {
                is CaptureResult.Online -> {
                    val message = "Captured and synced \u2713"
                    if (state.batchMode) {
                        _uiState.update {
                            it.copy(
                                unifiedText = "",
                                batchCount = it.batchCount + 1,
                                isSubmitting = false,
                                snackbarMessage = message,
                            )
                        }
                    } else {
                        _uiState.update {
                            CaptureUiState(
                                snackbarMessage = message,
                                pendingSyncCount = it.pendingSyncCount,
                                isServerConfigured = it.isServerConfigured,
                            )
                        }
                    }
                    persistCapturePreferences(state)
                }
                is CaptureResult.Offline -> {
                    val message = "Saved locally \u2014 will sync when connected"
                    if (state.batchMode) {
                        _uiState.update {
                            it.copy(
                                unifiedText = "",
                                batchCount = it.batchCount + 1,
                                isSubmitting = false,
                                snackbarMessage = message,
                            )
                        }
                    } else {
                        _uiState.update {
                            CaptureUiState(
                                snackbarMessage = message,
                                pendingSyncCount = it.pendingSyncCount,
                                isServerConfigured = it.isServerConfigured,
                            )
                        }
                    }
                    persistCapturePreferences(state)
                }
                is CaptureResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            snackbarMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun persistCapturePreferences(state: CaptureUiState) {
        viewModelScope.launch {
            preferencesManager.setLastCalendar(state.calendar)
            preferencesManager.setLastKind(state.kind)
            preferencesManager.setLastPriority(state.priority)
        }
    }
}
