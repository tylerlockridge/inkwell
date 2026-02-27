package com.obsidiancapture

import com.obsidiancapture.ui.navigation.DeepLink
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for DeepLink URI constants and string formatting.
 * Note: parseToRoute and Uri-building methods require Android framework (instrumented tests).
 * These tests validate the string constants and scheme used for deep links.
 */
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
        // parseToRoute(null) should return null — this is pure Kotlin
        val result = DeepLink.parseToRoute(null)
        assertEquals(null, result)
    }
}
