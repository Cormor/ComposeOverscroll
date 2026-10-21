# Compose 兼容性报告

**核查日期：** 2026-10-21

**仓库：** [`Cormor/ComposeOverscroll`](https://github.com/Cormor/ComposeOverscroll)

**核查版本：** `main`，`55332147ed05597e5f7bb19a1ab0e0315f20d615`（远端 `origin/main` 当前 HEAD）

远端 `main` 最近提交于 2023-11-01；当前最新标签为 `1.3.0`（2023-08-17）。

## 结论

仓库当前配置**不能直接使用最新稳定版 Jetpack Compose 1.12.1**。原因是项目仍固定使用 Compose 1.5.4、AGP 8.1.0、Gradle 8.4 和 `compileSdk 34`；Compose 1.12 起要求 `compileSdk 37`，Compose Foundation 1.12.0 的发行说明注明最低需要 AGP 9.2.0。AGP 9.2.0 又要求 Gradle 9.4.1。

这能确认当前构建配置达不到 Compose 1.12.1 的工具链要求；**不能据此断言 `overscroll_core` 的源码 API 与 1.12.1 不兼容**。本次没有把项目迁移到新工具链，也没有用 Compose 1.12.1 编译该源码。

## 仓库当前配置

| 项目 | 当前值 |
| --- | --- |
| Compose UI / Material 直接依赖；Foundation 解析版本 | 1.5.4 / 1.5.4 |
| Android Gradle Plugin | 8.1.0 |
| Kotlin Gradle Plugin | 1.9.10 |
| Compose Compiler | 1.5.3 |
| Gradle Wrapper | 8.4 |
| `app` / `overscroll_core` 的 `compileSdk` | 34 |
| `app` / `overscroll_core` 的 `minSdk` | 30 / 21 |

版本来自根目录 `build.gradle`、`app/build.gradle`、`overscroll_core/build.gradle` 和 `gradle/wrapper/gradle-wrapper.properties`。Gradle `debugRuntimeClasspath` 解析到 `androidx.compose.foundation:foundation:1.5.4`。官方 Kotlin 对应表确认 Compose Compiler 1.5.3 对应 Kotlin 1.9.10；这与仓库当前固定的编译器和 Kotlin 版本匹配。

## 最新稳定版要求

截至核查日，AndroidX 版本表列出的 Compose UI、Foundation、Animation 和 Material 稳定版为 **1.12.1**；官方 Compose BOM 页面列出的稳定 BOM 为 **2026.09.00**。BOM 可统一相关 Compose 库版本，但不会替项目升级 Android Gradle Plugin、Gradle 或 `compileSdk`。

按官方发行说明，Compose Foundation 1.12.0 起使用 `compileSdk 37`，最低 AGP 为 9.2.0。AGP 9.2.0 支持 API 37，最低 Gradle 为 9.4.1，要求 JDK 17，并使用 Kotlin Gradle Plugin 2.2.10。Kotlin 2.0 及以上项目应使用版本与 Kotlin 匹配的 Compose Compiler Gradle Plugin。

因此，把现有依赖版本从 1.5.4 直接改成 1.12.1 不足以完成升级。AGP 9.x 还涉及 Kotlin 插件和 DSL 的迁移；本项目当前使用 `org.jetbrains.kotlin.android`，升级时应一并按官方迁移说明核对。

## 本次验证

- 使用仓库原始配置执行 `:app:assembleDebug`：**通过**（Windows Android Studio JBR 21.0.10、Gradle Wrapper 8.4、Android SDK 34；53 项任务完成）。构建有 Java 8 source/target 已弃用及 Gradle 9 不兼容提示，但没有阻止本次构建。
- 将 debug APK 安装到 `Pixel_3a_API_34_extension_level_7_x86_64` 模拟器（API 34）：`MainActivity` 成功显示；对灰色 `LazyColumn` 执行纵向滑动后，列表从 `Item 0` 移动至 `Item 19`。启动和滑动后的相关 logcat 窗口没有发现 `FATAL EXCEPTION`。
- 此运行验证只覆盖仓库当前 Compose 1.5.4 配置和 API 34 模拟器，不代表 Compose 1.12.1 或 `minSdk 21` 至 API 33 的设备已验证。本次没有运行自动化测试。

本机 Android SDK 当前安装到 `android-36`，没有 API 37 平台；因此本次没有进行 Compose 1.12.1 构建或运行验证。

## 建议的升级与复核顺序

1. 将 `compileSdk` 更新到 37，并按项目使用的 Compose 1.12.x 官方最低要求升级 AGP 至至少 9.2.0、Gradle 至至少 9.4.1；同时核对 Kotlin/AGP 迁移要求。
2. 按 Kotlin 2.2.10 配置匹配的 Compose Compiler Gradle Plugin，并考虑使用 `androidx.compose:compose-bom:2026.09.00` 管理 Compose 依赖。
3. 重新编译 `app` 和 `overscroll_core`，再在 API 34 和 API 37 环境中验证启动、列表滚动、越界回弹及嵌套滚动；另行验证目标最低 Android 版本。

## 官方资料

- [AndroidX 稳定版本表](https://developer.android.com/jetpack/androidx/versions)
- [Compose BOM 与当前 BOM 版本](https://developer.android.com/develop/ui/compose/bom)
- [Compose Foundation 发行说明](https://developer.android.com/jetpack/androidx/releases/compose-foundation)
- [AGP 9.2.0 发行说明及兼容性表](https://developer.android.com/build/releases/agp-9-2-0-release-notes)
- [Compose Compiler Gradle Plugin 配置](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [AGP 9.0.1 迁移说明](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Compose Compiler 与 Kotlin 兼容表](https://developer.android.com/jetpack/androidx/releases/compose-kotlin)
