package com.obsidiancapture

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the Settings screen.
 * Verifies section headers, key UI elements, and basic navigation render correctly.
 */
@HiltAndroidTest
class SettingsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        Espresso.closeSoftKeyboard()
        // Navigate to the Settings tab (match by nav label text)
        composeRule.onNodeWithText("Settings", useUnmergedTree = true).performClick()
    }

    @Test
    fun settingsScreen_title_isDisplayed() {
        // Nav bar label + screen title both contain "Settings"; first visible node is sufficient
        composeRule.onAllNodesWithText("Settings")[0].assertIsDisplayed()
    }

    @Test
    fun settingsScreen_systemHealthCard_isDisplayed() {
        composeRule.onNodeWithText("System Health").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_serverConnectionSection_isDisplayed() {
        composeRule.onNodeWithText("Server Connection").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_syncNowButton_isDisplayed() {
        composeRule.onNodeWithText("Sync Now").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_pushNotificationsToggle_isDisplayed() {
        composeRule.onNodeWithText("Push Notifications").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_hapticFeedbackToggle_isDisplayed() {
        // Haptic Feedback may be below the fold — scroll to it first
        composeRule.onNodeWithText("Haptic Feedback").performScrollTo().assertIsDisplayed()
    }
}
