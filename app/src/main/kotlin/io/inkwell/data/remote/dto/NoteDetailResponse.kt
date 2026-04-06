package io.inkwell.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteDetailResponse(
    val uid: String,
    val frontmatter: NoteFrontmatter,
    val body: String,
    val gcalStatus: GcalStatus? = null,
    val captureMetadata: CaptureMetadata? = null,
)

@Serializable
data class NoteFrontmatter(
    val uid: String = "",
    val title: String? = null,
    val kind: String = "one_shot",
    val status: String = "open",
    val created: String = "",
    val updated: String = "",
    val tags: List<String> = emptyList(),
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val calendar: String? = null,
    val priority: String? = null,
    val confidence: String? = null,
    val source: String? = null,
)

@Serializable
data class GcalStatus(
    val eventId: String? = null,
    val lastPushedAt: String? = null,
    val lastError: String? = null,
)

/**
 * Server-provided capture classification metadata.
 * Returned as a sibling of `frontmatter` in the detail response.
 *
 * ## Idea normalization
 * The server canonically represents ideas as `captureType = "task"` with
 * `frontmatter.kind = "brainstorming"`. Inkwell normalizes this to local
 * `captureType = "idea"` during sync (see [InboxSyncEngine]).
 */
@Serializable
data class CaptureMetadata(
    val captureType: String = "task",
    val listName: String? = null,
    val items: List<CaptureMetadataItem>? = null,
    val persistent: Boolean? = null,
    val shared: Boolean? = null,
    val shareToken: String? = null,
)

@Serializable
data class CaptureMetadataItem(
    val text: String,
    val checked: Boolean = false,
)
