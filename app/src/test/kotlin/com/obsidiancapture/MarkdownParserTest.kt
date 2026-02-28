package com.obsidiancapture

import androidx.compose.ui.text.LinkAnnotation
import com.obsidiancapture.util.MarkdownParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun `plain text is unchanged`() {
        val result = MarkdownParser.parse("Hello world")
        assertEquals("Hello world", result.text)
    }

    @Test
    fun `bold text markers are stripped`() {
        val result = MarkdownParser.parse("This is **bold** text")
        assertEquals("This is bold text", result.text)
    }

    @Test
    fun `italic text markers are stripped`() {
        val result = MarkdownParser.parse("This is *italic* text")
        assertEquals("This is italic text", result.text)
    }

    @Test
    fun `inline code markers are stripped`() {
        val result = MarkdownParser.parse("Use `code` here")
        assertEquals("Use code here", result.text)
    }

    @Test
    fun `h1 prefix is stripped`() {
        val result = MarkdownParser.parse("# Heading One")
        assertEquals("Heading One", result.text)
    }

    @Test
    fun `h2 prefix is stripped`() {
        val result = MarkdownParser.parse("## Heading Two")
        assertEquals("Heading Two", result.text)
    }

    @Test
    fun `h3 prefix is stripped`() {
        val result = MarkdownParser.parse("### Heading Three")
        assertEquals("Heading Three", result.text)
    }

    @Test
    fun `unordered list with dash renders bullet`() {
        val result = MarkdownParser.parse("- Item one")
        assertTrue(result.text.contains("\u2022"))
        assertTrue(result.text.contains("Item one"))
    }

    @Test
    fun `unordered list with asterisk renders bullet`() {
        val result = MarkdownParser.parse("* Item one")
        assertTrue(result.text.contains("\u2022"))
        assertTrue(result.text.contains("Item one"))
    }

    @Test
    fun `ordered list keeps number`() {
        val result = MarkdownParser.parse("1. First item")
        assertTrue(result.text.contains("1."))
        assertTrue(result.text.contains("First item"))
    }

    @Test
    fun `link renders text only`() {
        val result = MarkdownParser.parse("Click [here](https://example.com)")
        assertEquals("Click here", result.text)
    }

    @Test
    fun `link has URL annotation`() {
        val result = MarkdownParser.parse("[link](https://example.com)")
        val annotations = result.getLinkAnnotations(0, result.text.length)
        assertEquals(1, annotations.size)
        assertEquals("https://example.com", (annotations[0].item as LinkAnnotation.Url).url)
    }

    @Test
    fun `strikethrough markers are stripped`() {
        val result = MarkdownParser.parse("This is ~~deleted~~ text")
        assertEquals("This is deleted text", result.text)
    }

    @Test
    fun `multiline text preserves newlines`() {
        val result = MarkdownParser.parse("Line 1\nLine 2\nLine 3")
        assertTrue(result.text.contains("\n"))
        assertEquals("Line 1\nLine 2\nLine 3", result.text)
    }

    @Test
    fun `mixed formatting works`() {
        val result = MarkdownParser.parse("# Title\n\nSome **bold** and *italic* text\n- List item")
        assertTrue(result.text.contains("Title"))
        assertTrue(result.text.contains("bold"))
        assertTrue(result.text.contains("italic"))
        assertTrue(result.text.contains("\u2022"))
    }

    @Test
    fun `unclosed bold marker is literal`() {
        val result = MarkdownParser.parse("This is **unclosed")
        assertEquals("This is **unclosed", result.text)
    }

    @Test
    fun `unclosed code marker is literal`() {
        val result = MarkdownParser.parse("This is `unclosed")
        assertEquals("This is `unclosed", result.text)
    }

    @Test
    fun `empty string produces empty result`() {
        val result = MarkdownParser.parse("")
        assertEquals("", result.text)
    }
}
