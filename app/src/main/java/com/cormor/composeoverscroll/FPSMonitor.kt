package com.cormor.composeoverscroll

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class FPSMonitor : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        addView(ComposeView(context).apply {
            setContent { FPSMonitor() }
        })
    }
}

private const val fpsUpdDelay = 250L // x??????????????????fps??????
private const val frameCount = 10 // x????????????
private const val greenFPS = 57 // ??????????????????x???????????????

/**
 * ??????????????????
 */
enum class FPSCountMethod {
    /**
     * ????????????x?????????????????????????????????
     */
    FixedInterval,

    /**
     * ??????????????????????????????????????????????????????
     */
    FixedFrameCount,

    /**
     * ????????????????????????????????????????????????
     */
    RealTime
}

@Composable
fun FPSMonitor(modifier: Modifier = Modifier) {
    var displayedFPS by remember { mutableStateOf(0) }
    var textContent by remember { mutableStateOf("FPS:") }
    var fpsCountMethod by remember { mutableStateOf(FPSCountMethod.RealTime) }
    val textColor by remember {
        derivedStateOf {
            // Color.Red.copy(Random(System.currentTimeMillis()).nextFloat())
            when (fpsCountMethod) {
                FPSCountMethod.RealTime        -> {
                    textContent = "FPS????????????:${displayedFPS}"
                    if (displayedFPS > greenFPS) Color.Green else Color.Red
                }

                FPSCountMethod.FixedInterval   -> {
                    textContent = "FPS???${fpsUpdDelay}ms???:${displayedFPS}"
                    if (displayedFPS > greenFPS) Color.Cyan else Color.Magenta
                }

                FPSCountMethod.FixedFrameCount -> {
                    textContent = "FPS??????${frameCount}??????:${displayedFPS}"
                    if (displayedFPS > greenFPS) Color.Cyan else Color.Magenta
                }
            }
        }
    }

    Text(
        text = textContent,
        modifier = modifier.clickable {
            fpsCountMethod = when (fpsCountMethod) {
                FPSCountMethod.FixedInterval   -> FPSCountMethod.FixedFrameCount
                FPSCountMethod.FixedFrameCount -> FPSCountMethod.RealTime
                FPSCountMethod.RealTime        -> FPSCountMethod.FixedInterval
            }
        },
        color = textColor
    )

    LaunchedEffect(Unit) {
        launch(Dispatchers.Default) {
            val fpsArray = FloatArray(frameCount) { 0f }
            var fpsCount = 0 // FixedInterval????????????
            var avgFPS = 0  // FixedFrameCount????????????
            var lastWriteIndex = 0 // RealTime????????????

            val countTask: suspend CoroutineScope.() -> Unit = {
                var lastUpdTime = 0L
                var writeIndex = 0
                while (true) withFrameMillis { frameTimeMillis ->
                    fpsCount++ // FixedInterval????????????

                    fpsArray[writeIndex] = 1000f / (frameTimeMillis - lastUpdTime) //
                    lastUpdTime = frameTimeMillis

                    lastWriteIndex = writeIndex // RealTime????????????

                    writeIndex++
                    if (writeIndex >= fpsArray.size) {
                        avgFPS = fpsArray.average().roundToInt() // FixedFrameCount????????????
                        writeIndex = 0
                    }
                }
            }

            val updDataTask: suspend CoroutineScope.() -> Unit = {
                while (true) {
                    delay(fpsUpdDelay)
                    displayedFPS = when (fpsCountMethod) {
                        FPSCountMethod.FixedInterval   -> (fpsCount * 1000 / fpsUpdDelay.toInt())
                        FPSCountMethod.FixedFrameCount -> avgFPS
                        FPSCountMethod.RealTime        -> fpsArray[lastWriteIndex].roundToInt()
                    }
                    fpsCount = 0
                }
            }

            launch(block = countTask)
            launch(block = updDataTask)
        }
    }
}