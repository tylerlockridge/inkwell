package io.inkwell

import android.net.Uri
import io.inkwell.ui.capture.CaptureColor
import io.inkwell.ui.capture.CaptureType
import io.inkwell.ui.capture.CaptureUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for the I13 capture validity rule.
 *
 * Rule: a capture is valid when it contains meaningful user content.
 * - Task/Note/Idea: text OR sourceUrl OR attachments
 * - List: non-blank name AND at least one non-blank item line
 * - Cosmetic metadata alone (pinned/color/tags) is NOT sufficient
 */
@RunWith(RobolectricTestRunner::class)
class CaptureValidationTest {

    // =====================================================================
    // Task/Note/Idea — text content
    // =====================================================================

    @Test
    fun `empty text is invalid`() {
        assertFalse(CaptureUiState(captureType = CaptureType.TASK, unifiedText = "").isValid)
    }

    @Test
    fun `whitespace-only text is invalid`() {
        assertFalse(CaptureUiState(captureType = CaptureType.TASK, unifiedText = "   \n  \t  ").isValid)
    }

    @Test
    fun `non-blank text is valid`() {
        assertTrue(CaptureUiState(captureType = CaptureType.TASK, unifiedText = "Buy milk").isValid)
    }

    @Test
    fun `title-only (single line) is valid`() {
        assertTrue(CaptureUiState(captureType = CaptureType.NOTE, unifiedText = "Meeting notes").isValid)
    }

    @Test
    fun `title plus body is valid`() {
        assertTrue(CaptureUiState(captureType = CaptureType.IDEA, unifiedText = "Title\nBody content").isValid)
    }

    // =====================================================================
    // Task/Note/Idea — sourceUrl content
    // =====================================================================

    @Test
    fun `sourceUrl alone is valid (share-intent URL-only scenario)`() {
        assertTrue(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            sourceUrl = "https://example.com",
        ).isValid)
    }

    @Test
    fun `blank sourceUrl with no text is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.NOTE,
            unifiedText = "",
            sourceUrl = "",
        ).isValid)
    }

    @Test
    fun `whitespace sourceUrl with no text is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            sourceUrl = "   ",
        ).isValid)
    }

    // =====================================================================
    // Task/Note/Idea — attachment content
    // =====================================================================

    @Test
    fun `attachment alone is valid`() {
        assertTrue(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            selectedAttachments = listOf(Uri.parse("content://photo.jpg")),
        ).isValid)
    }

    @Test
    fun `empty attachments with no text is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            selectedAttachments = emptyList(),
        ).isValid)
    }

    // =====================================================================
    // Task/Note/Idea — cosmetic metadata alone is NOT valid
    // =====================================================================

    @Test
    fun `pinned alone is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            pinned = true,
        ).isValid)
    }

    @Test
    fun `color alone is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            color = CaptureColor.BLUE,
        ).isValid)
    }

    @Test
    fun `tags alone are invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            selectedTags = setOf("work", "personal"),
        ).isValid)
    }

    @Test
    fun `priority alone is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            priority = "high",
        ).isValid)
    }

    @Test
    fun `full cosmetic metadata without content is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            pinned = true,
            color = CaptureColor.RED,
            selectedTags = setOf("work"),
            priority = "high",
            date = "2026-03-28",
        ).isValid)
    }

    // =====================================================================
    // Task/Note/Idea — combined content sources
    // =====================================================================

    @Test
    fun `text plus sourceUrl is valid`() {
        assertTrue(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "Check this article",
            sourceUrl = "https://example.com",
        ).isValid)
    }

    @Test
    fun `text plus attachment is valid`() {
        assertTrue(CaptureUiState(
            captureType = CaptureType.NOTE,
            unifiedText = "Photo notes",
            selectedAttachments = listOf(Uri.parse("content://photo.jpg")),
        ).isValid)
    }

    // =====================================================================
    // Task/Note/Idea — all capture types behave the same
    // =====================================================================

    @Test
    fun `NOTE type follows same rule as TASK`() {
        assertFalse(CaptureUiState(captureType = CaptureType.NOTE, unifiedText = "").isValid)
        assertTrue(CaptureUiState(captureType = CaptureType.NOTE, unifiedText = "Note").isValid)
    }

    @Test
    fun `IDEA type follows same rule as TASK`() {
        assertFalse(CaptureUiState(captureType = CaptureType.IDEA, unifiedText = "").isValid)
        assertTrue(CaptureUiState(captureType = CaptureType.IDEA, unifiedText = "Idea").isValid)
    }

    // =====================================================================
    // List — name + items
    // =====================================================================

    @Test
    fun `list with name and items is valid`() {
        assertTrue(CaptureUiState(
            captureType = CaptureType.LIST,
            listName = "Groceries",
            listItems = "Milk\nEggs",
        ).isValid)
    }

    @Test
    fun `list with blank name is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.LIST,
            listName = "",
            listItems = "Milk\nEggs",
        ).isValid)
    }

    @Test
    fun `list with whitespace name is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.LIST,
            listName = "   ",
            listItems = "Milk",
        ).isValid)
    }

    @Test
    fun `list with blank items is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.LIST,
            listName = "Groceries",
            listItems = "",
        ).isValid)
    }

    @Test
    fun `list with whitespace-only items is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.LIST,
            listName = "Groceries",
            listItems = "   \n   \n   ",
        ).isValid)
    }

    @Test
    fun `list with newlines-only items is invalid`() {
        assertFalse(CaptureUiState(
            captureType = CaptureType.LIST,
            listName = "Groceries",
            listItems = "\n\n\n",
        ).isValid)
    }

    @Test
    fun `list with one real item among blanks is valid`() {
        assertTrue(CaptureUiState(
            captureType = CaptureType.LIST,
            listName = "Groceries",
            listItems = "\n\nMilk\n\n",
        ).isValid)
    }

    @Test
    fun `list ignores sourceUrl and attachments for validity`() {
        // List validity is name + items, not text/url/attachments
        assertFalse(CaptureUiState(
            captureType = CaptureType.LIST,
            listName = "",
            listItems = "",
            sourceUrl = "https://example.com",
            selectedAttachments = listOf(Uri.parse("content://photo.jpg")),
        ).isValid)
    }

    // =====================================================================
    // Share-intent edge case
    // =====================================================================

    @Test
    fun `share-intent URL-only with no title results in sourceUrl-only valid capture`() {
        // Simulates: Chrome share with no EXTRA_SUBJECT, text was just a URL
        // After ShareIntentParser: title=null, text=null, sourceUrl="https://..."
        // After CaptureScreen LaunchedEffect: unifiedText="" (no title/text to prefill)
        // sourceUrl set via onSourceUrlChange
        assertTrue(CaptureUiState(
            captureType = CaptureType.TASK,
            unifiedText = "",
            sourceUrl = "https://example.com/article",
            shared = true,
        ).isValid)
    }
}
