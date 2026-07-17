<div align="center">
  <img src="images/144725232_p0.png" alt="Fox Model Loader banner"/>
  <p>图片作者：「pixiv」<a href="https://www.pixiv.net/users/76344429">法师来自未来</a></p>
  <h1>Fox Model Loader / 绯绯狐的模组加载</h1>
  <p>基于 <a href="https://github.com/OpenYSMDev/OpenYSM">OpenYSM</a> 的 Minecraft 自定义玩家模型加载器</p>
  <p>支持 Minecraft 26.1.x、Fabric 与 NeoForge</p>
</div>

> **📌 声明**
>
> 本项目人员**只负责模组移植**。开发与测试人员均与 OYSM 团队及 YSM 团队**没有任何关系**。
>
> 从7月17日开始，Fox Model Loader正式停止维护，无论你要自用还是fork，请切换到花火火的变身器，项目地址：https://github.com/sdf123098/Sparkle-Morpher
>
> 鉴于 OYSM 与 YSM 之间的历史争议，我们**不关注、也不参与**这些事情。我们的内心只有移植。
>
> 如果你觉得这是在挑衅，那你错了。我们只是在研究 **Vibe Coding**——即借助 AI 辅助工具探索模组移植与二次开发的可行性。
>
> 请勿在本仓库中发起与代码无关的争论。

## 项目简介

Fox Model Loader 是一款基于 OpenYSM 构建的 Minecraft 模组，可以将原版玩家模型替换为带完整动画的自定义模型。模组使用 Minecraft 基岩版模型与动画文件（`.ysm`），并借助 GeckoLib 实现复杂动画播放，让每位玩家都能拥有独立的角色外观、动作和第一人称手臂表现。

本项目专注于将 OpenYSM 迁移到 Minecraft 1.21+ 与 26.1.x 平台，并持续适配新版客户端的渲染、输入、动画与资源管理变化。

## 主要特性

### 自定义玩家模型

- 加载 `.ysm` 基岩版格式模型，替换默认 Steve / Alex 玩家模型。
- 每位玩家可独立选择模型与纹理，支持在模型、纹理和分类之间切换。
- 26.1.2 版移除了杂项分类（Alex、Steve 等皮肤模型），精简内置模型列表；default 模型不再受原版皮肤覆盖干扰。

### 完整动画系统

- 基于 Molang 表达式的动画引擎，支持复杂状态机与数据驱动动画。
- 内置行走、奔跑、跳跃、潜行、游泳、飞行、滑翔等移动动画。
- 支持剑、斧、镐、锹、锄、重锤、三叉戟、原版矛等武器姿势。
  - 26.1.2 版新增"原版矛"独立分类，三叉戟恢复为独立类型，不同材质矛使用数据驱动时长，基于原版动力武器组件计算动画采样。
- 支持拉弓、进食、饮用药水、格挡、投掷等使用动画。
- 主手与副手动画可独立响应，右键行为匹配原版逻辑（副手非空 + 主手工具/武器时仅主手挥动）。
- 可按手持物品、穿戴装备、骑乘实体、当前维度等条件触发自定义动画。
- swing 动画阈值修正为 `<=2`，数据驱动模式下通过 `shouldForceSwingPredicate()` 确保 swing 控制器正确参与判定。

### 内置资源站

- 在模型选择界面中浏览远程模型仓库。
- 支持 `.ysm` 与 `.zip` 格式下载，支持搜索、筛选、下载队列和已完成任务管理。
- 内置 CDN 加速，便于快速获取模型资源。

### 模型分类管理

- 支持创建、重命名、删除分类文件夹，支持批量移动模型。
- 删除分类时可选择保留或移除其中内容，操作完成后自动刷新缓存。

### 模型上传与导入

