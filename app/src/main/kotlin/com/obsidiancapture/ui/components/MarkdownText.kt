package com.obsidiancapture.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.obsidiancapture.util.MarkdownParser

/**
 * Composable that renders markdown text with M3 typography.
 * Supports clickable links via LinkAnnotation (Compose 1.7+).
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val annotatedString = MarkdownParser.parse(text)
    val style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
    )
}
