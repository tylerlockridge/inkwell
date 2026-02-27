package com.obsidiancapture

import com.obsidiancapture.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationTest {

    @Test
    fun `note detail route contains uid placeholder`() {
        assertEquals("note/{uid}", Screen.NOTE_DETAIL_ROUTE)
    }

    @Test
    fun `noteDetailRoute generates correct path`() {
        assertEquals("note/uid_abc123", Screen.noteDetailRoute("uid_abc123"))
    }

    @Test
    fun `noteDetailRoute handles encoded characters`() {
        assertEquals("note/uid_with%20space", Screen.noteDetailRoute("uid_with%20space"))
    }

    @Test
    fun `bottom nav has 3 items`() {
        assertEquals(3, Screen.bottomNavItems.size)
    }

    @Test
    fun `capture is first nav item`() {
        assertEquals("capture", Screen.bottomNavItems[0].route)
    }
}