- 支持从本地客户端选择模型并上传到服务器，文件夹模型自动打包为 `.zip`。
- 自定义文件夹上传界面采用紧凑图标工具栏，适配窄窗口与高界面缩放。
- 支持 `.ysm` 与 `.zip` 格式，已移除 `.7z` 入口。

### 实时模型预览

- GUI 中提供实时 3D 预览，支持旋转、缩放和查看模型细节。

### 第一人称手臂

- 将原版第一人称手臂替换为与当前模型匹配的自定义手臂，支持独立左手、右手骨骼，可同步手持物品与动画。

### 联机同步

- 客户端切换模型后自动同步到服务端，同一服务器中的其他玩家可看到对应的自定义模型。
- 服务端可配置模型上传权限与文件大小限制。
- 默认模型切换现已添加专项绕过逻辑，不再因服务端缓存校验误判而拒绝。

### 配置界面

- Fabric 版：通过 ModMenu 集成，在模组菜单中点击"设置"按钮即可打开配置界面。
- NeoForge 版：通过 `IConfigScreenFactory` + `registerExtensionPoint` 注册配置界面，模组菜单设置按钮可正常使用。
- 1.21.1 版：Fabric 与 NeoForge 配置界面均可在 v1.3.1 正常打开（NeoForge 1.21.1 为首次官方支持）。

### 诊断工具

- 动画帧分析器：帧级诊断工具，定位动画卡顿根因。
- 模型内存分析器：全生命周期内存检查点日志，可选开启"模型导入性能日志"。

### Android 支持

- 支持 PojavLauncher、FCL、Zalith Launcher，内置模型选择器与文件导入功能，提供与桌面端一致的体验。

## 源码与反馈

本项目基于上游 OpenYSM 迁移：

- 上游源码: https://github.com/OpenYSMDev/OpenYSM
- 反馈 QQ 群: 1104823534

Fox Model Loader 只专注于将 OpenYSM 迁移至新版 Minecraft 平台，并为社区提供可用的自定义玩家模型加载工具。请勿在本仓库中讨论与开发无关的争议话题。

## 迁移历程

本项目从 OpenYSM 1.20.1 出发，依次完成四个平台的迁移与适配，最终形成覆盖 1.21.1 与 26.1.2 双版本、Fabric 与 NeoForge 双加载器的四路构建体系。

### 阶段一：OpenYSM 1.20.1 → 1.21.1 Fabric

驱动引擎：DeepSeek V4 Pro（1M 上下文） + Claude Code

Minecraft 1.21.1 对游戏 API 做了较大调整，迁移主要解决以下技术问题：

- Mixin 目标方法签名变化，需要逐一适配。
- 核心类字段与方法重命名，引发连锁编译错误。
- Fabric Loom、Gradle 插件和 Fabric API 等依赖升级。

最终完成编译与运行时问题修复，游戏可正常启动并加载自定义模型。

**构建环境：**

| 组件 | 版本 |
| ---- | ---- |
| Java | 21 |
| Fabric Loom | 1.8-SNAPSHOT |
| Fabric Loader | 0.16.x |
| Fabric API | 1.21.1 对应版本 |

### 阶段二：1.21.1 → 26.1.2 Fabric

驱动引擎：GPT-5.5 + Codex

Minecraft 26.x 对渲染、输入、实体、NBT、Registry 与构建环境做了大量破坏性改动，迁移难度明显高于 1.21.1，主要解决以下技术问题：

- 将 `GuiGraphics` 相关渲染代码迁移到 `GuiGraphicsExtractor`，并适配新的渲染方法命名。
- 适配新版 `EntityRenderer` 类型参数、`extractRenderState()` 与 `createRenderState()`。
- 修复模型骨骼绑定、纹理映射、动画状态机在新 API 下的兼容问题。
- 适配新版鼠标和键盘输入事件对象。
- 修复 `AbstractArrow`、`Boat` 等实体类包路径变化导致的 import 问题。
- 适配 NBT 与 Registry API 中 Optional 返回值和键集合访问方式变化。
- 在缺少官方 Mojang 映射的情况下，通过 ASM 9.9 字节码分析工具生成 tiny v2 映射文件。
- 将 Fabric Loom 升级至 1.17.0-alpha.8，以支持 Java 25 字节码。
- 在 Architectury API 暂无 26.x 支持时，创建 22 个 stub 类补齐平台抽象。

