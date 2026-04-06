package io.inkwell

import io.inkwell.data.remote.dto.CaptureRequest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for Slice 3 field additions to [CaptureRequest].
 */
class CaptureRequestSlice3Test {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `Slice 3 fields serialize when present`() {
        val request = CaptureRequest(
            body = "Test note",
            source = "android",
            captureType = "note",
            color = "teal",
            pinned = true,
            sourceUrl = "https://example.com",
            shared = false,
        )
        val serialized = json.encodeToString(CaptureRequest.serializer(), request)
        assertTrue("color should be present", serialized.contains("\"color\":\"teal\""))
        assertTrue("pinned should be present", serialized.contains("\"pinned\":true"))
        assertTrue("sourceUrl should be present", serialized.contains("\"sourceUrl\":\"https://example.com\""))
        assertTrue("shared should be present", serialized.contains("\"shared\":false"))
    }

    @Test
    fun `Slice 3 fields default to null`() {
        val request = CaptureRequest(body = "Test", source = "android")
        val serialized = json.encodeToString(CaptureRequest.serializer(), request)
        // With encodeDefaults=true, null fields still serialize as null
        assertTrue("Default request should serialize", serialized.contains("\"body\":\"Test\""))
    }

    @Test
    fun `CaptureMetadata DTO deserializes correctly`() {
        val jsonStr = """{"captureType":"note","listName":null,"items":null,"persistent":null,"shared":true,"shareToken":"abc123"}"""
        val meta = json.decodeFromString(io.inkwell.data.remote.dto.CaptureMetadata.serializer(), jsonStr)
        assertEquals("note", meta.captureType)
        assertEquals(true, meta.shared)
        assertEquals("abc123", meta.shareToken)
    }

    @Test
    fun `CaptureMetadataItem deserializes correctly`() {
        val jsonStr = """{"text":"Buy milk","checked":true}"""
        val item = json.decodeFromString(io.inkwell.data.remote.dto.CaptureMetadataItem.serializer(), jsonStr)
        assertEquals("Buy milk", item.text)
        assertTrue(item.checked)
    }

    @Test
    fun `CaptureMetadata with items array`() {
        val jsonStr = """{"captureType":"list_item","listName":"Groceries","items":[{"text":"Milk","checked":false},{"text":"Eggs","checked":true}],"persistent":true}"""
        val meta = json.decodeFromString(io.inkwell.data.remote.dto.CaptureMetadata.serializer(), jsonStr)
        assertEquals("list_item", meta.captureType)
        assertEquals("Groceries", meta.listName)
        assertEquals(2, meta.items?.size)
        assertEquals("Milk", meta.items!![0].text)
        assertFalse(meta.items!![0].checked)
        assertTrue(meta.items!![1].checked)
    }
}
