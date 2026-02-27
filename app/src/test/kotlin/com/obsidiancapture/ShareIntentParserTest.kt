package com.obsidiancapture

import com.obsidiancapture.share.ShareIntentParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for ShareIntentParser.
 * Note: Intent-based tests require Android framework (instrumented tests).
 * These tests validate parse(null) and ShareData construction.
 */
class ShareIntentParserTest {

    @Test
    fun `returns null for null intent`() {
        assertNull(ShareIntentParser.parse(null))
    }

    @Test
    fun `ShareData holds title and text`() {
        val data = ShareIntentParser.ShareData(title = "Title", text = "Body")
        assertEquals("Title", data.title)
        assertEquals("Body", data.text)
    }

    @Test
    fun `ShareData title can be null`() {
        val data = ShareIntentParser.ShareData(title = null, text = "Body")
        assertNull(data.title)
        assertNotNull(data.text)
    }

    @Test
    fun `ShareData text can be null`() {
        val data = ShareIntentParser.ShareData(title = "Title", text = null)
        assertNotNull(data.title)
        assertNull(data.text)
    }

    @Test
    fun `ShareData equality works`() {
        val a = ShareIntentParser.ShareData(title = "T", text = "B")
        val b = ShareIntentParser.ShareData(title = "T", text = "B")
        assertEquals(a, b)
    }

    @Test
    fun `ShareData copy works`() {
        val original = ShareIntentParser.ShareData(title = "Original", text = "Body")
        val copied = original.copy(title = "Changed")
        assertEquals("Changed", copied.title)
        assertEquals("Body", copied.text)
    }
}
