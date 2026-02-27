package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteDetailResponse(
    val uid: String,
    val frontmatter: NoteFrontmatter,
    val body: String,
    val gcalStatus: GcalStatus? = null,
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
