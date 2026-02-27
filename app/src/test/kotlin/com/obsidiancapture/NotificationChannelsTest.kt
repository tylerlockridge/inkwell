package com.obsidiancapture

import com.obsidiancapture.notifications.NotificationChannels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NotificationChannelsTest {

    @Test
    fun `capture channel ID is correct`() {
        assertEquals("capture_new", NotificationChannels.CAPTURE_CHANNEL_ID)
    }

    @Test
    fun `gcal channel ID is correct`() {
        assertEquals("capture_gcal", NotificationChannels.GCAL_CHANNEL_ID)
    }

    @Test
    fun `sync status channel ID is correct`() {
        assertEquals("sync_status", NotificationChannels.SYNC_STATUS_CHANNEL_ID)
    }

    @Test
    fun `sync error channel ID is correct`() {
        assertEquals("sync_error", NotificationChannels.SYNC_ERROR_CHANNEL_ID)
    }

    @Test
    fun `all channel IDs are unique`() {
        val ids = listOf(
            NotificationChannels.CAPTURE_CHANNEL_ID,
            NotificationChannels.GCAL_CHANNEL_ID,
            NotificationChannels.SYNC_STATUS_CHANNEL_ID,
            NotificationChannels.SYNC_ERROR_CHANNEL_ID,
        )
        assertEquals(ids.size, ids.toSet().size)
    }
}
