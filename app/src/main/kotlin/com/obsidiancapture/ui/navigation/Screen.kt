package com.obsidiancapture.ui.navigation

sealed class Screen(
    val route: String,
    val label: String,
) {
    data object Capture : Screen("capture", "Capture")
    data object Inbox : Screen("inbox", "Inbox")
    data object Settings : Screen("settings", "Settings")

    companion object {
        val bottomNavItems: List<Screen> by lazy { listOf(Capture, Inbox, Settings) }
        const val NOTE_DETAIL_ROUTE = "note/{uid}"
        const val SYSTEM_HEALTH_ROUTE = "system-health"
        fun noteDetailRoute(uid: String) = "note/$uid"
    }
}
