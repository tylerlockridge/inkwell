package com.obsidiancapture

import com.obsidiancapture.data.remote.dto.CaptureResponse
import com.obsidiancapture.data.repository.CaptureResult
import com.obsidiancapture.data.repository.SyncResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for repository result types and their usage patterns.
 */
class CaptureResultTest {

    @Test
    fun `CaptureResult Online wraps server response`() {
        val response = CaptureResponse(path = "inbox/note.md", uid = "uid_abc")
        val result = CaptureResult.Online(response)
        assertTrue(result is CaptureResult.Online)
        assertEquals("uid_abc", result.response.uid)
    }

    @Test
    fun `CaptureResult Offline wraps client UUID`() {
        val result = CaptureResult.Offline(clientUuid = "uuid-123")
        assertTrue(result is CaptureResult.Offline)
        assertEquals("uuid-123", result.clientUuid)
    }

    @Test
    fun `CaptureResult Error wraps message`() {
        val result = CaptureResult.Error(message = "Network unavailable")
        assertTrue(result is CaptureResult.Error)
        assertEquals("Network unavailable", result.message)
    }

    @Test
    fun `SyncResult Success contains count`() {
        val result = SyncResult.Success(count = 5)
        assertTrue(result is SyncResult.Success)
        assertEquals(5, result.count)
    }

    @Test
    fun `SyncResult NoServer is singleton`() {
        val a = SyncResult.NoServer
        val b = SyncResult.NoServer
        assertTrue(a === b)
    }

    @Test
    fun `SyncResult Error contains message`() {
        val result = SyncResult.Error("Connection refused")
        assertTrue(result is SyncResult.Error)
        assertEquals("Connection refused", result.message)
    }

    @Test
    fun `when expression covers all CaptureResult variants`() {
        val results = listOf(
            CaptureResult.Online(CaptureResponse("p", "u")),
            CaptureResult.Offline("uuid"),
            CaptureResult.Error("err"),
        )

        for (result in results) {
            val label = when (result) {
                is CaptureResult.Online -> "online"
                is CaptureResult.Offline -> "offline"
                is CaptureResult.Error -> "error"
            }
            assertTrue(label.isNotBlank())
        }
    }

    @Test
    fun `when expression covers all SyncResult variants`() {
        val results = listOf(
            SyncResult.Success(0),
            SyncResult.NoServer,
            SyncResult.Error("err"),
        )

        for (result in results) {
            val label = when (result) {
                is SyncResult.Success -> "success"
                is SyncResult.NoServer -> "no_server"
                is SyncResult.Error -> "error"
            }
            assertTrue(label.isNotBlank())
        }
    }
}
