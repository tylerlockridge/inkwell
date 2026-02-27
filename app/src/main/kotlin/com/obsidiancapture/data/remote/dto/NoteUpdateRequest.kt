package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteUpdateRequest(
    val status: String? = null,
    val tags: List<String>? = null,
    val body: String? = null,
    val title: String? = null,
)

@Serializable
data class NoteUpdateResponse(
    val uid: String,
    val updated: String,
)
