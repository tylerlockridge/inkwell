package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Response from GET /api/status — server health + inbox summary + GCal sync + Syncthing.
 * Uses defaults for all fields so missing/unknown keys don't crash deserialization.
 */
@Serializable
data class SystemStatusResponse(
    val status: String = "unknown",
    val lastScanAt: String? = null,
    val itemsOpenCount: Int = 0,
    val reviewQueueCount: Int = 0,
    val quarantinedItemCount: Int = 0,
    val lastGcalErrorAt: String? = null,
    val memoryUsageMB: Int = 0,
    val uptimeSeconds: Long = 0,
    val version: String = "",
    val vaultAccessible: Boolean = false,
    val diskUsagePercent: Int? = null,
    val emailEnabled: Boolean = false,
    val syncthing: SyncthingStatusResponse? = null,
    val summary: StatusSummary? = null,
)

@Serializable
data class StatusSummary(
    val inbox: InboxSummary? = null,
    val review: ReviewSummary? = null,
    val gcal: GcalSummary? = null,
    val email: EmailSummary? = null,
)

@Serializable
data class InboxSummary(
    val total: Int = 0,
    val today: Int = 0,
)

@Serializable
data class ReviewSummary(
    val pending: Int = 0,
)

@Serializable
data class GcalSummary(
    val lastSync: String? = null,
    val errors: Int = 0,
)

@Serializable
data class EmailSummary(
    val enabled: Boolean = false,
)
