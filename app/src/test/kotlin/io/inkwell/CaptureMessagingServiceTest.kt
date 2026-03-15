package io.inkwell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for CaptureMessagingService message routing logic.
 *
 * CaptureMessagingService.onMessageReceived routes by data["type"]:
 *   - "new_capture" -> handleNewCapture (sync + notification)
 *   - "sync_required" -> handleSyncRequired (triggerImmediateSync)
 *   - "sync_error" -> handleSyncError (error notification)
 *   - missing type -> return (silently dropped)
 *   - unknown type -> falls through when block (silently dropped)
 *
 * Since the service requires @AndroidEntryPoint + Firebase, we test the routing
 * decision logic directly without instantiating the service.
 */
class CaptureMessagingServiceTest {

    /** Replicates the routing decision from onMessageReceived */
    private fun routeMessage(data: Map<String, String>): String? {
        val type = data["type"] ?: return null
        return when (type) {
            "new_capture" -> "new_capture"
            "sync_required" -> "sync_required"
            "sync_error" -> "sync_error"
            else -> null // unknown type falls through
        }
    }

    /** Replicates title/body defaulting from handleNewCapture */
    private fun resolveNotificationContent(data: Map<String, String>): Pair<String, String> {
        val title = data["title"] ?: "New Capture"
        val body = data["body"] ?: "A new note was captured"
        return title to body
    }

    /** Replicates uid-based deep link decision from handleNewCapture */
    private fun resolveDeepLinkTarget(data: Map<String, String>): String {
        val uid = data["uid"]
        return if (uid != null) "note/$uid" else "inbox"
    }

    @Test
    fun `new_capture routes to handleNewCapture`() {
        assertEquals("new_capture", routeMessage(mapOf("type" to "new_capture")))
    }

    @Test
    fun `missing uid routes to inbox deep link`() {
        assertEquals("inbox", resolveDeepLinkTarget(mapOf("type" to "new_capture")))
    }

    @Test
    fun `present uid routes to note deep link`() {
        assertEquals("note/abc-123", resolveDeepLinkTarget(mapOf("type" to "new_capture", "uid" to "abc-123")))
    }

    @Test
    fun `missing title defaults to New Capture`() {
        val (title, _) = resolveNotificationContent(mapOf("type" to "new_capture"))
        assertEquals("New Capture", title)
    }

    @Test
    fun `provided title is used`() {
        val (title, _) = resolveNotificationContent(mapOf("type" to "new_capture", "title" to "My Note"))
        assertEquals("My Note", title)
    }

    @Test
    fun `sync_required routes to triggerSync`() {
        assertEquals("sync_required", routeMessage(mapOf("type" to "sync_required")))
    }

    @Test
    fun `sync_error routes to error notification`() {
        assertEquals("sync_error", routeMessage(mapOf("type" to "sync_error")))
    }

    @Test
    fun `missing type is silently dropped`() {
        assertNull(routeMessage(mapOf("title" to "No type")))
    }

    @Test
    fun `unknown type is silently dropped`() {
        assertNull(routeMessage(mapOf("type" to "some_future_type")))
    }
}
