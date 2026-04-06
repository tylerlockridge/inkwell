package io.inkwell.share

import android.content.Intent

/**
 * Parses incoming share intents (ACTION_SEND) to extract shared text content,
 * source URL, and cleaned body text.
 *
 * ## URL extraction (I11)
 * If the shared text contains an `https://` or `http://` URL, the first match
 * is extracted as [ShareData.sourceUrl]. The body text is then cleaned:
 * - If the text is **only** a URL (plus optional whitespace), the URL is removed
 *   from body to avoid duplication (the title alone serves as body content).
 * - If the text contains **prose around the URL**, the full text is preserved
 *   as body (the URL stays in context).
 */
object ShareIntentParser {

    /** Pattern matching http/https URLs. Stops at whitespace or common delimiters. */
    private val URL_PATTERN = Regex("""https?://[^\s<>"{}|\\^`\[\]]+""")

    data class ShareData(
        val title: String?,
        val text: String?,
        val sourceUrl: String? = null,
    )

    /**
     * Parse an intent for shared text content.
     * Returns null if the intent is not a text share.
     */
    fun parse(intent: Intent?): ShareData? {
        if (intent == null) return null
        if (intent.action != Intent.ACTION_SEND) return null
        if (intent.type?.startsWith("text/") != true) return null

        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)?.trim()
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim()

        // Nothing to share
        if (subject.isNullOrBlank() && text.isNullOrBlank()) return null

        // If subject equals the text, don't duplicate
        val title = if (subject != null && subject != text) subject else null

        // Extract first URL from text
        val sourceUrl = text?.let { extractUrl(it) }

        // Clean body: if text was just a URL, strip it to avoid duplication
        val cleanedText = if (sourceUrl != null && text != null) {
            cleanBody(text, sourceUrl)
        } else {
            text
        }

        return ShareData(
            title = title,
            text = cleanedText,
            sourceUrl = sourceUrl,
        )
    }

    /**
     * Extract the first http/https URL from [text], or null if none found.
     */
    internal fun extractUrl(text: String): String? {
        return URL_PATTERN.find(text)?.value
    }

    /**
     * If removing the URL from [text] leaves only whitespace, return null
     * (the title will serve as body content, and the URL lives in sourceUrl).
     * Otherwise return the full text with URL in context.
     */
    internal fun cleanBody(text: String, url: String): String? {
        val withoutUrl = text.replace(url, "").trim()
        return if (withoutUrl.isBlank()) null else text
    }
}
