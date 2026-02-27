package com.obsidiancapture

import com.obsidiancapture.data.remote.dto.DeviceRegisterRequest
import com.obsidiancapture.data.remote.dto.DeviceRegisterResponse
import com.obsidiancapture.data.remote.dto.DeviceRemoveResponse
import com.obsidiancapture.notifications.NotificationChannels
import com.obsidiancapture.notifications.RegistrationResult
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceRegistrationTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `device register request defaults to android`() {
        val request = DeviceRegisterRequest(
            deviceId = "android-123",
            fcmToken = "token-abc",
        )
        assertEquals("android", request.platform)
    }

    @Test
    fun `device register request serializes correctly`() {
        val request = DeviceRegisterRequest(
            deviceId = "android-123",
            fcmToken = "token-abc",
        )
        val serialized = json.encodeToString(DeviceRegisterRequest.serializer(), request)
        assertTrue(serialized.contains("\"deviceId\":\"android-123\""))
        assertTrue(serialized.contains("\"fcmToken\":\"token-abc\""))
        assertTrue(serialized.contains("\"platform\":\"android\""))
    }

    @Test
    fun `device register response deserializes`() {
        val raw = """{"registered":true}"""
        val response = json.decodeFromString<DeviceRegisterResponse>(raw)
        assertTrue(response.registered)
    }

    @Test
    fun `device remove response deserializes`() {
        val raw = """{"removed":true}"""
        val response = json.decodeFromString<DeviceRemoveResponse>(raw)
        assertTrue(response.removed)
    }

    @Test
    fun `notification channel IDs are stable`() {
        assertEquals("capture_new", NotificationChannels.CAPTURE_CHANNEL_ID)
        assertEquals("capture_gcal", NotificationChannels.GCAL_CHANNEL_ID)
        assertEquals("sync_status", NotificationChannels.SYNC_STATUS_CHANNEL_ID)
        assertEquals("sync_error", NotificationChannels.SYNC_ERROR_CHANNEL_ID)
    }

    @Test
    fun `registration result enum covers all states`() {
        val results = RegistrationResult.entries
        assertEquals(4, results.size)
        assertTrue(results.contains(RegistrationResult.Success))
        assertTrue(results.contains(RegistrationResult.NoServer))
        assertTrue(results.contains(RegistrationResult.NoToken))
        assertTrue(results.contains(RegistrationResult.Error))
    }
}
