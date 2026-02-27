package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CaptureDefaultsResponse(
    val suggestedTags: List<String> = emptyList(),
    val defaultCalendar: String? = null,
    val defaultKind: String = "one_shot",
)
