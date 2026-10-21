# ComposeOverscroll
Overscroll any scrollable items!

## Preview
### compare with iOS
![overscrollIOS](https://user-images.githubusercontent.com/21119517/206082779-0c98ae17-b54c-4088-bec4-4c2e0cfd5672.gif)

### demo
https://user-images.githubusercontent.com/21119517/205905168-180bc330-85c0-4fbe-a712-8a999e398cb4.mp4

## 使用方式

### 从 GitHub/JitPack 添加依赖

`2.0.0` 面向 Jetpack Compose `1.12.1`。本版本 tag 已发布到 GitHub，JitPack 根项目依赖也已通过独立消费者编译验证；无需下载或复制本仓库源码。

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

本库公开 API 使用 Compose 类型。依赖版本按 `1.12.1` 发布；使用较新的 Compose BOM 时，Gradle 可以按项目的版本约束统一解析。

### Compose 兼容范围

- `2.0.0` 使用 Compose `1.12.1` 编译，要求 `compileSdk 37`、AGP `9.2.0` 或更高版本、Gradle `9.4.1` 或更高版本，以及 JDK 17 或更高版本。
- `2.0.0` 的库最低支持 Android API 23；Android 5.0/5.1（API 21–22）不兼容。
- 历史 `1.x` 代码曾使用 Compose `1.5.4`；旧版本请按对应 tag 的构建配置判断，不能据此推断所有 `1.x` 的兼容范围。
- 低于上述 Android 构建工具版本的项目请使用适配旧 Compose 的 `1.x` 版本；`2.0.0` 没有针对旧 Compose/旧 AGP 的兼容承诺。

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

仓库此前关联的 Google Issue Tracker 问题如下。本次核查无法读取它们的当前状态，因此不能确认 Google 是否已经修复；核查经过见[兼容性报告](docs/compose-compatibility-2026-10-21.md)。

https://issuetracker.google.com/issues/276682419
https://issuetracker.google.com/issues/261895103
