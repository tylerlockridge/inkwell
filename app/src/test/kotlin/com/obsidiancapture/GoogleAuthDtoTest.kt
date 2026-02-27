package com.obsidiancapture

import com.obsidiancapture.data.remote.dto.GoogleAuthRequest
import com.obsidiancapture.data.remote.dto.GoogleAuthResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleAuthDtoTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `GoogleAuthRequest serializes correctly`() {
        val request = GoogleAuthRequest(idToken = "test-id-token-abc")
        val serialized = json.encodeToString(GoogleAuthRequest.serializer(), request)
        assertEquals("""{"idToken":"test-id-token-abc"}""", serialized)
    }

    @Test
    fun `GoogleAuthRequest deserializes correctly`() {
        val jsonStr = """{"idToken":"deserialized-token"}"""
        val request = json.decodeFromString(GoogleAuthRequest.serializer(), jsonStr)
        assertEquals("deserialized-token", request.idToken)
    }

    @Test
    fun `GoogleAuthResponse serializes correctly`() {
        val response = GoogleAuthResponse(token = "app-auth-token-xyz")
        val serialized = json.encodeToString(GoogleAuthResponse.serializer(), response)
        assertEquals("""{"token":"app-auth-token-xyz"}""", serialized)
    }

    @Test
    fun `GoogleAuthResponse deserializes correctly`() {
        val jsonStr = """{"token":"deserialized-app-token"}"""
        val response = json.decodeFromString(GoogleAuthResponse.serializer(), jsonStr)
        assertEquals("deserialized-app-token", response.token)
    }

    @Test
    fun `GoogleAuthResponse ignores unknown keys`() {
        val jsonStr = """{"token":"my-token","extraField":"ignored"}"""
        val response = json.decodeFromString(GoogleAuthResponse.serializer(), jsonStr)
        assertEquals("my-token", response.token)
    }
}
