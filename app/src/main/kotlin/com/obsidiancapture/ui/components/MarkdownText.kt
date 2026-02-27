package com.obsidiancapture.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.obsidiancapture.util.MarkdownParser

/**
 * Composable that renders markdown text with M3 typography.
 * Supports clickable links.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val annotatedString = MarkdownParser.parse(text)
    val style = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = style,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        // Invalid URL or no browser
                    }
                }
        },
    )
}
