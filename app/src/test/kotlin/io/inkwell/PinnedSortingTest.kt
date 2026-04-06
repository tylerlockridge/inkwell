package io.inkwell

import io.inkwell.data.local.entity.BrowseType
import io.inkwell.data.local.entity.NoteEntity
import io.inkwell.data.local.entity.browseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for I15 pinned-first sorting behavior.
 *
 * The actual SQL ordering is `ORDER BY pinned DESC, created DESC`.
 * These tests validate the expected sort contract by simulating
 * the ordering on in-memory lists (since Room DAO tests require
 * an instrumented environment or Robolectric with an in-memory DB).
 */
class PinnedSortingTest {

    private fun note(
        uid: String,
        pinned: Boolean = false,
        created: String = "2026-03-28T12:00:00Z",
        captureType: String? = "task",
    ) = NoteEntity(
        uid = uid,
        title = uid,
        body = "body",
        pinned = pinned,
        created = created,
        updated = created,
        captureType = captureType,
    )

    /** Simulates the SQL: ORDER BY pinned DESC, created DESC */
    private fun List<NoteEntity>.pinnedFirstSort(): List<NoteEntity> =
        sortedWith(compareByDescending<NoteEntity> { it.pinned }.thenByDescending { it.created })

    // =====================================================================
    // Core ordering
    // =====================================================================

    @Test
    fun `pinned note sorts above unpinned even when older`() {
        val older = note("old-pinned", pinned = true, created = "2026-03-01T00:00:00Z")
        val newer = note("new-unpinned", pinned = false, created = "2026-03-28T00:00:00Z")
        val sorted = listOf(newer, older).pinnedFirstSort()
        assertEquals("old-pinned", sorted[0].uid)
        assertEquals("new-unpinned", sorted[1].uid)
    }

    @Test
    fun `unpinned notes remain reverse-chronological among themselves`() {
        val a = note("oldest", created = "2026-03-01T00:00:00Z")
        val b = note("middle", created = "2026-03-15T00:00:00Z")
        val c = note("newest", created = "2026-03-28T00:00:00Z")
        val sorted = listOf(a, c, b).pinnedFirstSort()
        assertEquals("newest", sorted[0].uid)
        assertEquals("middle", sorted[1].uid)
        assertEquals("oldest", sorted[2].uid)
    }

    @Test
    fun `pinned notes are reverse-chronological among themselves`() {
        val a = note("old-pin", pinned = true, created = "2026-03-01T00:00:00Z")
        val b = note("new-pin", pinned = true, created = "2026-03-28T00:00:00Z")
        val sorted = listOf(a, b).pinnedFirstSort()
        assertEquals("new-pin", sorted[0].uid)
        assertEquals("old-pin", sorted[1].uid)
    }

    @Test
    fun `mixed pinned and unpinned sort correctly`() {
        val notes = listOf(
            note("unpin-new", pinned = false, created = "2026-03-28T00:00:00Z"),
            note("pin-old", pinned = true, created = "2026-03-01T00:00:00Z"),
            note("unpin-old", pinned = false, created = "2026-03-01T00:00:00Z"),
            note("pin-new", pinned = true, created = "2026-03-28T00:00:00Z"),
        )
        val sorted = notes.pinnedFirstSort()
        assertEquals("pin-new", sorted[0].uid)
        assertEquals("pin-old", sorted[1].uid)
        assertEquals("unpin-new", sorted[2].uid)
        assertEquals("unpin-old", sorted[3].uid)
    }

    // =====================================================================
    // Tab filtering preserves pinned-first ordering
    // =====================================================================

    @Test
    fun `type filtering preserves pinned-first ordering`() {
        val notes = listOf(
            note("task-unpin", pinned = false, created = "2026-03-28T00:00:00Z", captureType = "task"),
            note("task-pin", pinned = true, created = "2026-03-01T00:00:00Z", captureType = "task"),
            note("note-pin", pinned = true, created = "2026-03-28T00:00:00Z", captureType = "note"),
        )
        val sorted = notes.pinnedFirstSort()
        val tasksOnly = sorted.filter { it.browseType == BrowseType.TASK }
        assertEquals(2, tasksOnly.size)
        assertEquals("task-pin", tasksOnly[0].uid)
        assertEquals("task-unpin", tasksOnly[1].uid)
    }

    // =====================================================================
    // Search results respect pinned ordering
    // =====================================================================

    @Test
    fun `search results respect pinned ordering`() {
        // Simulates search returning matching notes, then applying pinned sort
        val searchResults = listOf(
            note("match-unpin", pinned = false, created = "2026-03-28T00:00:00Z"),
            note("match-pin", pinned = true, created = "2026-03-01T00:00:00Z"),
        )
        val sorted = searchResults.pinnedFirstSort()
        assertEquals("match-pin", sorted[0].uid)
    }

    // =====================================================================
    // Edge cases
    // =====================================================================

    @Test
    fun `empty list sorts without error`() {
        val sorted = emptyList<NoteEntity>().pinnedFirstSort()
        assertTrue(sorted.isEmpty())
    }

    @Test
    fun `single pinned note is first and only`() {
        val sorted = listOf(note("only", pinned = true)).pinnedFirstSort()
        assertEquals(1, sorted.size)
        assertEquals("only", sorted[0].uid)
    }

    @Test
    fun `all pinned notes sort reverse-chronologically`() {
        val notes = listOf(
            note("a", pinned = true, created = "2026-03-01T00:00:00Z"),
            note("b", pinned = true, created = "2026-03-15T00:00:00Z"),
            note("c", pinned = true, created = "2026-03-28T00:00:00Z"),
        )
        val sorted = notes.pinnedFirstSort()
        assertEquals("c", sorted[0].uid)
        assertEquals("b", sorted[1].uid)
        assertEquals("a", sorted[2].uid)
    }

    @Test
    fun `all unpinned notes sort reverse-chronologically`() {
        val notes = listOf(
            note("a", created = "2026-03-01T00:00:00Z"),
            note("b", created = "2026-03-28T00:00:00Z"),
        )
        val sorted = notes.pinnedFirstSort()
        assertEquals("b", sorted[0].uid)
        assertEquals("a", sorted[1].uid)
    }
}
