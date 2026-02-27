package com.obsidiancapture

import com.obsidiancapture.data.repository.SyncResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncResultTest {

    @Test
    fun `success result carries count`() {
        val result = SyncResult.Success(5)
        assertEquals(5, result.count)
    }

    @Test
    fun `success result with zero count`() {
        val result = SyncResult.Success(0)
        assertEquals(0, result.count)
    }

    @Test
    fun `no server is a singleton`() {
        val result = SyncResult.NoServer
        assertTrue(result is SyncResult)
    }

    @Test
    fun `error carries message`() {
        val result = SyncResult.Error("Connection timeout")
        assertEquals("Connection timeout", result.message)
    }

    @Test
    fun `sealed class covers all variants`() {
        val results: List<SyncResult> = listOf(
            SyncResult.Success(1),
            SyncResult.NoServer,
            SyncResult.Error("fail"),
        )
        assertEquals(3, results.size)
        assertTrue(results[0] is SyncResult.Success)
        assertTrue(results[1] is SyncResult.NoServer)
        assertTrue(results[2] is SyncResult.Error)
    }

    @Test
    fun `when expression covers all variants`() {
        fun describe(result: SyncResult): String = when (result) {
            is SyncResult.Success -> "synced ${result.count}"
            is SyncResult.NoServer -> "no server"
            is SyncResult.Error -> "error: ${result.message}"
        }

        assertEquals("synced 3", describe(SyncResult.Success(3)))
        assertEquals("no server", describe(SyncResult.NoServer))
        assertEquals("error: timeout", describe(SyncResult.Error("timeout")))
    }
}
