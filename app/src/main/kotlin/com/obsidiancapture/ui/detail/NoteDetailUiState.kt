package com.obsidiancapture.ui.detail

import com.obsidiancapture.data.local.entity.NoteEntity

data class NoteDetailUiState(
    val note: NoteEntity? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editTitle: String = "",
    val editBody: String = "",
    val editTags: String = "",
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null,
    val navigateBack: Boolean = false,
)
