package io.inkwell.ui.capture

import android.net.Uri
import androidx.compose.ui.graphics.Color

enum class CaptureType { TASK, NOTE, LIST, IDEA }

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
    val selectedAttachments: List<Uri> = emptyList(),
    val showTypeToggleCoachMark: Boolean = false,
    val showIdeaTypeCoachMark: Boolean = false,
    val showAttachmentCoachMark: Boolean = false,
    // Slice 3 capture-time metadata
    val pinned: Boolean = false,
    val sourceUrl: String = "",
    val color: CaptureColor? = null,
    // shared = true when capture originated from Android share intent
    val shared: Boolean = false,
) {
    /**
     * A capture is valid when it contains meaningful user content.
     *
     * - **Task/Note/Idea**: text, a source URL, or attachments — any one suffices.
     * - **List**: a non-blank list name AND at least one non-blank item line.
     *
     * Cosmetic metadata alone (pinned, color, tags, priority) does NOT make a capture valid.
     */
    val isValid: Boolean get() = when (captureType) {
        CaptureType.TASK, CaptureType.NOTE, CaptureType.IDEA ->
            unifiedText.isNotBlank() || sourceUrl.isNotBlank() || selectedAttachments.isNotEmpty()
        CaptureType.LIST ->
            listName.isNotBlank() && listItems.lines().any { it.isNotBlank() }
    }

    val parsedTitle: String? get() {
        val firstLine = unifiedText.lineSequence().firstOrNull()?.trim()
        return if (firstLine.isNullOrBlank()) null else firstLine
    }

    val parsedBody: String get() {
        val lines = unifiedText.lines()
        return if (lines.size > 1) lines.drop(1).joinToString("\n").trim() else unifiedText.trim()
    }

    val hasExtrasMetadata: Boolean get() = pinned || sourceUrl.isNotBlank() || color != null
}

enum class ToolbarPanel { Tags, Schedule, Type, Calendar, Priority, Extras }

/**
 * Curated color palette for capture-time note coloring.
 * Each entry has a display name, a hex string for Room persistence,
 * and a Compose [Color] for rendering.
 */
enum class CaptureColor(val label: String, val hex: String, val composeColor: Color) {
    RED("Red", "#E53935", Color(0xFFE53935)),
    ORANGE("Orange", "#FB8C00", Color(0xFFFB8C00)),
    YELLOW("Yellow", "#FDD835", Color(0xFFFDD835)),
    GREEN("Green", "#43A047", Color(0xFF43A047)),
    BLUE("Blue", "#1E88E5", Color(0xFF1E88E5)),
    PURPLE("Purple", "#8E24AA", Color(0xFF8E24AA)),
}
