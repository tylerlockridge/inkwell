package com.obsidiancapture

import android.net.Uri
import com.obsidiancapture.ui.navigation.DeepLink
import com.obsidiancapture.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for DeepLink URI constants, string formatting, and parseToRoute routing.
 */
@RunWith(RobolectricTestRunner::class)
class DeepLinkTest {

    @Test
    fun `capture URI constant matches scheme`() {
        assertEquals("obsidiancapture://capture", DeepLink.CAPTURE_URI)
    }

    @Test
    fun `inbox URI constant matches scheme`() {
        assertEquals("obsidiancapture://inbox", DeepLink.INBOX_URI)
    }

    @Test
    fun `note detail URI contains uid placeholder`() {
        assertEquals("obsidiancapture://note/{uid}", DeepLink.NOTE_DETAIL_URI)
    }

    @Test
    fun `system health URI constant matches scheme`() {
        assertEquals("obsidiancapture://system-health", DeepLink.SYSTEM_HEALTH_URI)
    }

    @Test
    fun `scheme constant is correct`() {
        assertEquals("obsidiancapture", DeepLink.SCHEME)
    }

    @Test
    fun `all URIs use correct scheme prefix`() {
        val uris = listOf(
            DeepLink.CAPTURE_URI,
            DeepLink.INBOX_URI,
            DeepLink.NOTE_DETAIL_URI,
            DeepLink.SYSTEM_HEALTH_URI,
        )
        for (uri in uris) {
            assert(uri.startsWith("${DeepLink.SCHEME}://")) { "URI $uri missing scheme prefix" }
        }
    }

    @Test
    fun `note detail URI has uid path segment`() {
        assert(DeepLink.NOTE_DETAIL_URI.contains("/note/")) { "NOTE_DETAIL_URI missing /note/ segment" }
        assert(DeepLink.NOTE_DETAIL_URI.contains("{uid}")) { "NOTE_DETAIL_URI missing {uid} placeholder" }
    }

    @Test
    fun `URI constants are all distinct`() {
        val uris = listOf(
            DeepLink.CAPTURE_URI,
            DeepLink.INBOX_URI,
            DeepLink.NOTE_DETAIL_URI,
            DeepLink.SYSTEM_HEALTH_URI,
        )
        assertEquals(uris.size, uris.toSet().size)
    }

    @Test
    fun `parseToRoute returns null for null URI`() {
        val result = DeepLink.parseToRoute(null)
        assertNull(result)
    }

    // --- US-007: parseToRoute routing tests ---

    @Test
    fun `custom scheme capture routes correctly`() {
        val uri = Uri.parse("obsidiancapture://capture")
        assertEquals(Screen.Capture.route, DeepLink.parseToRoute(uri))
    }

    @Test
    fun `custom scheme inbox routes correctly`() {
        val uri = Uri.parse("obsidiancapture://inbox")
        assertEquals(Screen.Inbox.route, DeepLink.parseToRoute(uri))
    }

    @Test
    fun `custom scheme system-health routes correctly`() {
        val uri = Uri.parse("obsidiancapture://system-health")
        assertEquals(Screen.SYSTEM_HEALTH_ROUTE, DeepLink.parseToRoute(uri))
    }

    @Test
    fun `custom scheme note with uid routes correctly`() {
        val uri = Uri.parse("obsidiancapture://note/abc-123")
        assertEquals(Screen.noteDetailRoute("abc-123"), DeepLink.parseToRoute(uri))
    }

    @Test
    fun `custom scheme note with blank uid returns null`() {
        // obsidiancapture://note/ has an empty path segment
        val uri = Uri.parse("obsidiancapture://note/")
        assertNull(DeepLink.parseToRoute(uri))
    }

    @Test
    fun `HTTPS capture routes correctly`() {
        val uri = Uri.parse("https://tyler-capture.duckdns.org/app/capture")
        assertEquals(Screen.Capture.route, DeepLink.parseToRoute(uri))
    }

    @Test
    fun `HTTPS inbox routes correctly`() {
        val uri = Uri.parse("https://tyler-capture.duckdns.org/app/inbox")
        assertEquals(Screen.Inbox.route, DeepLink.parseToRoute(uri))
    }

    @Test
    fun `HTTPS note with uid routes correctly`() {
        val uri = Uri.parse("https://tyler-capture.duckdns.org/app/note/xyz-789")
        assertEquals(Screen.noteDetailRoute("xyz-789"), DeepLink.parseToRoute(uri))
    }

    @Test
    fun `HTTPS note missing uid returns null`() {
        val uri = Uri.parse("https://tyler-capture.duckdns.org/app/note")
        assertNull(DeepLink.parseToRoute(uri))
    }

    @Test
    fun `wrong host returns null`() {
        val uri = Uri.parse("https://evil.example.com/app/capture")
        assertNull(DeepLink.parseToRoute(uri))
    }

    @Test
    fun `unknown destination returns null for custom scheme`() {
        val uri = Uri.parse("obsidiancapture://settings")
        assertNull(DeepLink.parseToRoute(uri))
    }

    @Test
    fun `unknown destination returns null for HTTPS`() {
        val uri = Uri.parse("https://tyler-capture.duckdns.org/app/unknown")
        assertNull(DeepLink.parseToRoute(uri))
    }
}
