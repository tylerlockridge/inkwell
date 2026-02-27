package com.obsidiancapture.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegisterRequest(
    val deviceId: String,
    val fcmToken: String,
    val platform: String = "android",
)

@Serializable
data class DeviceRegisterResponse(
    val registered: Boolean,
)

@Serializable
data class DeviceRemoveResponse(
    val removed: Boolean,
)