最终解决 200+ 个编译错误和 40+ 个运行时崩溃，游戏可正常启动并加载模型。

**构建环境：**

| 组件 | 版本 |
| ---- | ---- |
| Java | 25 (Azul Zulu 25.0.2) |
| Gradle | 9.5.1 |
| Fabric Loom | 1.17.0-alpha.8 |
| Fabric Loader | 0.19.2 |
| Fabric API | 0.149.1+26.1.2 |
| Shadow 插件 | 9.0.0-beta4 |

### 阶段三：26.1.2 NeoForge 适配

驱动引擎：GPT-5.5 + Codex

在 26.1.2 Fabric 版稳定后，将项目迁移至 NeoForge 平台。NeoForge 26.1.2 使用独立的注册体系、数据组件 API、网络通道与事件总线，迁移主要解决以下技术问题：

- 将 Fabric 注册回调替换为 NeoForge `RegisterEvent` 与 `ModContainer.registerExtensionPoint()`。
- 将 Fabric 网络包替换为 NeoForge `Payload` + `RegisterPayloadHandlersEvent`。
- 将 Fabric API 事件替换为 NeoForge 事件总线订阅。
- 适配 NeoForge 数据组件注册与 `DataComponentType`。
- 配置系统从 Fabric Config API 迁移至 NeoForge `ModConfigSpec`。

### 阶段四：1.21.1 NeoForge 适配（最晚完成）

驱动引擎：GLM 5.2 + Claude Code

1.21.1 NeoForge 是四个平台中最后完成适配的，自 v1.3.1 起正式提供官方构建。迁移主要解决以下技术问题：

- 同阶段三的大部分 NeoForge API 替换工作，但需回退至 1.21.1 版本的 API 签名（如 1.21.1 不存在 `RegisterConfigScreenEvent`，改用 `IConfigScreenFactory` + `registerExtensionPoint`）。
- NeoForge 1.21.1 零 Architectury 依赖，与独立 Architectury API 无冲突风险，无需桩类处理。

**构建环境：**

| 组件 | 版本 |
| ---- | ---- |
| Java | 21 |
| NeoForge | 1.21.1 对应版本 |

## 迁移经验

1. Stub 只应为目标 JAR 中确实不存在的类创建，真实存在的类必须使用真实实现。
2. 方法签名需要精确匹配到包名和参数类型，不能用 `Object` 粗略替代。
3. `javap` 和字节码检查比猜测 API 更可靠。
4. 相同类型错误应优先批量修复，避免逐个手动处理。
5. 渲染管线、模型渲染和输入事件系统是 26.x 迁移中最需要重点验证的部分。
6. Architectury stub 类与真实 API 存在方法签名冲突时，仅靠 `depends` 声明不足以解决——Fabric 版可依赖 Loader 版本去重规避，但 26.1.2 版需源码级包重命名彻底消除碰撞。
7. Shadow 插件的 relocate 在 Java 25 项目中不可用（内置 ASM 不支持 class file major version 69），包重定位必须通过源码级修改完成。
8. NeoForge 1.21.1 不存在 `RegisterConfigScreenEvent`，配置界面注册必须使用 `IConfigScreenFactory` + `registerExtensionPoint`。
9. `swingPulseAge` 在 tick 中从 1 递增到 2，`<=1` 阈值永远为 false——动画阈值必须基于实际运行行为调试而非文档假设。
10. 默认模型不在服务端缓存时，C2S 同步包会误判无效——必须为 default 添加专项绕过逻辑。
