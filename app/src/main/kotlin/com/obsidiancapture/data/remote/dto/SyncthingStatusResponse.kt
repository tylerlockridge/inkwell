package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncthingStatusResponse(
    val system: SyncthingSystem? = null,
    val devices: List<SyncthingDevice> = emptyList(),
    val folders: List<SyncthingFolder> = emptyList(),
    val reachable: Boolean = false,
    val lastCheckedAt: String = "",
)

@Serializable
data class SyncthingSystem(
    val myID: String = "",
    val uptime: Long = 0,
    val version: String = "",
)

@Serializable
data class SyncthingDevice(
    val deviceID: String = "",
    val name: String = "",
    val connected: Boolean = false,
    val paused: Boolean = false,
    val lastSeen: String = "",
)

@Serializable
data class SyncthingFolder(
    val id: String = "",
    val label: String = "",
    val state: String = "",
    val completion: Int = 0,
    val errors: Int = 0,
)

@Serializable
data class SyncthingRestartResponse(
    val restarted: Boolean = false,
)
