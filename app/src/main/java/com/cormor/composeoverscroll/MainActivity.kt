package com.cormor.composeoverscroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cormor.overscroll.core.overScrollHorizontal
import com.cormor.overscroll.core.overScrollVertical
import com.cormor.overscroll.core.parabolaScrollEasing
import com.cormor.overscroll.core.rememberOverscrollFlingBehavior

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoPage()
            // DemoPage2()
            // DemoPage3()
        }
    }
}

@Composable fun DemoPage() {
    // overscrollVertical 需放在scroll相关Modifier前面
    // 注意，可滚动的Compose中嵌套可滚动项，需要设置高度/量算规则以帮助量算，否则量算时遇到无限高度的可滚动项目会崩溃
    var springStiff by remember { mutableFloatStateOf(Spring.StiffnessLow) }
    var springDamp by remember { mutableFloatStateOf(Spring.DampingRatioLowBouncy) }
    var dragP by remember { mutableFloatStateOf(50f) }
    // 整体可滚动+overscroll
    Column(Modifier.fillMaxSize()
        .overScrollVertical(true, { x1, x2 -> parabolaScrollEasing(x1, x2, dragP) }, springStiff = springStiff, springDamp = springDamp)
        .padding(32.dp, 0.dp)
    ) {
        Column(Modifier.height(100.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("springStiff=$springStiff")
            Slider(springStiff, { springStiff = it }, Modifier.fillMaxWidth(), valueRange = 1f..500f)
        }
        Column(Modifier.height(100.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("springDamp=$springDamp")
            Slider(springDamp, { springDamp = it }, Modifier.fillMaxWidth(), valueRange = Float.MIN_VALUE..1f)
        }
        Column(Modifier.height(100.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("drag P=$dragP")
            Slider(dragP, { dragP = it }, Modifier.fillMaxWidth(), valueRange = 0.1f..500f)
        }
        val scrollState1 = rememberLazyListState()
        // 普通的lazyColumn
        LazyColumn(Modifier.fillMaxWidth().weight(1f).background(Color.Cyan)
            .overScrollVertical(false, { x1, x2 -> parabolaScrollEasing(x1, x2, dragP) }, springStiff = springStiff, springDamp = springDamp),
            state = scrollState1,
            flingBehavior = rememberOverscrollFlingBehavior { scrollState1 }
        ) {
            items(15, { "${it}_1" }, { 1 }) {
                Content(it)
            }
        }
        val scrollState2 = rememberLazyListState()
        // 普通的lazyColumn
        LazyColumn(Modifier.fillMaxWidth().weight(5f).background(Color.LightGray)
            .overScrollVertical(true, { x1, x2 -> parabolaScrollEasing(x1, x2, dragP) }, springStiff = springStiff, springDamp = springDamp),
            state = scrollState2, flingBehavior = rememberOverscrollFlingBehavior { scrollState2 }) {
            items(50, { "${it}_2" }, { 1 }) {
                Content(it)
            }
            item {
                val scrollStateInner = rememberLazyListState()
                LazyColumn(Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Green)
                    .overScrollVertical(), // * u should do it
                    state = scrollStateInner, // * u should do it
                    flingBehavior = rememberOverscrollFlingBehavior { scrollStateInner } // * u should do it
                ) {
                    items(150, { "${it}_1" }, { 1 }) {
                        Content(it)
                    }
                }
            }
            items(50, { "${it}_1" }, { 1 }) {
                Content(it)
            }
        }
    }
    FPSMonitor()
}

@Composable fun Content(index: Int) {
    Text("Item $index")
}

@Composable fun DemoPage2() {
    Column(Modifier.fillMaxSize()
        .overScrollVertical() // 让不可滚动的column也能响应内部组件的嵌套滚动效果
        .padding(32.dp, 0.dp)
    ) {
        val scrollState = rememberLazyListState()

        LazyColumn(Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(Color.Cyan)
            .overScrollVertical(), // * u should do it
            state = scrollState, // * u should do it
            flingBehavior = rememberOverscrollFlingBehavior { scrollState } // * u should do it
        ) {
            items(150, { "${it}_1" }, { 1 }) {
                Content(it)
            }
        }
    }
    FPSMonitor()
}

@Composable
fun DemoPage3() {
    val scrollState = rememberScrollState()
    val scrollStateHorizontal = rememberScrollState()

    Column(Modifier
        .fillMaxSize()
        // .overScrollVertical() // invoke before the scrollable Modifier
        .overScrollHorizontal() // invoke before the scrollable Modifier
        // .verticalScroll(state = scrollState, flingBehavior = rememberOverscrollFlingBehavior { scrollState }) // must use rememberOverscrollFlingBehavior
        .horizontalScroll(state = scrollStateHorizontal, flingBehavior = rememberOverscrollFlingBehavior { scrollStateHorizontal }) // must use rememberOverscrollFlingBehavior
    ) {
        repeat(150) {
            Content(it)
        }
    }
}