package com.obsidiancapture.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obsidiancapture.data.local.entity.NoteEntity
import com.obsidiancapture.data.repository.InboxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val inboxRepository: InboxRepository,
) : ViewModel() {

    private val uid: String = savedStateHandle.get<String>("uid") ?: ""

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            val note = inboxRepository.getNote(uid)
            if (note != null) {
                _uiState.update {
                    it.copy(
                        note = note,
                        isLoading = false,
                        editTitle = note.title,
                        editBody = note.body,
                        editTags = NoteEntity.tagsFromJson(note.tags).joinToString(", "),
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, snackbarMessage = "Note not found") }
            }
        }
    }

    fun onToggleEdit() {
        val state = _uiState.value
        if (state.isEditing) {
            // Save changes
            saveEdits()
        } else {
            _uiState.update { it.copy(isEditing = true) }
        }
    }

    fun onEditTitleChange(title: String) {
        _uiState.update { it.copy(editTitle = title) }
    }

    fun onEditBodyChange(body: String) {
        _uiState.update { it.copy(editBody = body) }
    }

    fun onEditTagsChange(tags: String) {
        _uiState.update { it.copy(editTags = tags) }
    }

    fun onMarkDone() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            inboxRepository.updateNoteStatus(uid, "done")
            _uiState.update { it.copy(isSaving = false, snackbarMessage = "Marked done", navigateBack = true) }
        }
    }

    fun onMarkDropped() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            inboxRepository.updateNoteStatus(uid, "dropped")
            _uiState.update { it.copy(isSaving = false, snackbarMessage = "Dropped", navigateBack = true) }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun saveEdits() {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val tags = state.editTags
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            inboxRepository.updateNoteContent(
                uid = uid,
                title = state.editTitle,
                body = state.editBody,
                tags = tags,
            )

            // Reload note from DB
            val updated = inboxRepository.getNote(uid)
            _uiState.update {
                it.copy(
                    note = updated,
                    isEditing = false,
                    isSaving = false,
                    snackbarMessage = "Saved",
                )
            }
        }
    }

}
