package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
)
