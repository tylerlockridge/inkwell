package com.obsidiancapture

import com.obsidiancapture.auth.BiometricAuthManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BiometricAuthManagerTest {

    @Test
    fun `capability enum has all expected values`() {
        val values = BiometricAuthManager.Capability.entries
        assertEquals(4, values.size)
    }

    @Test
    fun `capability AVAILABLE exists`() {
        assertNotNull(BiometricAuthManager.Capability.AVAILABLE)
    }

    @Test
    fun `capability NO_HARDWARE exists`() {
        assertNotNull(BiometricAuthManager.Capability.NO_HARDWARE)
    }

    @Test
    fun `capability NOT_ENROLLED exists`() {
        assertNotNull(BiometricAuthManager.Capability.NOT_ENROLLED)
    }

    @Test
    fun `capability UNAVAILABLE exists`() {
        assertNotNull(BiometricAuthManager.Capability.UNAVAILABLE)
    }

    @Test
    fun `manager can be instantiated`() {
        val manager = BiometricAuthManager()
        assertNotNull(manager)
    }
}
