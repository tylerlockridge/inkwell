package com.obsidiancapture

import com.obsidiancapture.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTest {
    @Test
    fun bottomNavItems_has_three_destinations() {
        assertEquals(3, Screen.bottomNavItems.size)
    }

    @Test
    fun routes_are_unique() {
        val routes = Screen.bottomNavItems.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun capture_is_first_destination() {
        assertEquals("capture", Screen.bottomNavItems[0].route)
    }
}
