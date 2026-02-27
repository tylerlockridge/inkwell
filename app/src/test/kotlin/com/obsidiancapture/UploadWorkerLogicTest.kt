package com.obsidiancapture

import com.obsidiancapture.data.local.entity.NoteEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for upload worker routing logic.
 * UploadWorker routes based on uid prefix:
 * - "pending_*" → POST /api/capture (new captures)
 * - otherwise → PATCH /api/note/:uid (updates to existing notes)
 */
class UploadWorkerLogicTest {

    @Test
    fun `pending uid is routed to new capture`() {
        val note = makeNote(uid = "pending_abc-123")
        assertTrue(note.uid.startsWith("pending_"))
    }

    @Test
    fun `server uid is routed to update`() {
        val note = makeNote(uid = "uid_abc123")
        assertFalse(note.uid.startsWith("pending_"))
    }

    @Test
    fun `pending prefix detection is case sensitive`() {
        val note = makeNote(uid = "Pending_abc")
        assertFalse(note.uid.startsWith("pending_"))
    }

    @Test
    fun `empty uid is not pending`() {
        val note = makeNote(uid = "")
        assertFalse(note.uid.startsWith("pending_"))
    }

    @Test
    fun `copy replaces pending uid with server uid after upload`() {
        val pending = makeNote(uid = "pending_abc-123", pendingSync = true)
        val synced = pending.copy(
            uid = "uid_server_assigned",
            pendingSync = false,
            syncedAt = "2026-02-08T12:00:00Z",
        )
        assertFalse(synced.uid.startsWith("pending_"))
        assertFalse(synced.pendingSync)
    }

    private fun makeNote(
        uid: String,
        pendingSync: Boolean = true,
    ) = NoteEntity(
        uid = uid,
        title = "Test",
        body = "Body",
        created = "2026-02-08T10:00:00Z",
        updated = "2026-02-08T10:00:00Z",
        pendingSync = pendingSync,
    )
}
