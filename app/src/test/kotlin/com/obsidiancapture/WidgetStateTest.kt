package com.obsidiancapture

import com.obsidiancapture.widget.WidgetStateUpdater
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WidgetStateTest {

    @Test
    fun `KEY_INBOX_COUNT has correct name`() {
        assertEquals("inbox_count", WidgetStateUpdater.KEY_INBOX_COUNT.name)
    }

    @Test
    fun `KEY_PENDING_SYNC_COUNT has correct name`() {
        assertEquals("pending_sync_count", WidgetStateUpdater.KEY_PENDING_SYNC_COUNT.name)
    }

    @Test
    fun `keys are distinct`() {
        assertNotNull(WidgetStateUpdater.KEY_INBOX_COUNT)
        assertNotNull(WidgetStateUpdater.KEY_PENDING_SYNC_COUNT)
        assert(WidgetStateUpdater.KEY_INBOX_COUNT != WidgetStateUpdater.KEY_PENDING_SYNC_COUNT)
    }

    @Test
    fun `InboxCountWidget can be instantiated`() {
        val widget = com.obsidiancapture.widget.InboxCountWidget()
        assertNotNull(widget)
    }

    @Test
    fun `QuickCaptureWidget can be instantiated`() {
        val widget = com.obsidiancapture.widget.QuickCaptureWidget()
        assertNotNull(widget)
    }

    @Test
    fun `InboxCountWidgetReceiver can be instantiated`() {
        val receiver = com.obsidiancapture.widget.InboxCountWidgetReceiver()
        assertNotNull(receiver)
    }

    @Test
    fun `QuickCaptureWidgetReceiver can be instantiated`() {
        val receiver = com.obsidiancapture.widget.QuickCaptureWidgetReceiver()
        assertNotNull(receiver)
    }
}
