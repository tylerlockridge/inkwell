package com.obsidiancapture

import com.obsidiancapture.data.local.PreferencesManager
import com.obsidiancapture.sync.SyncScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for URL validation, sync interval, and security constraints.
 */
class UrlValidationTest {

    @Test
    fun `HTTPS URLs are always valid`() {
        assertTrue(PreferencesManager.isValidServerUrl("https://example.com"))
        assertTrue(PreferencesManager.isValidServerUrl("https://sub.domain.com:8443/path"))
        assertTrue(PreferencesManager.isValidServerUrl("HTTPS://EXAMPLE.COM"))
    }

    @Test
    fun `HTTP localhost is valid for development`() {
        assertTrue(PreferencesManager.isValidServerUrl("http://localhost"))
        assertTrue(PreferencesManager.isValidServerUrl("http://localhost:3000"))
        assertTrue(PreferencesManager.isValidServerUrl("http://localhost:8080/api"))
    }

    @Test
    fun `HTTP emulator address is valid for development`() {
        assertTrue(PreferencesManager.isValidServerUrl("http://10.0.2.2"))
        assertTrue(PreferencesManager.isValidServerUrl("http://10.0.2.2:3000"))
    }

    @Test
    fun `HTTP on public addresses is rejected`() {
        assertFalse(PreferencesManager.isValidServerUrl("http://example.com"))
        assertFalse(PreferencesManager.isValidServerUrl("http://192.168.1.100:3000"))
        assertFalse(PreferencesManager.isValidServerUrl("http://10.0.0.1:3000"))
        assertFalse(PreferencesManager.isValidServerUrl("http://my-server.local"))
    }

    @Test
    fun `non-HTTP schemes are rejected`() {
        assertFalse(PreferencesManager.isValidServerUrl("ftp://example.com"))
        assertFalse(PreferencesManager.isValidServerUrl("ws://example.com"))
        assertFalse(PreferencesManager.isValidServerUrl("file:///etc/passwd"))
    }

    @Test
    fun `plain text without scheme is rejected`() {
        assertFalse(PreferencesManager.isValidServerUrl("example.com"))
        assertFalse(PreferencesManager.isValidServerUrl("just some text"))
    }

    @Test
    fun `minimum sync interval is 15 minutes`() {
        assertEquals(15L, SyncScheduler.MIN_INTERVAL_MINUTES)
    }
}
