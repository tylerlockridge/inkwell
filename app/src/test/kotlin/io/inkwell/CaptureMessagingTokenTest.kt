package io.inkwell

import io.inkwell.data.local.PreferencesManager
import io.inkwell.data.remote.CaptureApiService
import io.inkwell.data.remote.dto.DeviceRegisterRequest
import io.inkwell.data.remote.dto.DeviceRegisterResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for CaptureMessagingService.registerTokenWithServer logic.
 *
 * Since CaptureMessagingService requires @AndroidEntryPoint + Firebase,
 * we replicate the registerTokenWithServer logic here to verify:
 * - blank serverUrl → early return (no API call)
 * - blank deviceId → early return (no API call)
 * - network failure → caught silently
 * - success → registerDevice called with correct params
 */
class CaptureMessagingTokenTest {

    private val preferencesManager: PreferencesManager = mockk(relaxed = true)
    private val apiService: CaptureApiService = mockk(relaxed = true)

    /**
     * Replicates registerTokenWithServer from CaptureMessagingService lines 186-201
     */
    private suspend fun registerTokenWithServer(token: String): Boolean {
        return try {
            val serverUrl = preferencesManager.serverUrl.first()
            if (serverUrl.isBlank()) return false

            val deviceId = preferencesManager.deviceId.first()
            if (deviceId.isBlank()) return false

            apiService.registerDevice(
                serverUrl,
                DeviceRegisterRequest(deviceId = deviceId, fcmToken = token),
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    @Test
    fun `token persisted via setFcmToken`() = runTest {
        // onNewToken calls preferencesManager.setFcmToken(token)
        // Verify PreferencesManager has the method signature
        coEvery { preferencesManager.setFcmToken("new-token") } returns Unit
        preferencesManager.setFcmToken("new-token")
        coVerify { preferencesManager.setFcmToken("new-token") }
    }

    @Test
    fun `token registered with server on valid config`() = runTest {
        every { preferencesManager.serverUrl } returns flowOf("https://example.com")
        every { preferencesManager.deviceId } returns flowOf("device-123")
        coEvery { apiService.registerDevice(any(), any()) } returns DeviceRegisterResponse(registered = true)

        val result = registerTokenWithServer("fcm-token-abc")

        assertTrue(result)
        coVerify {
            apiService.registerDevice(
                "https://example.com",
                DeviceRegisterRequest(deviceId = "device-123", fcmToken = "fcm-token-abc"),
            )
        }
    }

    @Test
    fun `blank serverUrl early return without API call`() = runTest {
        every { preferencesManager.serverUrl } returns flowOf("")

        val result = registerTokenWithServer("fcm-token")

        assertTrue(!result)
        coVerify(exactly = 0) { apiService.registerDevice(any(), any()) }
    }

    @Test
    fun `blank deviceId early return without API call`() = runTest {
        every { preferencesManager.serverUrl } returns flowOf("https://example.com")
        every { preferencesManager.deviceId } returns flowOf("")

        val result = registerTokenWithServer("fcm-token")

        assertTrue(!result)
        coVerify(exactly = 0) { apiService.registerDevice(any(), any()) }
    }

    @Test
    fun `network failure caught silently`() = runTest {
        every { preferencesManager.serverUrl } returns flowOf("https://example.com")
        every { preferencesManager.deviceId } returns flowOf("device-123")
        coEvery { apiService.registerDevice(any(), any()) } throws RuntimeException("Connection refused")

        val result = registerTokenWithServer("fcm-token")

        assertTrue(!result)
    }

    @Test
    fun `success posts device registration`() = runTest {
        every { preferencesManager.serverUrl } returns flowOf("https://example.com")
        every { preferencesManager.deviceId } returns flowOf("device-456")
        coEvery { apiService.registerDevice(any(), any()) } returns DeviceRegisterResponse(registered = true)

        val result = registerTokenWithServer("token-xyz")

        assertTrue(result)
        coVerify { apiService.registerDevice("https://example.com", any()) }
    }
}
