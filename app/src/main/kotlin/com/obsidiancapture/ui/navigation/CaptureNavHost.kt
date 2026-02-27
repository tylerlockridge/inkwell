package com.obsidiancapture.ui.navigation

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.obsidiancapture.ui.capture.CaptureScreen
import com.obsidiancapture.ui.detail.NoteDetailScreen
import com.obsidiancapture.ui.inbox.InboxScreen
import com.obsidiancapture.ui.health.SystemHealthScreen
import com.obsidiancapture.ui.settings.SettingsScreen
import com.obsidiancapture.ui.theme.CaptureAnimations

@Composable
fun CaptureNavHost(
    initialRoute: String? = null,
    sharedText: String? = null,
    sharedTitle: String? = null,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on health screen only
    val showBottomBar = currentDestination?.route != Screen.SYSTEM_HEALTH_ROUTE

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
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) selectedIcon else unselectedIcon,
                                    contentDescription = screen.label,
                                )
                            },
                            label = { Text(screen.label) },
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
                deepLinks = listOf(navDeepLink { uriPattern = DeepLink.CAPTURE_URI }),
            ) {
                CaptureScreen(
                    sharedText = sharedText,
                    sharedTitle = sharedTitle,
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(
                route = Screen.Inbox.route,
                deepLinks = listOf(navDeepLink { uriPattern = DeepLink.INBOX_URI }),
            ) {
                InboxScreen(
                    onNoteClick = { uid ->
                        navController.navigate(Screen.noteDetailRoute(uid))
                    },
                    onNavigateToCapture = {
                        navController.navigate(Screen.Capture.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(
                route = Screen.NOTE_DETAIL_ROUTE,
                arguments = listOf(navArgument("uid") { type = NavType.StringType }),
                deepLinks = listOf(navDeepLink { uriPattern = DeepLink.NOTE_DETAIL_URI }),
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
                deepLinks = listOf(navDeepLink { uriPattern = DeepLink.SYSTEM_HEALTH_URI }),
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
    Screen.Settings -> Icons.Filled.Settings to Icons.Outlined.Settings
}
