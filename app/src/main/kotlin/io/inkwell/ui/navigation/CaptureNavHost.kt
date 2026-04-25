package io.inkwell.ui.navigation

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.inkwell.ui.theme.md_theme_dark_primary
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import io.inkwell.ui.capture.CaptureScreen
import io.inkwell.ui.detail.NoteDetailScreen
import io.inkwell.ui.inbox.InboxScreen
import io.inkwell.ui.inbox.InboxTab
import io.inkwell.ui.health.SystemHealthScreen
import io.inkwell.ui.settings.SettingsScreen
import io.inkwell.ui.theme.CaptureAnimations

@Composable
fun CaptureNavHost(
    initialRoute: String? = null,
    sharedText: String? = null,
    sharedTitle: String? = null,
    sharedSourceUrl: String? = null,
    isFromShareIntent: Boolean = false,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route !in setOf(
        Screen.NOTE_DETAIL_ROUTE,
        Screen.SYSTEM_HEALTH_ROUTE,
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true
                        val (selectedIcon, unselectedIcon) = screen.icons()

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigateTopLevel(screen.route)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) selectedIcon else unselectedIcon,
                                    contentDescription = screen.label,
                                )
                            },
                            label = { Text(screen.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = md_theme_dark_primary,
                                selectedTextColor = md_theme_dark_primary,
                                indicatorColor = md_theme_dark_primary.copy(alpha = 0.15f),
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute ?: Screen.Capture.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { CaptureAnimations.screenEnter() },
            exitTransition = { CaptureAnimations.screenExit() },
            popEnterTransition = { CaptureAnimations.screenPopEnter() },
            popExitTransition = { CaptureAnimations.screenPopExit() },
        ) {
            composable(
                route = Screen.Capture.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = DeepLink.CAPTURE_URI },
                    navDeepLink { uriPattern = DeepLink.HTTPS_CAPTURE_URI },
                ),
            ) {
                CaptureScreen(
                    sharedText = sharedText,
                    sharedTitle = sharedTitle,
                    sharedSourceUrl = sharedSourceUrl,
                    isFromShareIntent = isFromShareIntent,
                    onNavigateToSettings = {
                        navController.navigateTopLevel(Screen.Settings.route)
                    },
                )
            }
            composable(
                route = Screen.Inbox.route,
                deepLinks = listOf(
                    navDeepLink { uriPattern = DeepLink.INBOX_URI },
                    navDeepLink { uriPattern = DeepLink.HTTPS_INBOX_URI },
                ),
            ) {
                InboxScreen(
                    initialTab = InboxTab.All,
                    onNoteClick = { uid ->
                        navController.navigate(Screen.noteDetailRoute(uid))
                    },
                    onNavigateToCapture = {
                        navController.navigateTopLevel(Screen.Capture.route)
                    },
                    onNavigateToSettings = {
                        navController.navigateTopLevel(Screen.Settings.route)
                    },
                )
            }
            composable(route = Screen.Tasks.route) {
                InboxScreen(
                    initialTab = InboxTab.Tasks,
                    onNoteClick = { uid ->
                        navController.navigate(Screen.noteDetailRoute(uid))
                    },
                    onNavigateToCapture = {
                        navController.navigateTopLevel(Screen.Capture.route)
                    },
                    onNavigateToSettings = {
                        navController.navigateTopLevel(Screen.Settings.route)
                    },
                )
            }
            composable(route = Screen.Notes.route) {
                InboxScreen(
                    initialTab = InboxTab.Notes,
                    onNoteClick = { uid ->
                        navController.navigate(Screen.noteDetailRoute(uid))
                    },
                    onNavigateToCapture = {
                        navController.navigateTopLevel(Screen.Capture.route)
                    },
                    onNavigateToSettings = {
                        navController.navigateTopLevel(Screen.Settings.route)
                    },
                )
            }
            composable(route = Screen.Lists.route) {
                InboxScreen(
                    initialTab = InboxTab.Lists,
                    onNoteClick = { uid ->
                        navController.navigate(Screen.noteDetailRoute(uid))
                    },
                    onNavigateToCapture = {
                        navController.navigateTopLevel(Screen.Capture.route)
                    },
                    onNavigateToSettings = {
                        navController.navigateTopLevel(Screen.Settings.route)
                    },
                )
            }
            composable(
                route = Screen.NOTE_DETAIL_ROUTE,
                arguments = listOf(navArgument("uid") { type = NavType.StringType }),
                deepLinks = listOf(
                    navDeepLink { uriPattern = DeepLink.NOTE_DETAIL_URI },
                    navDeepLink { uriPattern = DeepLink.HTTPS_NOTE_DETAIL_URI },
                ),
            ) {
                NoteDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToHealth = {
                        navController.navigate(Screen.SYSTEM_HEALTH_ROUTE)
                    },
                )
            }
            composable(
                route = Screen.SYSTEM_HEALTH_ROUTE,
                deepLinks = listOf(
                    navDeepLink { uriPattern = DeepLink.SYSTEM_HEALTH_URI },
                    navDeepLink { uriPattern = DeepLink.HTTPS_SYSTEM_HEALTH_URI },
                ),
            ) {
                SystemHealthScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private fun Screen.icons(): Pair<ImageVector, ImageVector> = when (this) {
    Screen.Capture -> Icons.Filled.Edit to Icons.Outlined.Edit
    Screen.Inbox -> Icons.Filled.Inbox to Icons.Outlined.Inbox
    Screen.Tasks -> Icons.Filled.TaskAlt to Icons.Outlined.TaskAlt
    Screen.Notes -> Icons.AutoMirrored.Filled.Article to Icons.AutoMirrored.Outlined.Article
    Screen.Lists -> Icons.AutoMirrored.Filled.FormatListBulleted to
        Icons.AutoMirrored.Outlined.FormatListBulleted
    Screen.Settings -> Icons.Filled.Settings to Icons.Outlined.Settings
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
