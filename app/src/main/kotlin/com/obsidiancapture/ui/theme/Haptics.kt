package com.obsidiancapture.ui.theme

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * Centralized haptic feedback helper for the Capture app.
 *
 * Usage:
 * ```
 * val haptics = rememberCaptureHaptics()
 * Button(onClick = { haptics.click(); doStuff() }) { ... }
 * ```
 *
 * Respects the `hapticsEnabled` parameter so Settings can toggle haptics globally.
 */
@Stable
class CaptureHaptics(
    private val hapticFeedback: HapticFeedback,
    private val view: android.view.View,
    private val enabled: Boolean,
) {
    /** Light tap — button press, chip toggle, toolbar icon tap. */
    fun click() {
        if (!enabled) return
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /** Confirm action — capture sent, sync complete, connection success. */
    fun confirm() {
        if (!enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /** Reject / error — connection failed, validation error. */
    fun reject() {
        if (!enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /** Heavy — long press trigger, destructive action. */
    fun heavy() {
        if (!enabled) return
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    /** Tick — scroll snap, swipe threshold crossed. */
    fun tick() {
        if (!enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
}

/**
 * Remember a [CaptureHaptics] instance scoped to the current composition.
 *
 * @param enabled Whether haptic feedback is active (wired to Settings toggle).
 */
@Composable
fun rememberCaptureHaptics(enabled: Boolean = true): CaptureHaptics {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    return remember(hapticFeedback, view, enabled) {
        CaptureHaptics(hapticFeedback, view, enabled)
    }
}
