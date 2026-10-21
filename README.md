# ComposeOverscroll
Overscroll any scrollable items!

## Preview
### compare with iOS
![overscrollIOS](https://user-images.githubusercontent.com/21119517/206082779-0c98ae17-b54c-4088-bec4-4c2e0cfd5672.gif)

### demo
https://user-images.githubusercontent.com/21119517/205905168-180bc330-85c0-4fbe-a712-8a999e398cb4.mp4

## 使用方式

### 从 GitHub/JitPack 添加依赖

新项目建议使用 `2.0.0`，它面向 Jetpack Compose `1.12.1`。本版本 tag 已发布到 GitHub，JitPack 根项目依赖已通过独立消费者编译验证；无需下载或复制本仓库源码。

在项目的 `settings.gradle` 中添加仓库：

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

在 Android 模块的 `build.gradle` 中添加：

```groovy
dependencies {
    implementation 'com.github.Cormor:ComposeOverscroll:2.0.0'
}
```

请使用上面的仓库聚合坐标；模块坐标 `com.github.Cormor.ComposeOverscroll:overscroll_core:2.0.0` 在本次核查时无法从 JitPack 解析。

旧项目如果使用 Compose `1.5.x`–`1.12.x`，可选择兼容版 `1.3.1`；各次版本已测试的具体稳定版见报告，其他补丁版未逐个测试：

```groovy
dependencies {
    implementation 'com.github.Cormor:ComposeOverscroll:1.3.1'
}
```

这是 JitPack 根项目坐标，远端 AAR 已通过独立消费者编译和 API 34 模拟器运行检查；JitPack 未提供这个 tag 的 `overscroll_core` 模块坐标。`1.3.1` 使用旧版 Android 构建工具发布，Compose 依赖从 `1.5.0` 起经过核查。不要使用 `1.3.0` 的 JitPack 包：它虽然能解析，但 AAR 中没有库类。

### Compose 兼容范围

- `2.0.0` 使用 Compose `1.12.1` 编译，要求 `compileSdk 37`、AGP `9.2.0` 或更高版本、Gradle `9.4.1` 或更高版本，以及 JDK 17 或更高版本。
- `2.0.0` 的库最低支持 Android API 23；Android 5.0/5.1（API 21–22）不兼容。
- `1.3.1` 在 Compose `1.5.0`、`1.5.4`、`1.6.8`、`1.7.8`、`1.8.3`、`1.9.5`、`1.10.6`、`1.11.4` 和 `1.12.1` 上通过编译和 API 34 模拟器运行；此次测试的 `1.3.3`、`1.4.3` 会因缺少 Compose Runtime API 而崩溃。
- `1.3.1` AAR 声明的 `minSdk` 为 21；本次模拟器运行核查使用 API 34，没有覆盖 API 21–33 的设备。
- 选择版本时，Compose `1.5.0`–`1.12.1` 的旧项目用 `1.3.1`；新项目或采用 Compose `1.12.1` 新版构建工具链的项目用 `2.0.0`。

完整核查和验证范围见[Compose 兼容性报告](docs/compose-compatibility-2026-10-21.md)。

## How to use

### for column :
```
Column(Modifier
    .fillMaxSize()
    .overScrollVertical() // invoke before the scrollable Modifier
    //.overScrollHorizontal() // or this
    .verticalScroll(state = scrollState, flingBehavior = rememberOverscrollFlingBehavior { scrollState }) // must use rememberOverscrollFlingBehavior
    //.horizontalScroll(state = scrollState, flingBehavior = rememberOverscrollFlingBehavior { scrollState }) // must use rememberOverscrollFlingBehavior
) {
    // ...
}
```
### for scrollable Composable like lazyColumn :
```
  val scrollState = rememberLazyListState()
        
        LazyColumn(Modifier
            .fillMaxWidth()
            .overScrollVertical(), // * u should do it
            state = scrollState, // * u should do it 
            flingBehavior = rememberOverscrollFlingBehavior { scrollState } // required for this library's overscroll fling behavior
        ) {
            // ...
        }
```
### U can call it nested! pls see the demo!

### Google Issue Tracker

用户提供的问题 `276682419` 截图显示 **Won't fix (Obsolete)**。2026-10-21，我们在 API 34 模拟器上运行了 AndroidX `androidx-main` 的整个 `NestedScrollModifierTest`：34 项全通过，包括检查 fling 方向的 `nestedScroll_movingTarget_velocityShouldRespectSign`。因此本仓把“当前 AndroidX 主线的 nested-scroll 方向反转回归”标记为已修复。该测试没有覆盖精确的小幅快速手势或速度大小，Google 工单状态也未更改。问题 `261895103` 仍无法核实。证据和限制见[兼容性报告](docs/compose-compatibility-2026-10-21.md)。

https://issuetracker.google.com/issues/276682419
https://issuetracker.google.com/issues/261895103
