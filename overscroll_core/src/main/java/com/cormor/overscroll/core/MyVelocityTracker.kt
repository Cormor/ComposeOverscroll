package com.cormor.overscroll.core

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.VelocityTracker1D
import androidx.compose.ui.unit.Velocity

class MyVelocityTracker {
    val xVelocityTracker = VelocityTracker1D(true)
    val yVelocityTracker = VelocityTracker1D(true)


    internal var currentPointerPositionAccumulator = Offset.Zero
    
    fun addPosition(timeMillis: Long, position: Offset) {
        xVelocityTracker.addDataPoint(timeMillis, position.x)
        yVelocityTracker.addDataPoint(timeMillis, position.y)
    }

    /**
     * Computes the estimated velocity of the pointer at the time of the last provided data point.
     *
     * This can be expensive. Only call this when you need the velocity.
     */
    fun calculateVelocity(): Velocity {
        return Velocity(xVelocityTracker.calculateVelocity(), yVelocityTracker.calculateVelocity())
    }

    /**
     * Clears the tracked positions added by [addPosition].
     */
    fun resetTracking() {
        xVelocityTracker.resetTracking()
        yVelocityTracker.resetTracking()
    }

    fun addPointerInputChange(event: PointerInputChange) {

        // Register down event as the starting point for the accumulator
        if (event.changedToDownIgnoreConsumed()) {
            currentPointerPositionAccumulator = event.position
            resetTracking()
        }

        // To calculate delta, for each step we want to  do currentPosition - previousPosition.
        // Initially the previous position is the previous position of the current event
        var previousPointerPosition = event.previousPosition
        @OptIn(ExperimentalComposeUiApi::class)
        event.historical.forEach {
            // Historical data happens within event.position and event.previousPosition
            // That means, event.previousPosition < historical data < event.position
            // Initially, the first delta will happen between the previousPosition and
            // the first position in historical delta. For subsequent historical data, the
            // deltas happen between themselves. That's why we need to update previousPointerPosition
            // everytime.
            val historicalDelta = it.position - previousPointerPosition
            previousPointerPosition = it.position

            // Update the current position with the historical delta and add it to the tracker
            currentPointerPositionAccumulator += historicalDelta
            addPosition(it.uptimeMillis, currentPointerPositionAccumulator)
        }

        // For the last position in the event
        // If there's historical data, the delta is event.position - lastHistoricalPoint
        // If there's no historical data, the delta is event.position - event.previousPosition
        val delta = event.position - previousPointerPosition
        currentPointerPositionAccumulator += delta
        addPosition(event.uptimeMillis, currentPointerPositionAccumulator)
    }
}
