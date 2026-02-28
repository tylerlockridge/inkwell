package com.obsidiancapture

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the Capture screen (the default start destination).
 * These tests verify core UI elements render correctly and basic interactions work.
 */
@HiltAndroidTest
class CaptureScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun captureScreen_sendButton_isDisplayed() {
        composeRule.onNodeWithContentDescription("Send").assertIsDisplayed()
    }

    @Test
    fun captureScreen_metadataExpandButton_isDisplayed() {
        composeRule.onNodeWithContentDescription("Expand metadata").assertIsDisplayed()
    }

    @Test
    fun captureScreen_metadataExpandButton_togglesOnClick() {
        composeRule.onNodeWithContentDescription("Expand metadata").performClick()
        composeRule.onNodeWithContentDescription("Collapse metadata").assertIsDisplayed()
    }

    @Test
    fun captureScreen_typeTextField_acceptsInput() {
        composeRule.onNodeWithText("What's on your mind?", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Send").assertIsDisplayed()
    }

    @Test
    fun captureScreen_expandMetadata_showsTagsButton() {
        composeRule.onNodeWithContentDescription("Expand metadata").performClick()
        composeRule.onNodeWithContentDescription("Tags").assertIsDisplayed()
    }

    @Test
    fun captureScreen_expandMetadata_showsScheduleButton() {
        composeRule.onNodeWithContentDescription("Expand metadata").performClick()
        composeRule.onNodeWithContentDescription("Time").assertIsDisplayed()
    }
}
