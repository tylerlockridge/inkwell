package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CaptureRequest(
    val body: String,
    val title: String? = null,
    val tags: List<String>? = null,
    val kind: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val calendar: String? = null,
    val priority: String? = null,
    val source: String = "android",
    val uuid: String? = null,
)
