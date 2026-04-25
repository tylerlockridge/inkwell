package io.inkwell.ui.navigation

sealed class Screen(
    val route: String,
    val label: String,
) {
    data object Capture : Screen("capture", "Capture")
    data object Inbox : Screen("inbox", "Inbox")
    data object Tasks : Screen("tasks", "Tasks")
    data object Notes : Screen("notes", "Notes")
    data object Lists : Screen("lists", "Lists")
    data object Settings : Screen("settings", "Settings")

    companion object {
        val bottomNavItems: List<Screen> by lazy { listOf(Capture, Inbox, Tasks, Notes, Lists) }
        const val NOTE_DETAIL_ROUTE = "note/{uid}"
        const val SYSTEM_HEALTH_ROUTE = "system-health"
        fun noteDetailRoute(uid: String) = "note/$uid"
    }
}
