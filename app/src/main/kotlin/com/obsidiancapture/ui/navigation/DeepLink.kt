package com.obsidiancapture.ui.navigation

import android.net.Uri

/**
 * Deep link URI constants and parsing utilities.
 * Scheme: obsidiancapture://
 */
object DeepLink {
    const val SCHEME = "obsidiancapture"

    // URI patterns for navDeepLink
    const val CAPTURE_URI = "$SCHEME://capture"
    const val INBOX_URI = "$SCHEME://inbox"
    const val NOTE_DETAIL_URI = "$SCHEME://note/{uid}"
    const val SYSTEM_HEALTH_URI = "$SCHEME://system-health"

    /** Build a deep link URI for a specific note. */
    fun noteUri(uid: String): Uri = Uri.parse("$SCHEME://note/$uid")

    /** Build a deep link URI for the capture screen. */
    fun captureUri(): Uri = Uri.parse(CAPTURE_URI)

    /** Build a deep link URI for the inbox screen. */
    fun inboxUri(): Uri = Uri.parse(INBOX_URI)

    /** Build a deep link URI for system health. */
    fun systemHealthUri(): Uri = Uri.parse(SYSTEM_HEALTH_URI)

    /**
     * Parse a deep link URI to a navigation route.
     * Returns null if the URI is not a valid deep link.
     */
    fun parseToRoute(uri: Uri?): String? {
        if (uri == null || uri.scheme != SCHEME) return null

        val host = uri.host ?: return null
        val pathSegments = uri.pathSegments

        return when (host) {
            "capture" -> Screen.Capture.route
            "inbox" -> Screen.Inbox.route
            "system-health" -> Screen.SYSTEM_HEALTH_ROUTE
            "note" -> {
                val uid = pathSegments.firstOrNull() ?: return null
                if (uid.isBlank()) return null
                Screen.noteDetailRoute(uid)
            }
            else -> null
        }
    }
}
