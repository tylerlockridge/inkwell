package com.obsidiancapture

import com.obsidiancapture.notifications.NotificationActionReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NotificationActionReceiverTest {

    @Test
    fun `ACTION_MARK_DONE has correct value`() {
        assertEquals("com.obsidiancapture.ACTION_MARK_DONE", NotificationActionReceiver.ACTION_MARK_DONE)
    }

    @Test
    fun `ACTION_RETRY_SYNC has correct value`() {
        assertEquals("com.obsidiancapture.ACTION_RETRY_SYNC", NotificationActionReceiver.ACTION_RETRY_SYNC)
    }

    @Test
    fun `actions are distinct`() {
        assertNotEquals(NotificationActionReceiver.ACTION_MARK_DONE, NotificationActionReceiver.ACTION_RETRY_SYNC)
    }

    @Test
    fun `EXTRA_NOTE_UID key is correct`() {
        assertEquals("extra_note_uid", NotificationActionReceiver.EXTRA_NOTE_UID)
    }

    @Test
    fun `EXTRA_NOTIFICATION_ID key is correct`() {
        assertEquals("extra_notification_id", NotificationActionReceiver.EXTRA_NOTIFICATION_ID)
    }
}
