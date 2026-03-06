package com.obsidiancapture.ui.navigation

import android.net.Uri

/**
 * Deep link URI constants and parsing utilities.
 *
 * Two schemes are supported:
 * - `obsidiancapture://` — custom scheme (backward compat, kept for existing links)
 * - `https://tyler-capture.duckdns.org/app/` — verified App Links (cannot be intercepted)
 */
object DeepLink {
    const val SCHEME = "obsidiancapture"

    // Custom-scheme URI patterns (backward compat)
    const val CAPTURE_URI = "$SCHEME://capture"
    const val INBOX_URI = "$SCHEME://inbox"
    const val NOTE_DETAIL_URI = "$SCHEME://note/{uid}"
    const val SYSTEM_HEALTH_URI = "$SCHEME://system-health"

    // HTTPS App Link URI patterns (verified — OS routes directly to this app)
    private const val HTTPS_HOST = "tyler-capture.duckdns.org"
    const val HTTPS_CAPTURE_URI = "https://$HTTPS_HOST/app/capture"
    const val HTTPS_INBOX_URI = "https://$HTTPS_HOST/app/inbox"
    const val HTTPS_NOTE_DETAIL_URI = "https://$HTTPS_HOST/app/note/{uid}"
    const val HTTPS_SYSTEM_HEALTH_URI = "https://$HTTPS_HOST/app/system-health"

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
     * Handles both `obsidiancapture://` and `https://tyler-capture.duckdns.org/app/` URIs.
     * Returns null if the URI does not match a known deep link.
     */
    fun parseToRoute(uri: Uri?): String? {
        if (uri == null) return null

        // HTTPS App Links: https://tyler-capture.duckdns.org/app/<destination>
        if (uri.scheme == "https" && uri.host == HTTPS_HOST) {
            val segments = uri.pathSegments // ["app", "<destination>", ...]
            if (segments.firstOrNull() != "app") return null
            return when (segments.getOrNull(1)) {
                "capture" -> Screen.Capture.route
                "inbox" -> Screen.Inbox.route
                "system-health" -> Screen.SYSTEM_HEALTH_ROUTE
                "note" -> {
                    val uid = segments.getOrNull(2) ?: return null
                    if (uid.isBlank()) return null
                    Screen.noteDetailRoute(uid)
                }
                else -> null
            }
        }

        // Custom scheme: obsidiancapture://<destination>
        if (uri.scheme != SCHEME) return null
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
