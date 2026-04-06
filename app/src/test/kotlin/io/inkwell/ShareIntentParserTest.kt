package io.inkwell

import io.inkwell.share.ShareIntentParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for ShareIntentParser including I11 URL extraction and body cleaning.
 * Note: Intent-based tests require Android framework (instrumented tests).
 * These tests validate parse(null), ShareData construction, and the internal
 * extractUrl / cleanBody helpers.
 */
class ShareIntentParserTest {

    // --- parse(null) ---

    @Test
    fun `returns null for null intent`() {
        assertNull(ShareIntentParser.parse(null))
    }

    // --- ShareData construction ---

    @Test
    fun `ShareData holds title text and sourceUrl`() {
        val data = ShareIntentParser.ShareData(title = "Title", text = "Body", sourceUrl = "https://example.com")
        assertEquals("Title", data.title)
        assertEquals("Body", data.text)
        assertEquals("https://example.com", data.sourceUrl)
    }

    @Test
    fun `ShareData sourceUrl defaults to null`() {
        val data = ShareIntentParser.ShareData(title = "T", text = "B")
        assertNull(data.sourceUrl)
    }

    @Test
    fun `ShareData equality works`() {
        val a = ShareIntentParser.ShareData(title = "T", text = "B", sourceUrl = "https://x.com")
        val b = ShareIntentParser.ShareData(title = "T", text = "B", sourceUrl = "https://x.com")
        assertEquals(a, b)
    }

    @Test
    fun `ShareData copy works`() {
        val original = ShareIntentParser.ShareData(title = "Original", text = "Body")
        val copied = original.copy(sourceUrl = "https://example.com")
        assertEquals("https://example.com", copied.sourceUrl)
        assertEquals("Body", copied.text)
    }

    // --- extractUrl ---

    @Test
    fun `extractUrl finds https URL`() {
        assertEquals(
            "https://example.com/article",
            ShareIntentParser.extractUrl("Check this out: https://example.com/article"),
        )
    }

    @Test
    fun `extractUrl finds http URL`() {
        assertEquals(
            "http://example.com",
            ShareIntentParser.extractUrl("Visit http://example.com for more"),
        )
    }

    @Test
    fun `extractUrl returns first URL when multiple present`() {
        assertEquals(
            "https://first.com",
            ShareIntentParser.extractUrl("See https://first.com and https://second.com"),
        )
    }

    @Test
    fun `extractUrl returns null for no URL`() {
        assertNull(ShareIntentParser.extractUrl("Just some text without links"))
    }

    @Test
    fun `extractUrl returns null for empty string`() {
        assertNull(ShareIntentParser.extractUrl(""))
    }

    @Test
    fun `extractUrl handles URL with query params`() {
        assertEquals(
            "https://example.com/search?q=test&page=1",
            ShareIntentParser.extractUrl("https://example.com/search?q=test&page=1"),
        )
    }

    @Test
    fun `extractUrl handles URL with fragment`() {
        assertEquals(
            "https://example.com/page#section",
            ShareIntentParser.extractUrl("https://example.com/page#section"),
        )
    }

    @Test
    fun `extractUrl handles URL with path and trailing slash`() {
        assertEquals(
            "https://example.com/path/to/resource/",
            ShareIntentParser.extractUrl("https://example.com/path/to/resource/"),
        )
    }

    @Test
    fun `extractUrl ignores mailto links`() {
        assertNull(ShareIntentParser.extractUrl("Contact mailto:user@example.com"))
    }

    @Test
    fun `extractUrl ignores ftp links`() {
        assertNull(ShareIntentParser.extractUrl("Download from ftp://files.example.com"))
    }

    @Test
    fun `extractUrl stops at whitespace`() {
        assertEquals(
            "https://example.com/article",
            ShareIntentParser.extractUrl("https://example.com/article is great"),
        )
    }

    // --- cleanBody ---

    @Test
    fun `cleanBody returns null when text is only a URL`() {
        assertNull(ShareIntentParser.cleanBody("https://example.com", "https://example.com"))
    }

    @Test
    fun `cleanBody returns null when text is URL with whitespace`() {
        assertNull(ShareIntentParser.cleanBody("  https://example.com  ", "https://example.com"))
    }

    @Test
    fun `cleanBody preserves prose with URL in context`() {
        val text = "Check out this article https://example.com/article it's really good"
        val result = ShareIntentParser.cleanBody(text, "https://example.com/article")
        assertEquals(text, result)
    }

    @Test
    fun `cleanBody preserves text when URL is at start`() {
        val text = "https://example.com — amazing resource"
        val result = ShareIntentParser.cleanBody(text, "https://example.com")
        assertEquals(text, result)
    }

    @Test
    fun `cleanBody preserves text when URL is at end`() {
        val text = "Read this: https://example.com"
        val result = ShareIntentParser.cleanBody(text, "https://example.com")
        assertEquals(text, result)
    }

    // --- Integration-style tests on extractUrl + cleanBody ---

    @Test
    fun `Chrome share URL-only scenario`() {
        // Chrome shares: subject = "Page Title", text = "https://example.com/article"
        val text = "https://example.com/article"
        val url = ShareIntentParser.extractUrl(text)
        assertEquals("https://example.com/article", url)
        val cleaned = ShareIntentParser.cleanBody(text, url!!)
        assertNull("Body should be null when text is only URL", cleaned)
    }

    @Test
    fun `Twitter share with prose and link`() {
        val text = "This thread is amazing https://twitter.com/user/status/12345 must read"
        val url = ShareIntentParser.extractUrl(text)
        assertEquals("https://twitter.com/user/status/12345", url)
        val cleaned = ShareIntentParser.cleanBody(text, url!!)
        assertEquals("Prose should be preserved", text, cleaned)
    }

    @Test
    fun `Share with no URL`() {
        val text = "Just a thought I want to capture"
        val url = ShareIntentParser.extractUrl(text)
        assertNull(url)
        // No cleaning needed when there's no URL
    }

    @Test
    fun `Share with multiple URLs preserves all in body`() {
        val text = "Compare https://a.com and https://b.com"
        val url = ShareIntentParser.extractUrl(text)
        assertEquals("https://a.com", url)
        val cleaned = ShareIntentParser.cleanBody(text, url!!)
        assertEquals("Full text with both URLs preserved", text, cleaned)
    }
}
