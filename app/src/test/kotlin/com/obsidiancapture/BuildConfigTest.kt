package com.obsidiancapture

import com.obsidiancapture.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for build configuration constants and navigation structure
 * that must remain stable across release builds.
 */
class BuildConfigTest {

    @Test
    fun `bottom nav has exactly three items`() {
        assertEquals(3, Screen.bottomNavItems.size)
    }

    @Test
    fun `bottom nav order is capture inbox settings`() {
        val routes = Screen.bottomNavItems.map { it.route }
        assertEquals(listOf("capture", "inbox", "settings"), routes)
    }

    @Test
    fun `note detail route contains uid parameter`() {
        assertTrue(Screen.NOTE_DETAIL_ROUTE.contains("{uid}"))
    }

    @Test
    fun `note detail route builder creates valid path`() {
        val route = Screen.noteDetailRoute("uid_abc123")
        assertEquals("note/uid_abc123", route)
    }

    @Test
    fun `screen labels are user-facing`() {
        assertEquals("Capture", Screen.Capture.label)
        assertEquals("Inbox", Screen.Inbox.label)
        assertEquals("Settings", Screen.Settings.label)
    }
}
