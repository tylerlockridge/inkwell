package com.obsidiancapture

import com.obsidiancapture.data.local.PreferencesManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferencesDefaultsTest {

    @Test
    fun `default sync interval is 15 minutes`() {
        assertEquals(15L, PreferencesManager.DEFAULT_SYNC_INTERVAL)
    }

    @Test
    fun `isValidServerUrl allows HTTPS URLs`() {
        assertTrue(PreferencesManager.isValidServerUrl("https://example.com"))
        assertTrue(PreferencesManager.isValidServerUrl("https://tyler-capture.duckdns.org"))
        assertTrue(PreferencesManager.isValidServerUrl("https://10.0.0.1:8443"))
    }

    @Test
    fun `isValidServerUrl allows localhost HTTP for development`() {
        assertTrue(PreferencesManager.isValidServerUrl("http://localhost:3000"))
        assertTrue(PreferencesManager.isValidServerUrl("http://localhost"))
        assertTrue(PreferencesManager.isValidServerUrl("http://10.0.2.2:3000"))
    }

    @Test
    fun `isValidServerUrl rejects plain HTTP on non-local hosts`() {
        assertFalse(PreferencesManager.isValidServerUrl("http://example.com"))
        assertFalse(PreferencesManager.isValidServerUrl("http://192.168.1.1:3000"))
        assertFalse(PreferencesManager.isValidServerUrl("http://tyler-capture.duckdns.org"))
    }

    @Test
    fun `isValidServerUrl allows blank URL`() {
        assertTrue(PreferencesManager.isValidServerUrl(""))
        assertTrue(PreferencesManager.isValidServerUrl("   "))
    }
}
