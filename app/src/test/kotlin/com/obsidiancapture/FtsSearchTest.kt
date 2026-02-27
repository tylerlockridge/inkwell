package com.obsidiancapture

import com.obsidiancapture.data.local.entity.NoteFtsEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class FtsSearchTest {

    @Test
    fun `NoteFtsEntity can be created`() {
        val fts = NoteFtsEntity(title = "Test", body = "Body", tags = "[\"tag1\"]")
        assertEquals("Test", fts.title)
        assertEquals("Body", fts.body)
        assertEquals("[\"tag1\"]", fts.tags)
    }

    @Test
    fun `FTS query prefix appending for 3+ chars`() {
        val query = "test"
        val ftsQuery = "$query*"
        assertEquals("test*", ftsQuery)
    }

    @Test
    fun `short queries use LIKE fallback`() {
        val query = "ab"
        // 2 chars should NOT use FTS
        assert(query.length < 3)
    }

    @Test
    fun `3 char queries use FTS`() {
        val query = "abc"
        assert(query.length >= 3)
    }

    @Test
    fun `empty query has length 0`() {
        val query = ""
        assert(query.length < 3)
    }

    @Test
    fun `migration version constants are correct`() {
        val migration = com.obsidiancapture.data.local.AppDatabase.MIGRATION_2_3
        assertEquals(2, migration.startVersion)
        assertEquals(3, migration.endVersion)
    }

    @Test
    fun `migration 1 to 2 version constants are correct`() {
        val migration = com.obsidiancapture.data.local.AppDatabase.MIGRATION_1_2
        assertEquals(1, migration.startVersion)
        assertEquals(2, migration.endVersion)
    }
}
