package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InboxResponse(
    val items: List<InboxItem>,
    val totalCount: Int,
    val syncToken: String,
)

@Serializable
data class InboxItem(
    val uid: String,
    val path: String,
    val status: String,
    val kind: String,
    val content_hash: String = "",
    val updated_at: String = "",
)
