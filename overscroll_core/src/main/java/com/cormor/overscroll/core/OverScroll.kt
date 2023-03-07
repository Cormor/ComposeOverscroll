package com.cormor.overscroll.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * A parabolic rolling easing curve.
 *
 * When rolling in the same direction, the farther away from 0, the greater the "resistance"; the closer to 0, the smaller the "resistance";
 *
 * No drag effect is applied when the scrolling direction is opposite to the currently existing overscroll offset
 *
 * Note: when [p]=50f, its performance should be consistent with iOS
 * @param currentOffset Offset value currently out of bounds
 * @param newOffset The offset of the new scroll
 * @param p Key parameters for parabolic curve calculation
 */
@Stable
fun parabolaScrollEasing(currentOffset: Float, newOffset: Float, p: Float = 50f): Float {
    val ratio = (p / (sqrt(p * abs(currentOffset + newOffset / 2).coerceAtLeast(Float.MIN_VALUE)))).coerceIn(Float.MIN_VALUE, 1f)
    return if (sign(currentOffset) == sign(newOffset)) {
        currentOffset + newOffset * ratio
    } else {
        currentOffset + newOffset
    }
}

/**
 * OverScroll effect for scrollable Composable .
 *
 * You should call it before Modifiers with similar semantics such as [Modifier.scrollable], so that nested scrolling can work normally
 * @Author: cormor
 * @Email: cangtiansuo@gmail.com
 * @param nestedScrollToParent Whether to dispatch nested scroll events to parent
 * @param scrollEasing The incoming values are the currently existing overscroll Offset
 * and the new offset from the gesture.
 * modify it to cooperate with [springStiff] to customize the sliding damping effect.
 * The current default easing comes from iOS, you don't need to modify it!
 * @param springStiff springStiff for overscroll effect，For better user experience, the new value is not recommended to be higher than[Spring.StiffnessMediumLow]
 * @param springDamp springDamp for overscroll effect，generally do not need to set
 */
fun Modifier.overScrollVertical(
    nestedScrollToParent: Boolean = true,
    scrollEasing: (currentOffset: Float, newOffset: Float) -> Float = @Stable { currentOffset, newOffset -> parabolaScrollEasing(currentOffset, newOffset) },
    springStiff: Float = 300f,
    springDamp: Float = Spring.DampingRatioNoBouncy,
): Modifier = composed {
    val hasChangedParams = remember(nestedScrollToParent, springStiff, springDamp) { System.currentTimeMillis() }

    val dispatcher = remember(hasChangedParams) { NestedScrollDispatcher() }
    var overscrollY by remember(hasChangedParams) { mutableStateOf(0f) }

    val nestedConnection = remember(hasChangedParams) {
        object : NestedScrollConnection {
            /**
             * If the offset is less than this value, we consider the animation to end.
             */
            val visibilityThreshold = 0.5f
            lateinit var lastFlingAnimator: Animatable<Float, AnimationVector1D>

            var offsetY = overscrollY
                set(value) {
                    field = value
                    overscrollY = value
                }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (::lastFlingAnimator.isInitialized && lastFlingAnimator.isRunning) {
                    dispatcher.coroutineScope.launch {
                        lastFlingAnimator.stop()
                    }
                }
                val realAvailable = when {
                    nestedScrollToParent -> available - dispatcher.dispatchPreScroll(available, source)
                    else                 -> available
                }

                val isSameDirection = sign(realAvailable.y) == sign(offsetY)
                if (abs(offsetY) <= visibilityThreshold || isSameDirection) {
                    return available - realAvailable
                }
                val offsetAtLast = offsetY + realAvailable.y
                // sign changed, coerce to start scrolling and exit
                return if (sign(offsetY) != sign(offsetAtLast)) {
                    offsetY = 0f
                    Offset(x = 0f, y = offsetAtLast)
                } else {
                    offsetY = scrollEasing(offsetY, realAvailable.y)
                    Offset(x = 0f, y = available.y)
                }
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val realAvailable = when {
                    nestedScrollToParent -> available - dispatcher.dispatchPostScroll(consumed, available, source)
                    else                 -> available
                }
                when (source) {
                    NestedScrollSource.Fling -> offsetY += realAvailable.y
                    else                     -> offsetY = scrollEasing(offsetY, realAvailable.y)
                }
                return Offset(x = 0f, y = available.y)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (::lastFlingAnimator.isInitialized && lastFlingAnimator.isRunning) {
                    lastFlingAnimator.stop()
                }
                val parentConsumed = when {
                    nestedScrollToParent -> dispatcher.dispatchPreFling(available)
                    else                 -> Velocity.Zero
                }
                val realAvailable = available - parentConsumed
                var leftVelocity = realAvailable.y

                if (abs(offsetY) >= visibilityThreshold && sign(realAvailable.y) != sign(offsetY)) {
                    lastFlingAnimator = Animatable(offsetY).apply {
                        when {
                            leftVelocity < 0 -> updateBounds(lowerBound = 0f)
                            leftVelocity > 0 -> updateBounds(upperBound = 0f)
                        }
                    }
                    leftVelocity = lastFlingAnimator.animateTo(0f, spring(springDamp, springStiff, visibilityThreshold), leftVelocity) {
                        offsetY = scrollEasing(offsetY, value - offsetY)
                    }.endState.velocity
                }
                return Velocity(parentConsumed.x, y = available.y - leftVelocity)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val realAvailable = when {
                    nestedScrollToParent -> available - dispatcher.dispatchPostFling(consumed, available)
                    else                 -> available
                }

                lastFlingAnimator = Animatable(offsetY)
                val leftVelocity: Float = lastFlingAnimator.animateTo(0f, spring(springDamp, springStiff, visibilityThreshold), realAvailable.y) {
                    offsetY = scrollEasing(offsetY, value - offsetY)
                }.endState.velocity
                return Velocity(x = 0f, y = available.y - leftVelocity)
            }
        }
    }

    this
        .clipToBounds()
        .nestedScroll(nestedConnection, dispatcher)
        .graphicsLayer { translationY = overscrollY }
}
