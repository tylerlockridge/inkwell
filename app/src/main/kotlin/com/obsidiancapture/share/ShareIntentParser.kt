package com.obsidiancapture.share

import android.content.Intent

/**
 * Parses incoming share intents (ACTION_SEND) to extract shared text content.
 */
object ShareIntentParser {

    data class ShareData(
        val title: String?,
        val text: String?,
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

        return ShareData(title = title, text = text)
    }
}
