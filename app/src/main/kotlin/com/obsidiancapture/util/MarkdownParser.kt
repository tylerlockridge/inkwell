package com.obsidiancapture.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Minimal Markdown parser that converts markdown text to Compose AnnotatedString.
 * Supports: **bold**, *italic*, # headers (1-3), - lists, `code`, [links](url), ~~strikethrough~~
 */
object MarkdownParser {

    fun parse(text: String): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.lines()
            lines.forEachIndexed { index, line ->
                parseLine(line)
                if (index < lines.lastIndex) append("\n")
            }
        }
    }

    private fun AnnotatedString.Builder.parseLine(line: String) {
        val trimmed = line.trimStart()

        // Headers
        when {
            trimmed.startsWith("### ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                    parseInline(trimmed.removePrefix("### "))
                }
                return
            }
            trimmed.startsWith("## ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                    parseInline(trimmed.removePrefix("## "))
                }
                return
            }
            trimmed.startsWith("# ") -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                    parseInline(trimmed.removePrefix("# "))
                }
                return
            }
        }

        // Unordered list items
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            append("  \u2022 ")
            parseInline(trimmed.substring(2))
            return
        }

        // Ordered list items
        val orderedMatch = ORDERED_LIST_REGEX.matchAt(trimmed, 0)
        if (orderedMatch != null) {
            append("  ${orderedMatch.groupValues[1]} ")
            parseInline(trimmed.substring(orderedMatch.range.last + 1))
            return
        }

        // Regular line
        parseInline(line)
    }

    private fun AnnotatedString.Builder.parseInline(text: String) {
        var i = 0
        while (i < text.length) {
            when {
                // Code span: `code`
                text[i] == '`' && !text.startsWith("```", i) -> {
                    val end = text.indexOf('`', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append('`')
                        i++
                    }
                }

                // Bold: **text**
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            parseInline(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append("**")
                        i += 2
                    }
                }

                // Strikethrough: ~~text~~
                text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            parseInline(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append("~~")
                        i += 2
                    }
                }

                // Italic: *text*
                text[i] == '*' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            parseInline(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append('*')
                        i++
                    }
                }

                // Link: [text](url)
                text[i] == '[' -> {
                    val closeBracket = text.indexOf(']', i + 1)
                    if (closeBracket != -1 && closeBracket + 1 < text.length && text[closeBracket + 1] == '(') {
                        val closeParen = text.indexOf(')', closeBracket + 2)
                        if (closeParen != -1) {
                            val linkText = text.substring(i + 1, closeBracket)
                            val url = text.substring(closeBracket + 2, closeParen)
                            pushStringAnnotation(tag = "URL", annotation = url)
                            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                append(linkText)
                            }
                            pop()
                            i = closeParen + 1
                        } else {
                            append('[')
                            i++
                        }
                    } else {
                        append('[')
                        i++
                    }
                }

                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }

    private val ORDERED_LIST_REGEX = Regex("""^(\d+\.) """)
}
