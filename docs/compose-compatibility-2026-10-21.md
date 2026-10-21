# Compose 兼容性报告

**核查日期：** 2026-10-21
**仓库：** [`Cormor/ComposeOverscroll`](https://github.com/Cormor/ComposeOverscroll)
**目标版本：** `2.0.0`（本地提交；尚未推送到 GitHub）

## 结论

本项目已升级到 Jetpack Compose **1.12.1**，并在本机完成库 release 构建、Maven 发布、独立消费者编译和 API 34 模拟器滚动验证。库的 GitHub/JitPack 依赖坐标为：

```text
com.github.Cormor.ComposeOverscroll:overscroll_core:2.0.0
```

本地验证已证明 release AAR 可构建，且消费者不引用本仓库源码也能解析并编译该库。GitHub tag 尚未推送，JitPack 远端构建和实际远端坐标解析还未验证；README 已写明这一前提。

## 最新稳定版和构建要求

截至核查日，AndroidX 官方版本表列出的 Compose UI、Foundation、Animation、Runtime 和 Material 稳定版为 **1.12.1**；稳定版 Compose BOM 为 **2026.09.00**。Compose 1.12.0 起要求 `compileSdk 37` 和 AGP 9；Foundation 发行说明列出最低 AGP 9.2.0。AGP 9.2.0 最低要求 Gradle 9.4.1、JDK 17。AGP 9.4.0 支持 API 37，最低 Gradle 9.6.0、JDK 17。

本项目使用 AGP 9.4.0、Gradle 9.6.0、AGP 内置 Kotlin 所配的 Kotlin Gradle Plugin 2.2.10，以及同版本的 Compose Compiler Gradle Plugin。Compose BOM 固定为 2026.09.00；`compileSdk` 为 37，Java/Kotlin 字节码目标为 11。应用的 `targetSdk 34`、应用 `minSdk 30` 保持原值；库 `minSdk` 更新为 23。本机使用 Android Studio JBR 21.0.10。

## 项目改动

- `app` 和 `overscroll_core` 都启用 Kotlin Compose 编译器插件并使用 `compileSdk 37`；Compose 依赖统一到 BOM 2026.09.00。
- Compose 1.12.1 的内联代码使用 JVM 11 字节码，因此 app 和库的 Java 编译目标调整为 11。
- 将已废弃的 `NestedScrollSource.Drag` 改为官方替代的 `NestedScrollSource.UserInput`。
- 为 app 显式添加 Material Icons Extended 依赖，确保示例里的 Email、Face 图标能在当前依赖下编译。
- 将库 `minSdk` 从 21 调整为 23。Compose 1.12.1 的 `foundation-layout` manifest 要求最低 API 23；保留 21 会导致消费者在 Manifest 合并时失败。
- 关闭库自身 release AAR 的 R8 收缩。首次检查发现原设置会把 release AAR 的 `classes.jar` 收缩为空；修复后发布 AAR 中包含 `OverScrollKt` 类。
- `overscroll_core` 配置 Android release AAR 和 sources JAR 的 Maven publication，模块名为 `overscroll_core`，版本默认 `2.0.0`。Compose animation、foundation、runtime 和 ui 作为 `api` 依赖写入发布元数据。
- 根目录新增 `jitpack.yml`，指定 JitPack 使用 JDK 17；README 添加 JitPack 仓库和 `implementation` 示例。

## 跨版本兼容声明

`2.0.0` 面向 Compose 1.12.1，最低构建工具要求是 `compileSdk 37`、AGP 9.2.0、Gradle 9.4.1 和 JDK 17；库最低 Android API 为 23。项目内构建使用 AGP/Gradle 9.4.0/9.6.0。

仓库原来的 `main` 配置使用 Compose 1.5.4。Compose 1.12.1 与该版本相隔多个版本；本报告不声明 `2.0.0` 能由旧 Compose、旧 AGP 或旧 Gradle 构建，也不声明所有历史 `1.x` 标签彼此兼容。使用 Android API 21–22 的应用不能使用 `2.0.0`；旧工具链项目应按其所用 tag 的配置选择版本，迁移前需满足上述构建工具和 Android 最低版本要求。

## 本次验证记录

- `gradlew wrapper --gradle-version 9.6.0 --distribution-type bin`：**通过**。wrapper 任务成功；随后 `gradlew --version` 显示 Gradle 9.6.0、JBR 21.0.10。
- `:overscroll_core:assembleRelease`：**通过**。使用 Compose 1.12.1 编译、打包 release AAR；编译期间没有 Compose API 错误。
- `:overscroll_core:publishReleasePublicationToMavenLocal`：**通过**。在仓库 `build/` 下的临时 Maven 仓库生成 `overscroll_core-2.0.0.aar`、POM、Gradle Module Metadata 和 sources JAR。POM 中的坐标与 README 示例一致，Compose API 依赖版本均为 1.12.1。
- 独立消费者 APK：**通过**。临时 Android app 只通过本地 Maven 仓库使用 `com.github.Cormor.ComposeOverscroll:overscroll_core:2.0.0`，没有引用 `:overscroll_core` project dependency 或本仓库源码；消费者代码调用了 `Modifier.overScrollVertical()`。首次用 `minSdk 21` 打包时，Manifest 合并报告 Compose `foundation-layout` 要求 API 23；库最低版本据此改为 23。重新发布 AAR 并将消费者 `minSdk` 设为 23 后，`assembleDebug` 成功；API 21–22 不兼容已由该检查确认。
- `:app:assembleDebug`：**通过**。Compose 1.12.1 下 demo app 成功构建。
- API 34（Android 14）模拟器：**通过基础滚动和一次边界回弹检查**。debug app 安装启动后，灰色 LazyColumn 从可见的 `Item 0`–`Item 22` 滑到 `Item 30`–`Item 49`。重新安装当前构建后，在灰色列表顶部向下拖动，外层内容随之下移；释放后内容回到原位，应用进程仍在运行，最近 400 条 `AndroidRuntime:E` 查询没有错误输出。这只覆盖当前 demo 的一次纵向嵌套边界操作；没有验证 API 37 模拟器、最低 API 23 设备、所有嵌套滚动边界或 JitPack 远端构建，也没有运行自动化测试。
- Gradle 首次检查 SDK 时自动创建了 Android SDK Platform 37.0（API 37，Extension Level 22）和 Build-Tools 36.0.0。后续构建通过 `android.builder.sdkDownload=false` 禁止继续自动下载；本次没有删除这些 SDK 文件。

## 官方资料

- [AndroidX Compose 稳定版本表](https://developer.android.com/jetpack/androidx/versions)
- [Compose BOM](https://developer.android.com/develop/ui/compose/bom)
- [Compose 设置、编译器插件及 compileSdk 要求](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [Compose Foundation 发行说明](https://developer.android.com/jetpack/androidx/releases/compose-foundation)
- [NestedScrollSource API 参考](https://developer.android.com/reference/kotlin/androidx/compose/ui/input/nestedscroll/NestedScrollSource)
- [AGP 9.2.0 发行说明](https://developer.android.com/build/releases/agp-9-2-0-release-notes)
- [AGP 9.4.0 发行说明](https://developer.android.com/build/releases/agp-9-4-0-release-notes)
- [AGP 内置 Kotlin 迁移说明](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [JitPack Android 发布说明](https://docs.jitpack.io/android/)
