package com.cormor.overscroll.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * OverScroll effect for scrollable Composable .
 *
 * You should call it before Modifiers with similar semantics such as [Modifier.scrollable], so that nested scrolling can work normally
 * @Author: cormor
 * @Email: cangtiansuo@gmail.com
 * @param nestedScrollToParent 是否将嵌套滚动事件分发给parent
 * @param scrollEasing 传入值分别是当前已有的overscrollOffset和新的来自手势的offset，修改它配合[springStiff]以定制滑动阻尼效果。当前默认easing来自iOS，可以不修改！
 * @param springStiff overscroll的springStiff，为了更好的用户体验，新值不建议高于[Spring.StiffnessMediumLow]
 * @param springDamp overscroll的springDamp，一般不需要设置
 */
fun Modifier.overScrollVertical(
    nestedScrollToParent: Boolean = true,
    scrollEasing: (currentOverscrollOffset: Float, newScrollOffset: Float) -> Float =
        { currentOffset, newOffset ->
            val p = 50f
            val ratio = (p / (sqrt(p * abs(currentOffset + newOffset / 2).coerceAtLeast(Float.MIN_VALUE)))).coerceIn(Float.MIN_VALUE, 1f)
            currentOffset + newOffset * ratio
        },
    springStiff: Float = 300f,
    springDamp: Float = Spring.DampingRatioNoBouncy,
): Modifier = composed {
    val dispatcher = remember { NestedScrollDispatcher() }
    val overscrollOffset = remember { Animatable(0f) }
    
    val nestedConnection = remember(nestedScrollToParent, springStiff, springDamp) {
        object : NestedScrollConnection {
            lateinit var lastFlingAnimator: Animatable<Float, AnimationVector1D>

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (this::lastFlingAnimator.isInitialized) {
                    dispatcher.coroutineScope.launch {
                        lastFlingAnimator.stop()
                    }
                }
                val parentConsume = when {
                    nestedScrollToParent -> dispatcher.dispatchPreScroll(available, source)
                    else                 -> Offset.Zero
                }
                val realAvailable = available - parentConsume

                val isSameDirection = sign(realAvailable.y) == sign(overscrollOffset.value)
                return if (abs(overscrollOffset.value) > 0.5 && !isSameDirection) {
                    // sign changed, coerce to start scrolling and exit
                    if (sign(overscrollOffset.value) != sign(overscrollOffset.value + realAvailable.y)) {
                        dispatcher.coroutineScope.launch { overscrollOffset.snapTo(0f) }
                        Offset(x = 0f, y = available.y)
                    } else {
                        dispatcher.coroutineScope.launch {
                            overscrollOffset.snapTo(overscrollOffset.value + realAvailable.y)
                        }
                        Offset(x = 0f, y = available.y)
                    }
                } else {
                    parentConsume
                }
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val realAvailable = when {
                    nestedScrollToParent -> available - dispatcher.dispatchPostScroll(consumed, available, source)
                    else                 -> available
                }
                dispatcher.coroutineScope.launch {
                    when (source) {
                        NestedScrollSource.Fling -> overscrollOffset.snapTo(overscrollOffset.value + realAvailable.y)
                        else                     -> overscrollOffset.snapTo(scrollEasing(overscrollOffset.value, realAvailable.y))
                    }
                }
                return Offset(x = 0f, y = available.y)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return when {
                    nestedScrollToParent -> dispatcher.dispatchPreFling(available)
                    else                 -> Velocity.Zero
                }
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val realAvailable = when {
                    nestedScrollToParent -> available - dispatcher.dispatchPostFling(consumed, available)
                    else                 -> available
                }
                lastFlingAnimator = Animatable(overscrollOffset.value)
                val leftVelocity = lastFlingAnimator.animateTo(0f, spring(springDamp, springStiff, 0.5f), realAvailable.y) {
                    dispatcher.coroutineScope.launch {
                        overscrollOffset.snapTo(scrollEasing(overscrollOffset.value, value - overscrollOffset.value))
                    }
                }.endState.velocity
                return available.copy(y = available.y - leftVelocity)
            }
        }
    }
    this
        .clipToBounds()
        .offset { IntOffset(0, overscrollOffset.value.roundToInt()) }
        .nestedScroll(nestedConnection, dispatcher)
}
