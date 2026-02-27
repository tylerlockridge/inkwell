package com.obsidiancapture.notifications

import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.data.remote.CaptureApiService
import com.obsidiancapture.data.remote.dto.DeviceRegisterRequest
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages device registration with the server for push notifications.
 *
 * On first launch, generates a stable device ID and persists it.
 * When an FCM token is available, registers the device with the server.
 */
@Singleton
class DeviceRegistrationManager @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val apiService: CaptureApiService,
) {
    suspend fun ensureDeviceId(): String {
        var deviceId = preferencesManager.deviceId.first()
        if (deviceId.isBlank()) {
            deviceId = "android-${UUID.randomUUID()}"
            preferencesManager.setDeviceId(deviceId)
        }
        return deviceId
    }

    suspend fun registerWithServer(): RegistrationResult {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return RegistrationResult.NoServer

        val fcmToken = preferencesManager.fcmToken.first()
        if (fcmToken.isBlank()) return RegistrationResult.NoToken

        val deviceId = ensureDeviceId()

        return try {
            apiService.registerDevice(
                serverUrl,
                DeviceRegisterRequest(deviceId = deviceId, fcmToken = fcmToken),
            )
            RegistrationResult.Success
        } catch (_: Exception) {
            RegistrationResult.Error
        }
    }

    suspend fun unregisterFromServer(): Boolean {
        val serverUrl = preferencesManager.serverUrl.first()
        if (serverUrl.isBlank()) return false

        val deviceId = preferencesManager.deviceId.first()
        if (deviceId.isBlank()) return false

        return try {
            apiService.removeDevice(serverUrl, deviceId)
            true
        } catch (_: Exception) {
            false
        }
    }
}

enum class RegistrationResult {
    Success,
    NoServer,
    NoToken,
    Error,
}
