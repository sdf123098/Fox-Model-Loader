# Fox Model Loader: Revival 2.0

**Fox Model Loader: Revival 2.0** 基于 Sparkle Morpher **1.2.9** 已发布源码恢复，延续 Fox Model Loader 的自定义模型体验。模组 ID 与资源命名空间为 `foxmodelloader`，保留模型自身的 ID 和 `.ysm` 格式。

本次发布采用全新的像素风格模组图标，提供 Fabric 与 NeoForge 两种加载器、三个 Minecraft 版本的六个标准版 JAR。

**QQ：** 1104823534 | **Discord：** [加入社区](https://discord.gg/3KqK7USF39) | **Patreon：** [cw/Soid211](https://www.patreon.com/cw/Soid211) | **爱发电：** [Micaftic](https://afdian.com/a/Micaftic)

## 本次发布

- 恢复 Sparkle Morpher 1.2.9 的模型加载、动画、音效与客户端／服务端同步功能。
- 统一模组名称为 **Fox Model Loader: Revival**，版本为 **2.0**，使用 `foxmodelloader` 模组 ID 和资源命名空间。
- 更换像素风格模组图标，六个发行 JAR 使用同一图标。
- 提供 Minecraft **1.21.1、26.1.2、26.2** 的 Fabric 与 NeoForge 版本。
- 标准版内置从源码重建的 SIMD 原生渲染库，包含六个平台的二进制文件。

## 功能概览

### 自定义模型与格式

用自定义 3D 模型替换玩家外观，并为模型配置动画和音效。支持 `.ysm` 模型、Blockbench `.bbmodel` 项目和 Figura Avatar `.zip` 包；支持从本地文件、目录或 URL 导入模型，并通过分组与收藏管理模型库。

### 动画与音效

- **动画轮盘**：默认按键 **Z**，快速切换模型动作与动画。
- **动画控制器**：支持状态机，以及循环、单次播放和保持播放结束状态。
- **Molang 表达式**：驱动动态动画与混合。
- **模型音效**：播放模型附带的语音和音效，内置 Java Opus 解码器。

### 多人游戏与服务端

保留原版 Minecraft 客户端／服务端模型同步：多人共享模型需要**服务端和客户端均安装对应版本的模组**。服务端可推送模型、配置黑名单，并通过 `config/foxmodelloader-server.toml` 设置全局模型传输带宽。

本次恢复沿用 1.2.9 的同步方案，未引入后续 SPM Cloud 迁移。

## 下载与版本选择

只安装与你的 Minecraft 版本和加载器匹配的一个 JAR。文件名中的 `26.1.x` 对应 26.1 系列，此次构建目标为 **26.1.2**。

| Minecraft | 加载器 | Java | 下载 |
| --- | --- | --- | --- |
| 1.21.1 | Fabric | 21 | [fox-model-loader-revival-2.0-fa1.21.1.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.0/fox-model-loader-revival-2.0-fa1.21.1.jar) |
| 26.1.2 | Fabric | 25 | [fox-model-loader-revival-2.0-fa26.1.x.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.0/fox-model-loader-revival-2.0-fa26.1.x.jar) |
| 26.2 | Fabric | 25 | [fox-model-loader-revival-2.0-fa26.2.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.0/fox-model-loader-revival-2.0-fa26.2.jar) |
| 1.21.1 | NeoForge | 21 | [fox-model-loader-revival-2.0-neo1.21.1.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.0/fox-model-loader-revival-2.0-neo1.21.1.jar) |
| 26.1.2 | NeoForge | 25 | [fox-model-loader-revival-2.0-neo26.1.x.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.0/fox-model-loader-revival-2.0-neo26.1.x.jar) |
| 26.2 | NeoForge | 25 | [fox-model-loader-revival-2.0-neo26.2.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.0/fox-model-loader-revival-2.0-neo26.2.jar) |

## 安装

1. 安装对应 Minecraft 版本的 Fabric 或 NeoForge，以及表中所需 Java。
2. 下载匹配的 JAR，放入游戏实例的 `mods` 目录；更新时移除旧版模组 JAR。
3. **Fabric 版另需安装对应 Minecraft 版本的 Fabric API**。其余所需依赖已通过 Jar-in-Jar 打包。
4. 多人模型共享时，在服务端与各客户端安装匹配的版本。

本页提供的是包含原生库的**标准版**。内置原生库覆盖 Windows x64／x86、Linux x64、macOS x64／arm64 和 Android arm64；平台可用性仍取决于相应的 Minecraft 运行环境。

## 构建与校验

六个标准版 JAR 均经过 `clean build -Pdist=native --no-build-cache --rerun-tasks` 干净构建。各版本测试套件通过（各有 1 项跳过）；模组身份、图标、JAR 完整性及六个平台原生库的 SHA-256 校验均通过。

| 文件 | SHA-256 |
| --- | --- |
| `fox-model-loader-revival-2.0-fa1.21.1.jar` | `ef869e1e15d121b8c34aed44b276e174d31659321322bf932ff8257bc8716525` |
| `fox-model-loader-revival-2.0-fa26.1.x.jar` | `0ab0392dbb3c966cf56db7cee480679a70df400a0afa3f902a73b73f47717018` |
| `fox-model-loader-revival-2.0-fa26.2.jar` | `16ae02bd70f6861c138ccffb8b60239a41772401550a69be2c0f416369a7edb0` |
| `fox-model-loader-revival-2.0-neo1.21.1.jar` | `a6862ecc23b613efe9255eb0b23ebddf0f509a3a9e361b4ffa8249c797a36f26` |
| `fox-model-loader-revival-2.0-neo26.1.x.jar` | `f93a89653e49907517c61ea57d48cff0c6941cd6b77cd53f410c8ac334ae67f6` |
| `fox-model-loader-revival-2.0-neo26.2.jar` | `93fa06074f66a628492fdbbc8e9a57efebc6766cbd85fa5d863d10a1618cae56` |

## 致谢与许可证

- 基于 Sparkle Morpher 1.2.9 恢复。
- [OpenYSM](https://github.com/OpenYSM) 与 [YSMParser](https://github.com/OpenYSMDev/YSMParser)：模型格式、解析与渲染基础。
- [Blockbench](https://github.com/JannisX11/blockbench)：模型与动画创作工具。
- 默认模型库：[sdf123098/YSM-Model](https://github.com/sdf123098/YSM-Model)。

**许可证：MIT。**
