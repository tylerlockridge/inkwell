package com.obsidiancapture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for SyncWorker result decision logic.
 *
 * Policy: retry only when ALL items failed (systemic issue like network down).
 * Partial success is treated as success — individual item errors are logged
 * and retried on the next scheduled run.
 */
class SyncWorkerResultTest {

    // Mirrors the condition in SyncWorker.doWork():
    //   if (failCount > 0 && successCount == 0 && itemsNotEmpty) -> retry
    //   else -> success
    private fun shouldRetry(successCount: Int, failCount: Int, totalItems: Int): Boolean =
        failCount > 0 && successCount == 0 && totalItems > 0

    @Test
    fun `all items failed triggers retry`() {
        assertTrue(shouldRetry(successCount = 0, failCount = 5, totalItems = 5))
    }

    @Test
    fun `all items succeeded returns success`() {
        assertFalse(shouldRetry(successCount = 5, failCount = 0, totalItems = 5))
    }

    @Test
    fun `partial failure returns success not retry`() {
        // One failure + one success → success (individual retry on next run)
        assertFalse(shouldRetry(successCount = 3, failCount = 2, totalItems = 5))
    }

    @Test
    fun `empty inbox returns success`() {
        assertFalse(shouldRetry(successCount = 0, failCount = 0, totalItems = 0))
    }

    @Test
    fun `single item success returns success`() {
        assertFalse(shouldRetry(successCount = 1, failCount = 0, totalItems = 1))
    }

    @Test
    fun `single item failure triggers retry`() {
        assertTrue(shouldRetry(successCount = 0, failCount = 1, totalItems = 1))
    }

    @Test
    fun `all items skipped returns success`() {
        // Items not stale → staleItems empty → successCount=0, failCount=0, totalItems=N
        // shouldRetry: failCount > 0 is false → returns success
        assertFalse(shouldRetry(successCount = 0, failCount = 0, totalItems = 10))
    }
}
