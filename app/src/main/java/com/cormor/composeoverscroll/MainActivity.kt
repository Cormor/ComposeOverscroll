package com.cormor.composeoverscroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cormor.overscroll.core.overScrollVertical

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoPage()
        }
    }
}

@Composable fun DemoPage() {
    // overscrollVertical 需放在scroll相关Modifier前面
    // 注意，可滚动的Compose中嵌套可滚动项，需要设置高度/量算规则以帮助量算，否则量算时遇到无限高度的可滚动项目会崩溃
    var springStiff by remember { mutableStateOf(300f) }
    var springDamp by remember { mutableStateOf(1f) }
    // 整体可滚动+overscroll
    Column(Modifier.fillMaxSize()
        .overScrollVertical(springStiff = springStiff, springDamp = springDamp)
    ) {
        Column(Modifier.height(100.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("springStiff=$springStiff")
            Slider(springStiff, { springStiff = it }, Modifier.fillMaxWidth(), valueRange = 1f..1000f)
        }
        Column(Modifier.height(100.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("springDamp=$springDamp")
            Slider(springDamp, { springDamp = it }, Modifier.fillMaxWidth(), valueRange = Float.MIN_VALUE..1f)
        }
        // 普通的lazyColumn
        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            items(15, { "${it}_1" }, { 1 }) {
                Content(it)
            }
        }
        // 普通的lazyColumn
        LazyColumn(Modifier.fillMaxWidth().weight(5f)) {
            items(20, { "${it}_2" }, { 1 }) {
                Content(it)
            }
            item(contentType = "inner nested") {
                // 该LazyColumn nestedScrollToParent = false
                LazyColumn(Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .overScrollVertical(false, springStiff = springStiff, springDamp = springDamp)
                    .background(Color.Yellow)
                ) {
                    items(15, { "${it}_3-" }, { 1 }) {
                        Content(it)
                    }
                    item(contentType = "inner inner nested Item") {
                        // 多重嵌套
                        LazyColumn(Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .overScrollVertical(false, springStiff = springStiff, springDamp = springDamp)
                            .background(Color.Green)
                        ) {
                            items(25, { "${it}_3" }, { 1 }) {
                                Content(it)
                            }
                        }
                    }
                    items(15, { "${it}_3+" }, { 1 }) {
                        Content(it)
                    }
                }
            }
            items(50, { "${it}_4" }, { 1 }) {
                Content(it)
            }
        }
    }
    FPSMonitor()
}

@Composable fun Content(index: Int) {
    Text("Item $index")
}
