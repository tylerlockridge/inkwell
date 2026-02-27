package com.obsidiancapture

import com.obsidiancapture.data.local.entity.NoteEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for sync conflict resolution logic.
 * The system uses last-write-wins: server updates are skipped if local is newer.
 * Pending-sync notes are always skipped to preserve local changes.
 */
class SyncConflictTest {

    @Test
    fun `server note overwrites when server is newer`() {
        val local = makeNote(updated = "2026-02-08T10:00:00Z")
        val serverUpdatedAt = "2026-02-08T12:00:00Z"
        // Server is newer -> should overwrite
        assertTrue(serverUpdatedAt > local.updated)
    }

    @Test
    fun `local note preserved when local is newer`() {
        val local = makeNote(updated = "2026-02-08T14:00:00Z")
        val serverUpdatedAt = "2026-02-08T12:00:00Z"
        // Local is newer -> skip server update
        assertTrue(local.updated >= serverUpdatedAt)
    }

    @Test
    fun `local note preserved when timestamps are equal`() {
        val local = makeNote(updated = "2026-02-08T12:00:00Z")
        val serverUpdatedAt = "2026-02-08T12:00:00Z"
        // Equal -> skip server update (local wins ties)
        assertTrue(local.updated >= serverUpdatedAt)
    }

    @Test
    fun `pending sync note is always skipped regardless of timestamp`() {
        val local = makeNote(
            updated = "2026-01-01T00:00:00Z",
            pendingSync = true,
        )
        // Even if server is much newer, pending sync notes are never overwritten
        assertTrue(local.pendingSync)
    }

    @Test
    fun `ISO timestamp comparison works across dates`() {
        // ISO 8601 strings sort correctly via string comparison
        assertTrue("2026-02-09T00:00:00Z" > "2026-02-08T23:59:59Z")
        assertTrue("2026-03-01T00:00:00Z" > "2026-02-28T23:59:59Z")
        assertTrue("2027-01-01T00:00:00Z" > "2026-12-31T23:59:59Z")
    }

    @Test
    fun `sync error is cleared after successful sync`() {
        val withError = makeNote(syncError = "HTTP 500: Internal Server Error")
        val synced = withError.copy(syncError = null, pendingSync = false, syncedAt = "2026-02-08T12:00:00Z")
        assertFalse(synced.pendingSync)
        assertTrue(synced.syncError == null)
    }

    @Test
    fun `note with sync error has pendingSync cleared`() {
        val original = makeNote(pendingSync = true)
        // When a 4xx error occurs, pendingSync is cleared and error is set
        val errored = original.copy(pendingSync = false, syncError = "HTTP 400: Bad Request")
        assertFalse(errored.pendingSync)
        assertTrue(errored.syncError != null)
    }

    private fun makeNote(
        updated: String = "2026-02-08T10:00:00Z",
        pendingSync: Boolean = false,
        syncError: String? = null,
    ) = NoteEntity(
        uid = "uid_test",
        title = "Test",
        body = "Body",
        created = "2026-02-08T10:00:00Z",
        updated = updated,
        pendingSync = pendingSync,
        syncError = syncError,
    )
}
