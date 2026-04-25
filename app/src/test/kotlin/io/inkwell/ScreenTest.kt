package io.inkwell

import io.inkwell.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ScreenTest {
    @Test
    fun bottomNavItems_has_five_daily_destinations() {
        assertEquals(5, Screen.bottomNavItems.size)
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

    @Test
    fun settings_is_supporting_route_not_bottom_nav_destination() {
        val routes = Screen.bottomNavItems.map { it.route }
        assertFalse(routes.contains(Screen.Settings.route))
        assertEquals("settings", Screen.Settings.route)
    }
}
