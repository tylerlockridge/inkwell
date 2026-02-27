package com.obsidiancapture.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Wraps BiometricPrompt for coroutine-based auth.
 */
class BiometricAuthManager {

    enum class Capability {
        AVAILABLE,
        NO_HARDWARE,
        NOT_ENROLLED,
        UNAVAILABLE,
    }

    /**
     * Check if biometric authentication is available on the device.
     */
    fun checkCapability(activity: FragmentActivity): Capability {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Capability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Capability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Capability.NOT_ENROLLED
            else -> Capability.UNAVAILABLE
        }
    }

    /**
     * Show the biometric prompt and suspend until result.
     * Returns true if authentication succeeded, false otherwise.
     */
    suspend fun authenticate(activity: FragmentActivity): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (continuation.isActive) continuation.resume(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (continuation.isActive) continuation.resume(false)
                }

                override fun onAuthenticationFailed() {
                    // Called on a single failed attempt; prompt stays open for retry.
                    // Only resume on error (cancel/lockout) or success.
                }
            }

            val prompt = BiometricPrompt(activity, executor, callback)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(com.obsidiancapture.R.string.biometric_prompt_title))
                .setSubtitle(activity.getString(com.obsidiancapture.R.string.biometric_prompt_subtitle))
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
                .build()

            prompt.authenticate(promptInfo)

            continuation.invokeOnCancellation {
                prompt.cancelAuthentication()
            }
        }
    }
}
