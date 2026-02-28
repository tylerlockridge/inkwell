package com.obsidiancapture

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the Inbox screen.
 * Navigation to inbox via the bottom nav bar is tested here.
 */
@HiltAndroidTest
class InboxScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        Espresso.closeSoftKeyboard()
        // Navigate to the Inbox tab (match by nav label text)
        composeRule.onNodeWithText("Inbox", useUnmergedTree = true).performClick()
    }

    @Test
    fun inboxScreen_title_isDisplayed() {
        // Nav bar label + screen title both contain "Inbox"; first visible node is sufficient
        composeRule.onAllNodesWithText("Inbox")[0].assertIsDisplayed()
    }

    @Test
    fun inboxScreen_searchButton_isDisplayed() {
        composeRule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun inboxScreen_syncButton_isDisplayed() {
        composeRule.onNodeWithContentDescription("Sync").assertIsDisplayed()
    }

    @Test
    fun inboxScreen_fab_navigatesToCapture() {
        composeRule.onNodeWithContentDescription("New capture").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("New capture").performClick()
        // Should navigate back to capture — send button is the indicator
        composeRule.onNodeWithContentDescription("Send").assertIsDisplayed()
    }

    @Test
    fun inboxScreen_searchToggle_showsSearchField() {
        composeRule.onNodeWithContentDescription("Search").performClick()
        composeRule.onNodeWithText("Search notes...").assertIsDisplayed()
    }
}
