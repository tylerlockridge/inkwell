package com.obsidiancapture.ui.capture

enum class CaptureType { TASK, NOTE, LIST }

data class CaptureUiState(
    val unifiedText: String = "",
    val selectedTags: Set<String> = emptySet(),
    val suggestedTags: List<String> = emptyList(),
    val kind: String = "one_shot",
    val calendar: String? = null,
    val priority: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val batchMode: Boolean = false,
    val batchCount: Int = 0,
    val isSubmitting: Boolean = false,
    val snackbarMessage: String? = null,
    val pendingSyncCount: Int = 0,
    val activeToolbarPanel: ToolbarPanel? = null,
    val isMetadataExpanded: Boolean = false,
    val isServerConfigured: Boolean = false,
    val isBannerDismissed: Boolean = false,
    val captureType: CaptureType = CaptureType.TASK,
    val listName: String = "",
    val listItems: String = "",
    val persistent: Boolean = false,
) {
    val isValid: Boolean get() = when (captureType) {
        CaptureType.TASK, CaptureType.NOTE -> unifiedText.isNotBlank()
        CaptureType.LIST -> listName.isNotBlank() && listItems.isNotBlank()
    }

    val parsedTitle: String? get() {
        val firstLine = unifiedText.lineSequence().firstOrNull()?.trim()
        return if (firstLine.isNullOrBlank()) null else firstLine
    }

    val parsedBody: String get() {
        val lines = unifiedText.lines()
        return if (lines.size > 1) lines.drop(1).joinToString("\n").trim() else unifiedText.trim()
    }
}

enum class ToolbarPanel { Tags, Schedule, Type, Calendar, Priority }
