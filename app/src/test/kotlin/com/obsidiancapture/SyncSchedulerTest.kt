package com.obsidiancapture

import com.obsidiancapture.sync.SyncScheduler
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncSchedulerTest {

    @Test
    fun `sync work name is stable`() {
        assertEquals("periodic_sync", SyncScheduler.SYNC_WORK_NAME)
    }

    @Test
    fun `upload work name is stable`() {
        assertEquals("periodic_upload", SyncScheduler.UPLOAD_WORK_NAME)
    }

    @Test
    fun `immediate sync work name is stable`() {
        assertEquals("immediate_sync", SyncScheduler.SYNC_IMMEDIATE_NAME)
    }

    @Test
    fun `immediate upload work name is stable`() {
        assertEquals("immediate_upload", SyncScheduler.UPLOAD_IMMEDIATE_NAME)
    }

    @Test
    fun `work names are all unique`() {
        val names = listOf(
            SyncScheduler.SYNC_WORK_NAME,
            SyncScheduler.UPLOAD_WORK_NAME,
            SyncScheduler.SYNC_IMMEDIATE_NAME,
            SyncScheduler.UPLOAD_IMMEDIATE_NAME,
        )
        assertEquals(names.size, names.toSet().size)
    }
}
