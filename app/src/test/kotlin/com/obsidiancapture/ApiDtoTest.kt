package com.obsidiancapture

import com.obsidiancapture.data.remote.dto.GcalStatus
import com.obsidiancapture.data.remote.dto.InboxItem
import com.obsidiancapture.data.remote.dto.InboxResponse
import com.obsidiancapture.data.remote.dto.NoteDetailResponse
import com.obsidiancapture.data.remote.dto.NoteFrontmatter
import com.obsidiancapture.data.remote.dto.NoteUpdateRequest
import com.obsidiancapture.data.remote.dto.NoteUpdateResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserializes inbox response`() {
        val raw = """{"items":[{"uid":"uid_1","path":"inbox/test.md","status":"open","kind":"one_shot","content_hash":"abc","updated_at":"2026-02-08"}],"totalCount":1,"syncToken":"2026-02-08T10:00:00Z"}"""
        val response = json.decodeFromString<InboxResponse>(raw)
        assertEquals(1, response.items.size)
        assertEquals("uid_1", response.items[0].uid)
        assertEquals(1, response.totalCount)
    }

    @Test
    fun `deserializes note detail response`() {
        val raw = """{"uid":"uid_1","frontmatter":{"uid":"uid_1","title":"Test","kind":"one_shot","status":"open","created":"2026-02-08","updated":"2026-02-08","tags":["work"]},"body":"Hello world"}"""
        val response = json.decodeFromString<NoteDetailResponse>(raw)
        assertEquals("uid_1", response.uid)
        assertEquals("Test", response.frontmatter.title)
        assertEquals(listOf("work"), response.frontmatter.tags)
        assertEquals("Hello world", response.body)
        assertNull(response.gcalStatus)
    }

    @Test
    fun `deserializes note detail with gcal status`() {
        val raw = """{"uid":"uid_1","frontmatter":{"uid":"uid_1","kind":"complex","status":"open","created":"","updated":"","tags":[]},"body":"test","gcalStatus":{"eventId":"evt_123","lastPushedAt":"2026-02-08T10:00:00Z","lastError":null}}"""
        val response = json.decodeFromString<NoteDetailResponse>(raw)
        assertEquals("evt_123", response.gcalStatus?.eventId)
        assertNull(response.gcalStatus?.lastError)
    }

    @Test
    fun `serializes note update request`() {
        val request = NoteUpdateRequest(status = "done")
        val serialized = json.encodeToString(NoteUpdateRequest.serializer(), request)
        assert(serialized.contains("\"status\":\"done\""))
    }

    @Test
    fun `deserializes note update response`() {
        val raw = """{"uid":"uid_1","updated":"2026-02-08T12:00:00Z"}"""
        val response = json.decodeFromString<NoteUpdateResponse>(raw)
        assertEquals("uid_1", response.uid)
    }

    @Test
    fun `inbox item defaults`() {
        val item = InboxItem(uid = "uid_1", path = "inbox/test.md", status = "open", kind = "one_shot")
        assertEquals("", item.content_hash)
        assertEquals("", item.updated_at)
    }

    @Test
    fun `frontmatter defaults`() {
        val fm = NoteFrontmatter()
        assertEquals("one_shot", fm.kind)
        assertEquals("open", fm.status)
        assertEquals(emptyList<String>(), fm.tags)
        assertNull(fm.title)
        assertNull(fm.priority)
    }

    @Test
    fun `gcal status all nullable`() {
        val gcal = GcalStatus()
        assertNull(gcal.eventId)
        assertNull(gcal.lastPushedAt)
        assertNull(gcal.lastError)
    }
}
