package com.obsidiancapture

import com.obsidiancapture.data.local.entity.NoteEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteEntityTest {

    @Test
    fun `creates entity with required fields`() {
        val note = NoteEntity(
            uid = "uid_abc123",
            title = "Test note",
            body = "Body content",
            created = "2026-02-08T10:00:00Z",
            updated = "2026-02-08T10:00:00Z",
        )
        assertEquals("uid_abc123", note.uid)
        assertEquals("Test note", note.title)
        assertEquals("Body content", note.body)
        assertEquals("one_shot", note.kind)
        assertEquals("open", note.status)
        assertEquals("[]", note.tags)
        assertEquals("android", note.source)
        assertFalse(note.pendingSync)
        assertFalse(note.gcalEnabled)
    }

    @Test
    fun `optional fields default to null`() {
        val note = NoteEntity(
            uid = "uid_test",
            title = "",
            body = "test",
            created = "2026-02-08T10:00:00Z",
            updated = "2026-02-08T10:00:00Z",
        )
        assertNull(note.priority)
        assertNull(note.calendar)
        assertNull(note.date)
        assertNull(note.startTime)
        assertNull(note.endTime)
        assertNull(note.gcalEventId)
        assertNull(note.gcalLastPushedAt)
        assertNull(note.syncedAt)
        assertNull(note.clientUuid)
        assertNull(note.syncError)
    }

    @Test
    fun `pending sync note has flag set`() {
        val note = NoteEntity(
            uid = "pending_uuid-123",
            title = "Offline note",
            body = "Saved without internet",
            created = "2026-02-08T10:00:00Z",
            updated = "2026-02-08T10:00:00Z",
            pendingSync = true,
            clientUuid = "uuid-123",
        )
        assertTrue(note.pendingSync)
        assertTrue(note.uid.startsWith("pending_"))
        assertEquals("uuid-123", note.clientUuid)
    }

    @Test
    fun `tagsToJson encodes list to JSON array string`() {
        assertEquals("[]", NoteEntity.tagsToJson(null))
        assertEquals("[]", NoteEntity.tagsToJson(emptyList()))
        assertEquals("""["urgent"]""", NoteEntity.tagsToJson(listOf("urgent")))
        assertEquals("""["work","project"]""", NoteEntity.tagsToJson(listOf("work", "project")))
    }

    @Test
    fun `tagsToJson handles special characters safely`() {
        val tags = listOf("tag with spaces", "tag\"with\"quotes", "tag,comma")
        val json = NoteEntity.tagsToJson(tags)
        // Should round-trip correctly
        val decoded = NoteEntity.tagsFromJson(json)
        assertEquals(tags, decoded)
    }

    @Test
    fun `tagsFromJson decodes JSON array string to list`() {
        assertEquals(emptyList<String>(), NoteEntity.tagsFromJson(""))
        assertEquals(emptyList<String>(), NoteEntity.tagsFromJson("[]"))
        assertEquals(listOf("urgent"), NoteEntity.tagsFromJson("""["urgent"]"""))
        assertEquals(listOf("a", "b"), NoteEntity.tagsFromJson("""["a","b"]"""))
    }

    @Test
    fun `tagsFromJson returns empty list on invalid JSON`() {
        assertEquals(emptyList<String>(), NoteEntity.tagsFromJson("not json"))
        assertEquals(emptyList<String>(), NoteEntity.tagsFromJson("{invalid}"))
    }

    @Test
    fun `copy updates only specified fields`() {
        val original = NoteEntity(
            uid = "uid_orig",
            title = "Original",
            body = "Body",
            created = "2026-02-08T10:00:00Z",
            updated = "2026-02-08T10:00:00Z",
            pendingSync = true,
        )
        val updated = original.copy(
            uid = "uid_synced",
            pendingSync = false,
            syncedAt = "2026-02-08T11:00:00Z",
        )
        assertEquals("uid_synced", updated.uid)
        assertFalse(updated.pendingSync)
        assertEquals("2026-02-08T11:00:00Z", updated.syncedAt)
        assertEquals("Original", updated.title) // unchanged
        assertEquals("Body", updated.body)       // unchanged
    }
}
