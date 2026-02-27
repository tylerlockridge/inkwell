package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CaptureResponse(
    val path: String,
    val uid: String,
)
