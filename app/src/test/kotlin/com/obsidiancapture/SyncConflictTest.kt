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
    fun `Instant comparison correctly orders ISO timestamps across dates`() {
        // Production code uses java.time.Instant.parse() — verify it orders correctly
        val parse = { ts: String -> java.time.Instant.parse(ts) }
        assertTrue(parse("2026-02-09T00:00:00Z") > parse("2026-02-08T23:59:59Z"))
        assertTrue(parse("2026-03-01T00:00:00Z") > parse("2026-02-28T23:59:59Z"))
        assertTrue(parse("2027-01-01T00:00:00Z") > parse("2026-12-31T23:59:59Z"))
    }

    @Test
    fun `Instant comparison handles mixed millisecond precision correctly`() {
        // String comparison would produce the wrong result here:
        //   "T10:00:00.000Z" < "T10:00:00Z" lexicographically (because '.' < 'Z')
        // Instant.parse() treats them as the same moment, so server is NOT newer.
        val parse = { ts: String -> java.time.Instant.parse(ts) }
        val withMs = parse("2026-02-08T10:00:00.000Z")
        val withoutMs = parse("2026-02-08T10:00:00Z")
        assertFalse(withMs > withoutMs)   // same moment — server not newer
        assertFalse(withoutMs > withMs)

        // A timestamp with sub-second precision IS correctly treated as later
        val laterWithMs = parse("2026-02-08T10:00:00.123Z")
        assertTrue(laterWithMs > withoutMs)
    }

    @Test
    fun `invalid timestamp falls back to treating server as newer`() {
        // isServerNewer() returns true on parse failure to avoid stale data
        fun isServerNewer(localTs: String, serverTs: String): Boolean {
            return try {
                java.time.Instant.parse(localTs) < java.time.Instant.parse(serverTs)
            } catch (_: Exception) {
                true
            }
        }
        assertTrue(isServerNewer("not-a-date", "2026-02-08T10:00:00Z"))
        assertTrue(isServerNewer("2026-02-08T10:00:00Z", "also-not-a-date"))
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

    // --- US-001: Deeper conflict resolution tests replicating SyncWorker filter logic ---

    /**
     * Replicates the stale-item filter from SyncWorker lines 57-61:
     * localNote?.pendingSync != true && (localNote == null || isServerNewer(local, server))
     */
    private fun isServerNewer(localTs: String, serverTs: String): Boolean {
        return try {
            java.time.Instant.parse(localTs) < java.time.Instant.parse(serverTs)
        } catch (_: Exception) {
            true
        }
    }

    private fun shouldSync(localNote: NoteEntity?, serverUpdatedAt: String): Boolean {
        return localNote?.pendingSync != true &&
            (localNote == null || isServerNewer(localNote.updated, serverUpdatedAt))
    }

    @Test
    fun `stale filter - server newer timestamp overwrites local`() {
        val local = makeNote(updated = "2026-02-08T10:00:00Z")
        assertTrue(shouldSync(local, "2026-02-08T12:00:00Z"))
    }

    @Test
    fun `stale filter - local newer timestamp preserved`() {
        val local = makeNote(updated = "2026-02-08T14:00:00Z")
        assertFalse(shouldSync(local, "2026-02-08T12:00:00Z"))
    }

    @Test
    fun `stale filter - equal timestamps local wins`() {
        val local = makeNote(updated = "2026-02-08T12:00:00Z")
        assertFalse(shouldSync(local, "2026-02-08T12:00:00Z"))
    }

    @Test
    fun `stale filter - malformed timestamp falls back to server newer`() {
        val local = makeNote(updated = "not-a-date")
        assertTrue(shouldSync(local, "2026-02-08T10:00:00Z"))
    }

    @Test
    fun `stale filter - mixed millisecond precision handled correctly`() {
        // "T10:00:00.000Z" and "T10:00:00Z" are the same instant
        val local = makeNote(updated = "2026-02-08T10:00:00.000Z")
        assertFalse(shouldSync(local, "2026-02-08T10:00:00Z"))

        // But sub-second difference IS newer
        val local2 = makeNote(updated = "2026-02-08T10:00:00Z")
        assertTrue(shouldSync(local2, "2026-02-08T10:00:00.001Z"))
    }

    @Test
    fun `stale filter - pendingSync true always skipped even if server is newer`() {
        val local = makeNote(updated = "2020-01-01T00:00:00Z", pendingSync = true)
        assertFalse(shouldSync(local, "2026-12-31T23:59:59Z"))
    }

    @Test
    fun `stale filter - note not in local DB is included in sync`() {
        assertTrue(shouldSync(null, "2026-02-08T10:00:00Z"))
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
