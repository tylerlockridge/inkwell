package com.obsidiancapture

import com.obsidiancapture.data.remote.dto.CaptureRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureRequestTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `serializes minimal request with body only`() {
        val request = CaptureRequest(body = "Buy groceries")
        val serialized = json.encodeToString(request)
        assertTrue(serialized.contains("\"body\":\"Buy groceries\""))
        assertTrue(serialized.contains("\"source\":\"android\""))
    }

    @Test
    fun `serializes full request with all fields`() {
        val request = CaptureRequest(
            body = "Meeting notes",
            title = "Team standup",
            tags = listOf("work", "meetings"),
            kind = "complex",
            date = "2026-02-08",
            startTime = "10:00",
            endTime = "11:00",
            calendar = "work",
            priority = "high",
            source = "android",
            uuid = "test-uuid-123",
        )
        val serialized = json.encodeToString(request)
        assertTrue(serialized.contains("\"title\":\"Team standup\""))
        assertTrue(serialized.contains("\"kind\":\"complex\""))
        assertTrue(serialized.contains("\"priority\":\"high\""))
        assertTrue(serialized.contains("\"uuid\":\"test-uuid-123\""))
    }

    @Test
    fun `deserializes response`() {
        val responseJson = """{"path":"inbox/note.md","uid":"uid_abc123"}"""
        val response = Json.decodeFromString<com.obsidiancapture.data.remote.dto.CaptureResponse>(responseJson)
        assertEquals("uid_abc123", response.uid)
        assertEquals("inbox/note.md", response.path)
    }

    @Test
    fun `default source is android`() {
        val request = CaptureRequest(body = "test")
        assertEquals("android", request.source)
    }

    @Test
    fun `null optional fields are allowed`() {
        val request = CaptureRequest(body = "test")
        assertEquals(null, request.title)
        assertEquals(null, request.tags)
        assertEquals(null, request.kind)
        assertEquals(null, request.priority)
        assertEquals(null, request.date)
        assertEquals(null, request.uuid)
    }
}
