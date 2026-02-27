package com.obsidiancapture.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/**
 * Shared animation specs for the Capture app.
 * Screens and components import these to keep transitions consistent.
 */
object CaptureAnimations {

    // -- Duration constants --
    const val QUICK_MS = 150
    const val STANDARD_MS = 250
    const val EMPHASIZED_MS = 350

    // -- Screen transitions (for NavHost) --

    /** Slide-in from the right for forward navigation. */
    fun screenEnter(): EnterTransition =
        slideInHorizontally(
            animationSpec = tween(STANDARD_MS),
            initialOffsetX = { fullWidth -> fullWidth / 4 },
        ) + fadeIn(animationSpec = tween(STANDARD_MS))

    /** Slide-out to the left when navigating forward. */
    fun screenExit(): ExitTransition =
        slideOutHorizontally(
            animationSpec = tween(STANDARD_MS),
            targetOffsetX = { fullWidth -> -fullWidth / 4 },
        ) + fadeOut(animationSpec = tween(QUICK_MS))

    /** Slide-in from the left when navigating back. */
    fun screenPopEnter(): EnterTransition =
        slideInHorizontally(
            animationSpec = tween(STANDARD_MS),
            initialOffsetX = { fullWidth -> -fullWidth / 4 },
        ) + fadeIn(animationSpec = tween(STANDARD_MS))

    /** Slide-out to the right when navigating back. */
    fun screenPopExit(): ExitTransition =
        slideOutHorizontally(
            animationSpec = tween(STANDARD_MS),
            targetOffsetX = { fullWidth -> fullWidth / 4 },
        ) + fadeOut(animationSpec = tween(QUICK_MS))

    // -- Chip / tag scale animation --

    /** Scale-in for a chip appearing (e.g., tag chip selected). */
    fun chipEnter(): EnterTransition =
        scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            initialScale = 0.8f,
        ) + fadeIn(animationSpec = tween(QUICK_MS))

    /** Scale-out for a chip disappearing (e.g., tag chip deselected). */
    fun chipExit(): ExitTransition =
        scaleOut(
            animationSpec = tween(QUICK_MS),
            targetScale = 0.8f,
        ) + fadeOut(animationSpec = tween(QUICK_MS))

    // -- FAB press animation --

    /** Scale spec for FAB press-and-release. Use with `Modifier.graphicsLayer { scaleX = scale; scaleY = scale }`. */
    val fabPressScale = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    const val FAB_PRESSED_SCALE = 0.92f
    const val FAB_DEFAULT_SCALE = 1.0f

    // -- Content swap (e.g., panel switch) --

    /** Crossfade for swapping panel content in the smart toolbar. */
    fun panelSwap(): ContentTransform =
        fadeIn(animationSpec = tween(QUICK_MS)) togetherWith
            fadeOut(animationSpec = tween(QUICK_MS))
}
