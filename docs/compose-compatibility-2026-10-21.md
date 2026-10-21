# Compose 版本兼容与 JitPack 报告

**核查日期：** 2026-10-21
**仓库：** [`Cormor/ComposeOverscroll`](https://github.com/Cormor/ComposeOverscroll)
**核查对象：** `2.0.0`（当前版）及历史 `1.x` 发布线

## 结论

当前版 `2.0.0` 已升级到 Jetpack Compose **1.12.1**，并在本机完成库 release 构建、Maven 发布、独立消费者编译和 API 34 模拟器滚动验证。新项目使用的 GitHub/JitPack 依赖坐标为：

```text
com.github.Cormor:ComposeOverscroll:2.0.0
```

旧项目使用 Compose `1.5.0`–`1.12.1` 时，可用 `com.github.Cormor:ComposeOverscroll:1.3.1`。JitPack 已成功构建这个根项目坐标，实际远端 AAR 通过独立消费者完整构建和 API 34 模拟器滚动检查；JitPack 没有提供对应的 `overscroll_core` 模块路径。用该 AAR 做的 11 个 Compose 代表版本本地矩阵也全部通过编译和 `1.5.0` 起的运行检查。`1.3.3` 和 `1.4.3` 虽能编译预制 AAR 消费者，但源码编译失败，运行时也因 Compose Runtime 缺少 `PrimitiveSnapshotStateKt` 而崩溃，因此不支持。

`2.0.0` 和 `1.3.1` 都使用 JitPack 根项目坐标；`2.0.0` 的模块坐标 `com.github.Cormor.ComposeOverscroll:overscroll_core:2.0.0` 仍返回 404，`1.3.1` 的同类模块路径也返回 404。关于 Google 问题 `276682419`，用户提供的截图显示工单字段为 `Won't fix (Obsolete)`；另有当前 AndroidX 主线回归测试通过的证据，因此本报告将“当前主线上的 nested-scroll fling 方向反转回归”标记为已修复，但不把 Google 工单字段说成 `Fixed`。问题 `261895103` 的当前状态仍无法读取。

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
- 历史 tag `1.3.0` 的根项目 JitPack 包虽能解析，但 AAR 的 `classes.jar` 为空，独立消费者无法找到 `overScrollVertical()`。新建 `1.3.1` 发布补丁，仅增加 `maven-publish`、release AAR/sources JAR 发布配置和 JDK 17 设置，关闭 AAR 收缩并把公开 Compose API 依赖改为 `api`。
- 根目录新增 `jitpack.yml`，指定 JitPack 使用 JDK 17；README 添加 JitPack 仓库和 `implementation` 示例。

## 跨版本兼容结论

`2.0.0` 面向 Compose `1.12.1`，最低构建工具要求为 `compileSdk 37`、AGP 9.2.0、Gradle 9.4.1 和 JDK 17；库最低 Android API 为 23。

历史兼容版 `1.3.1` 在表中 Compose `1.5.0` 起的代表稳定版通过源码编译、独立消费者 APK 编译和 API 34 模拟器滚动检查。此次测试的 Compose `1.3.3` 与 `1.4.3` 不兼容；Compose `1.3.x`/`1.4.x` 项目需先升级到 `1.5.0` 或更高版本，再使用 `1.3.1`。本结论只覆盖表中逐项测试的稳定版，不表示每个补丁版本都单独测试过。

`1.3.1` AAR 声明 `minSdk 21`；模拟器矩阵只使用 API 34，没有在 API 21–33 设备上验证。旧工具链本身使用 AGP 8.1.0、Gradle 8.1.1、Kotlin 1.9.0 和 Compose 1.5.0 成功发布本地 AAR；消费端矩阵使用较新的构建工具。`1.3.1` 不代表所有历史 `1.x` tag 都能复用相同结果。

### 代表版本测试矩阵

每个 Compose 次版本选一个 BOM 映射下的稳定版代表。源码测试以 tag `1.3.0` 的库源码和调用 `Modifier.overScrollVertical()` 的 app 为对象；二进制测试使用按旧工具链发布的 `1.3.1` AAR。模拟器运行项安装独立 app、加载 overscroll modifier，并执行纵向滚动手势。表中 `通过` 只指这些检查，不外推到全部设备或所有未列出的补丁版本。

| Compose 稳定版 | Compose BOM | `1.3.x` 源码编译 | `1.3.1` AAR 消费者编译 | API 34 模拟器运行 |
| --- | --- | --- | --- | --- |
| 1.3.3 | 2023.01.00 | 失败：`mutableFloatStateOf`、`canScrollBackward`、`canScrollForward` 不存在 | 通过 | 失败：缺少 `PrimitiveSnapshotStateKt`，主线程崩溃 |
| 1.4.3 | 2023.06.01 | 失败：`mutableFloatStateOf` 不存在 | 通过 | 失败：缺少 `PrimitiveSnapshotStateKt`，主线程崩溃 |
| 1.5.0 | 2023.08.00 | 通过 | 通过 | 通过 |
| 1.5.4 | 2023.10.01 | 通过 | 通过 | 通过 |
| 1.6.8 | 2024.08.00 | 通过 | 通过 | 通过 |
| 1.7.8 | 2025.04.00 | 通过 | 通过 | 通过 |
| 1.8.3 | 2025.07.00 | 通过 | 通过 | 通过 |
| 1.9.5 | 2025.11.01 | 通过 | 通过 | 通过 |
| 1.10.6 | 2026.03.01 | 通过 | 通过 | 通过 |
| 1.11.4 | 2026.06.01 | 通过 | 通过 | 通过 |
| 1.12.1 | 2026.09.00 | 通过 | 通过 | 通过 |

## 本次验证记录

- `gradlew wrapper --gradle-version 9.6.0 --distribution-type bin`：**通过**。wrapper 任务成功；随后 `gradlew --version` 显示 Gradle 9.6.0、JBR 21.0.10。
- `:overscroll_core:assembleRelease`：**通过**。使用 Compose 1.12.1 编译、打包 release AAR；编译期间没有 Compose API 错误。
- `:overscroll_core:publishReleasePublicationToMavenLocal`：**通过**。在仓库 `build/` 下的临时 Maven 仓库生成 `overscroll_core-2.0.0.aar`、POM、Gradle Module Metadata 和 sources JAR；本地发布坐标为 `com.github.Cormor.ComposeOverscroll:overscroll_core:2.0.0`，POM 中的 Compose API 依赖版本均为 1.12.1。
- 独立本地 Maven 消费者 APK：**通过**。临时 Android app 只通过仓库 `build/` 下的本地 Maven 仓库使用发布模块，没有引用 `:overscroll_core` project dependency 或本仓库源码；消费者代码调用了 `Modifier.overScrollVertical()`。首次用 `minSdk 21` 打包时，Manifest 合并报告 Compose `foundation-layout` 要求 API 23；库最低版本据此改为 23。重新发布 AAR 并将消费者 `minSdk` 设为 23 后，`assembleDebug` 成功；API 21–22 不兼容已由该检查确认。
- 独立 JitPack 消费者 APK：**通过**。临时 Android app 的依赖仓库只加入 JitPack 和 Android 常用仓库，依赖 `com.github.Cormor:ComposeOverscroll:2.0.0`，源码直接调用 `Modifier.overScrollVertical()`，没有项目依赖或本仓库源码引用；`:app:assembleDebug` 成功。JitPack 构建日志显示 `BUILD SUCCESSFUL`。但对应的模块路径 `com.github.Cormor.ComposeOverscroll:overscroll_core:2.0.0` 在 JitPack 上没有解析成功（POM 请求返回 404，API 的 modules 列表为空），因此 README 不再给出该模块坐标。
- `:app:assembleDebug`：**通过**。Compose 1.12.1 下 demo app 成功构建。
- API 34（Android 14）模拟器：**通过基础滚动和一次边界回弹检查**。debug app 安装启动后，灰色 LazyColumn 从可见的 `Item 0`–`Item 22` 滑到 `Item 30`–`Item 49`。重新安装当前构建后，在灰色列表顶部向下拖动，外层内容随之下移；释放后内容回到原位，应用进程仍在运行，最近 400 条 `AndroidRuntime:E` 查询没有错误输出。这只覆盖当前 demo 的一次纵向嵌套边界操作；没有验证 API 37 模拟器、最低 API 23 设备或所有嵌套滚动边界，也没有运行自动化测试。
- Gradle 首次检查 SDK 时自动创建了 Android SDK Platform 37.0（API 37，Extension Level 22）和 Build-Tools 36.0.0。后续构建通过 `android.builder.sdkDownload=false` 禁止继续自动下载；本次没有删除这些 SDK 文件。


### 历史 `1.3.1` 发布与消费验证

- 从历史 tag `1.3.0` 创建 `1.3.1` 发布分支，仅调整 Maven 发布配置和 AAR 收缩设置，库源码保持不变；使用 AGP 8.1.0、Gradle 8.1.1、JDK 17、Kotlin 1.9.0、Compose 1.5.0 执行 `publishReleasePublicationToMavenLocal`：**通过**。发布的 `classes.jar` 含 `OverScrollKt`，POM 将 animation、foundation、runtime、ui 作为 `compile` 依赖发布。
- `1.3.1` tag 指向提交 `8dfe060`。JitPack 构建日志显示 **BUILD SUCCESSFUL**；远端根坐标 `com.github.Cormor:ComposeOverscroll:1.3.1` 的 POM 和 AAR 可下载，AAR 中 `classes.jar` 与旧工具链本地发布物中的类文件 SHA-256 相同。JitPack 的模块路径 `com.github.Cormor.ComposeOverscroll:overscroll_core:1.3.1` 返回 404，因此 README 使用实际可用的根坐标。
- 独立 Android 消费者只引用 JitPack 根坐标，没有 project dependency 或本仓源码引用；强制刷新依赖后 `:app:clean :app:assembleDebug`：**通过**。API 34 模拟器安装该远端坐标生成的 APK，启动并执行纵向滚动手势后进程仍存活，未记录 `AndroidRuntime` 致命异常。
- 独立 Android 消费者使用本地 Maven 发布物，Compose BOM `2023.01.00` 到 `2026.09.00` 共 11 个代表版本的 `:app:assembleDebug` 全部通过。单独编译预制 AAR 消费者成功，不代表 AAR 能在运行时兼容所有这些版本。
- API 34 模拟器矩阵中，Compose `1.3.3`、`1.4.3` 启动后在 Compose 内容装配时都因 `NoClassDefFoundError: androidx.compose.runtime.PrimitiveSnapshotStateKt` 崩溃；Compose `1.5.0`、`1.5.4`、`1.6.8`、`1.7.8`、`1.8.3`、`1.9.5`、`1.10.6`、`1.11.4`、`1.12.1` 执行纵向滚动手势后进程仍存活，未记录 `AndroidRuntime` 致命异常。
- 源码矩阵使用 AGP 9.4.0、Gradle 9.6.0、Kotlin/Compose 插件 2.2.10、`compileSdk 37`；`1.3.3`、`1.4.3` 编译失败，`1.5.0`–`1.12.1` 列出的版本编译成功。源码编译、二进制消费编译和模拟器运行是不同验证层，兼容下限按源码编译及运行时均通过的 `1.5.0` 起算。

## Google Issue Tracker 状态核查

README 旧版列出了 [问题 276682419](https://issuetracker.google.com/issues/276682419) 和 [问题 261895103](https://issuetracker.google.com/issues/261895103)，并称 `1.4.0-beta02` 起有嵌套手势分发错误。ComposeOverscroll 仓库本身没有这个上游失败用例的测试模块；README 已移除未经本仓验证的复现断言。本次改为直接验证 AndroidX 上游的嵌套滚动测试。

用户提供的 276682419 截图显示问题标题为 **“Wrong velocity at compose 1.4.0 release”**，描述关联 Compose 1.4.0、`LazyColumn`/`nestedScroll` 和错误速度追踪的怀疑；右侧状态显示 **“Won't fix (Obsolete)”**。这是截图上的工单字段状态，与当前源码是否通过回归测试分开记录。当前环境未能读取 Issue Tracker 的在线页面，因此不能确认截图之后字段是否变化；本次也没有修改 Google 工单。

### AndroidX 上游回归测试

- 在 AndroidX `frameworks/support` 的 `androidx-main` 分支提交 `0b4947f8ab42b78ef92f001563926e2d6eb0b83c` 上运行 `:compose:ui:ui:connectedAndroidDeviceTest`，runner 限定为 `androidx.compose.ui.input.nestedscroll.NestedScrollModifierTest`；目标设备是 API 34 的 `sdk_gphone64_x86_64` 模拟器 `emulator-5554`。
- 结果：**34 项通过，0 失败、0 错误、0 跳过**，AndroidX 测试结果 XML 的 exit code 为 `0`。其中 Gerrit CL 2121433 引入的 `nestedScroll_movingTarget_velocityShouldRespectSign` 也通过。当前测试文件位于 [`NestedScrollModifierTest.kt`](https://android.googlesource.com/platform/frameworks/support/+/0b4947f8ab42b78ef92f001563926e2d6eb0b83c/compose/ui/ui/src/androidDeviceTest/kotlin/androidx/compose/ui/input/nestedscroll/NestedScrollModifierTest.kt#1005)。
- 这是在官方 AndroidX 源码的本地稀疏 checkout 上执行的模拟器测试，不是 Google CI 结果。AndroidX 源码和测试文件未改动；本机补齐了所需 JDK、Kotlin/Native 和 Android SDK 工具链，并只在隔离副本中为本次下载的两个外部依赖记录 SHA-256，Gradle strict 依赖校验一直开启。
- 该用例在 nested-scroll `LazyColumn` 移动目标上分别执行向上、向下 swipe，并检查 `onPreFling` 收到的速度符号是否与手势方向一致。这直接覆盖“fling 方向反转”这一部分；没有比较速度的绝对值，也没有专门构造用户所说的“小幅度、较快”触摸轨迹。
- 结合该上游回归用例通过，以及用户反馈本机目前不再复现，本报告将**当前 AndroidX 主线的 nested-scroll fling 方向反转回归**标记为“已修复（回归测试通过）”。这不追溯保证 Compose 1.4.0 的每个补丁版本，也不表示 Google Issue Tracker 的 `276682419` 字段已改为 `Fixed`；精确快速短距离轨迹和速度大小仍未由该单测验证。

同时核查了用户提供的 `飞书20230224-153314.mp4` 和 `2023-04-04_11-21-27.mp4`。第一段录屏能看到嵌套列表在约 10–12 秒间发生滚动，内层与外层列表位置有变化；第二段显示重复的列表内容和滚动画面。视频没有提供触摸采样、输入速度或逐帧位移标定，也没有对照组，不能单凭画面验证“速度追踪器错误”、测量速度偏差或证明缺陷已修复。

因此，对问题 276682419 要分开看：用户截图里的工单字段为 **Won't fix (Obsolete)**；本次 AndroidX 主线的速度方向回归测试则通过，所以当前主线的方向反转问题按上述范围标记为已修复。用户视频没有触摸采样、输入速度或逐帧位移标定，不能单凭画面验证异常速度大小。对问题 261895103，当前页面只显示登录入口，本次仍无法确认其状态。Google Issue Tracker 的访问说明指出，问题详情会受公开性和权限控制；页面不可见本身不能证明问题已关闭或仍开放。[访问 Issue Tracker](https://developers.google.com/issue-tracker/guides/access-ui) [Issue Tracker 访问控制](https://developers.google.com/issue-tracker/concepts/access-control)

ComposeOverscroll 的 API 34 模拟器矩阵只覆盖一个调用 overscroll modifier 的独立纵向滚动 app 和一次合成滚动手势；AndroidX 上游 nested-scroll 测试另在同版本 API 34 模拟器上单独执行。上游类测试通过不等于 ComposeOverscroll 的所有嵌套手势和速度条件都已覆盖。

## 官方资料

- [AndroidX Compose 稳定版本表](https://developer.android.com/jetpack/androidx/versions)
- [Compose BOM](https://developer.android.com/develop/ui/compose/bom)
- [Compose BOM 与 AndroidX 库版本映射](https://developer.android.com/develop/ui/compose/bom/bom-mapping)
- [Compose 设置、编译器插件及 compileSdk 要求](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [Compose Foundation 发行说明](https://developer.android.com/jetpack/androidx/releases/compose-foundation)
- [NestedScrollSource API 参考](https://developer.android.com/reference/kotlin/androidx/compose/ui/input/nestedscroll/NestedScrollSource)
- [AGP 9.2.0 发行说明](https://developer.android.com/build/releases/agp-9-2-0-release-notes)
- [AGP 9.4.0 发行说明](https://developer.android.com/build/releases/agp-9-4-0-release-notes)
- [AGP 内置 Kotlin 迁移说明](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [JitPack Android 发布说明](https://docs.jitpack.io/android/)
- [JitPack 多模块构建说明](https://docs.jitpack.io/building/)
- [Google Issue Tracker 访问说明](https://developers.google.com/issue-tracker/guides/access-ui)
- [Google Issue Tracker 访问控制](https://developers.google.com/issue-tracker/concepts/access-control)
