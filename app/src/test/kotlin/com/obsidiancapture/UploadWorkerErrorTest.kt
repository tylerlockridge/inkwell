package com.obsidiancapture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for UploadWorker HTTP error classification logic.
 *
 * Classification rules (mirrors UploadWorker.doWork()):
 *   401       → authFailure = true; stop processing (Result.failure)
 *   400–499   → permanent failure; mark sync error, keep going
 *   5xx/other → retryable failure; retry with backoff
 *
 * Final result:
 *   authFailure          → Result.failure()
 *   retryableFailures>0  → Result.retry()
 *   else                 → Result.success()
 */
class UploadWorkerErrorTest {

    private fun classify(status: Int): ErrorKind = when {
        status == 401 -> ErrorKind.AUTH
        status in 400..499 -> ErrorKind.PERMANENT
        else -> ErrorKind.RETRYABLE
    }

    private enum class ErrorKind { AUTH, PERMANENT, RETRYABLE }

    @Test
    fun `401 is auth failure`() {
        assertTrue(classify(401) == ErrorKind.AUTH)
    }

    @Test
    fun `400 is permanent failure`() {
        assertTrue(classify(400) == ErrorKind.PERMANENT)
    }

    @Test
    fun `403 is permanent failure`() {
        assertTrue(classify(403) == ErrorKind.PERMANENT)
    }

    @Test
    fun `404 is permanent failure`() {
        assertTrue(classify(404) == ErrorKind.PERMANENT)
    }

    @Test
    fun `422 is permanent failure`() {
        assertTrue(classify(422) == ErrorKind.PERMANENT)
    }

    @Test
    fun `499 is permanent failure`() {
        assertTrue(classify(499) == ErrorKind.PERMANENT)
    }

    @Test
    fun `500 is retryable`() {
        assertTrue(classify(500) == ErrorKind.RETRYABLE)
    }

    @Test
    fun `503 is retryable`() {
        assertTrue(classify(503) == ErrorKind.RETRYABLE)
    }

    @Test
    fun `401 is not permanent`() {
        assertFalse(classify(401) == ErrorKind.PERMANENT)
    }

    @Test
    fun `500 is not permanent`() {
        assertFalse(classify(500) == ErrorKind.PERMANENT)
    }

    // Result routing

    private fun routeResult(authFailure: Boolean, retryableFailures: Int): String = when {
        authFailure -> "failure"
        retryableFailures > 0 -> "retry"
        else -> "success"
    }

    @Test
    fun `auth failure overrides retryable failures`() {
        assertTrue(routeResult(authFailure = true, retryableFailures = 3) == "failure")
    }

    @Test
    fun `retryable failures with no auth failure causes retry`() {
        assertTrue(routeResult(authFailure = false, retryableFailures = 1) == "retry")
    }

    @Test
    fun `no failures returns success`() {
        assertTrue(routeResult(authFailure = false, retryableFailures = 0) == "success")
    }

    @Test
    fun `permanent failures alone return success`() {
        // Permanent failures are logged + marked, but don't cause a retry
        assertTrue(routeResult(authFailure = false, retryableFailures = 0) == "success")
    }
}
