# 绯绯狐的模型加载器：复兴

<img src="src/main/resources/foxmodelloader.png" alt="绯绯狐的模型加载器：复兴" width="160">

绯绯狐的模型加载器：复兴（Fox Model Loader: Revival）沿袭 Sparkle Morpher **1.2.9** 路线。模组 ID 与资源命名空间为 `foxmodelloader`，保留传统 Minecraft 服务端同步架构；联机共享模型需要服务器与客户端都安装模组。各模型自身的 ID 与 `.ysm` 格式保持兼容。

> [English](README.md) | **中文** | [日本語](README_ja.md) | [한국어](README_ko.md)

**QQ:** 1104823534 | **Discord:** [点此加入](https://discord.gg/3KqK7USF39) | **Patreon:** [cw/Soid211](https://www.patreon.com/cw/Soid211) | **爱发电:** [Micaftic](https://afdian.com/a/Micaftic)

Minecraft 综合自定义模型加载器，让玩家为角色挂载自定义模型、动画与音效——告别一成不变的方块小人。

> 这是一个**综合模型加载器**：当前支持 `.ysm` 格式（基于 OpenYSM，MIT 许可）和 `.bbmodel` 格式（Blockbench），后续会陆续加入对其他主流模型格式的支持。

---

## 发布政策（Release Policy）

项目采用**季度 LTS 风格更新**：每季度安排一个计划功能版本。必要时，可在季度版本之间发布维护版本，修复严重 bug、兼容性问题或安全问题。

这一发布节奏是有意为之。项目优先考虑稳定性、兼容性和服务器部署的可预测性，不追求高频功能更新。

具体版本历史、发布说明与更新日志主要记录在 [Releases](https://github.com/sdf123098/Fox-Model-Loader/releases) 中。

## 项目历史（Project History）

Fox Model Loader 原先演化为 Sparkle Morpher。在 Sparkle Morpher **1.2.9** 之后，项目路线发生分叉。Fox Model Loader 从 **2.0** 开始，以 **Revival（复兴）** 身份恢复维护，继续保留传统 Minecraft 服务端同步架构。

## 功能特性

### 自定义玩家模型与皮肤

用完全自定义的 3D 模型替换原版玩家模型。所有自定义模型在多人游戏中**对其他玩家可见**

### 模型格式支持

- **`.ysm`** — 基于 OpenYSM/YSMParser 的原生格式，支持完整骨骼模型与权重动画。
- **`.bbmodel`** — 直接导入 Blockbench 项目文件。支持网格三角化（N 边形扇形三角化）、UV 归一化、面旋转、膨胀扩展、内嵌 Base64 纹理提取及 PNG IHDR 头解析。
- **Figura 头像包** — 直接导入 Figura `.zip` 压缩包。内置 `ZipModelSniffer` 自动识别并分流 YSM 文件夹、Figura 头像和纯 BBModel 压缩包。

### 动画系统

- **动画转盘**（默认按键：Z）— 径向菜单快速切换当前模型的动作与动画。
- **动画控制器** — 完整支持基于状态机的动画控制器，具备 `loop`（循环）、`once`（单次）和 `hold`（保持）播放模式。
- **Molang 表达式** — 数据点同时支持原始数值和 Molang 表达式字符串，实现动态动画混合。

### 音效系统

播放模型内置的语音和音效，由技能或动作触发。音频采用 **Opus** 格式，通过内置 Java Concentus 解码器播放。

### 多模型管理

- 从本地文件、目录或 URL 导入模型，支持加速下载。
- 按分组和收藏组织管理模型。
- 自动目录扫描，识别 `.ysm`、`.zip` 和 `.bbmodel` 文件。

### 服务端功能

- 服务端可定义模型清单并下发至客户端。
- 可配置黑名单（`config/foxmodelloader/blacklist.txt`）限制特定模型。
- 通过 Cardinal Components 实体数据实现客户端-服务端模型状态同步。

### 服务端带宽上限

服主可以在 `config/foxmodelloader-server.toml` 中配置全局模型传输限速：

```toml
[server_scheduler]
EnableGlobalBandwidthLimit = false
BandwidthLimit = 5
```

`BandwidthLimit` 单位为 Mbps。开启 `EnableGlobalBandwidthLimit` 后，该上限会全局共享作用于服务端向客户端下发模型同步包，以及客户端向服务端上传模型分片；收藏同步等普通小包不受限速影响。

### 模组兼容性

兼容主流模组：

| 类别 | 兼容模组 |
|------|---------|
| 战斗 | Better Combat |
| 饰品 | Curios |
| 建造与自动化 | Create |
| 渲染 | Iris、Sodium |
| 玩家皮肤 | 皮肤层兼容 |

### 当前发布目标

当前发布目标覆盖 Minecraft 1.21.1、26.1.2、26.2 的 Fabric 和 NeoForge。支持版本及对应下载以 [Releases](https://github.com/sdf123098/Fox-Model-Loader/releases) 为准。

| 变体 | 加载器 | Minecraft |
| --- | --- | --- |
| Fox-Model-Loader-Fa1.21.1 | Fabric | 1.21.1 |
| Fox-Model-Loader-Fa26.1.2 | Fabric | 26.1.2 |
| Fox-Model-Loader-Fa26.2 | Fabric | 26.2 |
| Fox-Model-Loader-Neo1.21.1 | NeoForge | 1.21.1 |
| Fox-Model-Loader-Neo26.1.2 | NeoForge | 26.1.2 |
| Fox-Model-Loader-Neo26.2 | NeoForge | 26.2 |

---

## 工作原理

### 模型导入管线

导入模型文件时，绯绯狐的模型加载器：复兴会执行智能处理管线：

1. **压缩包嗅探** — 按内容分类：YSM 文件夹、Figura 头像（含 `avatar.json` + `.bbmodel`）、纯 BBModel 压缩包或未知格式。
2. **解析** — `.ysm` 文件经 YSMParser 处理；`.bbmodel` 文件由内置 `BBModelParser` 解析，处理大纲树、立方体/网格元素、纹理、动画和控制器状态。
3. **转换** — 解析数据转换为引擎内部 `RawGeometry` 格式。N 顶点网格面经扇形三角化处理；UV 坐标按纹理分辨率归一化；压缩包中的外部 PNG 纹理优先于内嵌 Base64 源。
4. **渲染** — 转换后的模型在玩家激活时替换原版渲染器，自动隐藏默认玩家模型。

### BBModel 兼容性

完整支持 Blockbench 格式，包括：

- 大纲树的嵌套骨骼层级与父子关系
- 立方体和网格元素的正确面 UV 映射
- 内嵌纹理（Base64）的 PNG 头尺寸检测
- 动画播放与循环模式映射
- Blockbench 5 "free" 格式兼容（精简大纲节点 + `groups[]` 回退）
- 孤立元素处理（未被引用的元素自动归入默认骨骼）

---

## 架构

绯绯狐的模型加载器：复兴采用**公共核心 + 平台适配器**分层架构：

- **`common`** — 所有变体共享的核心逻辑：模型解析、网格处理、压缩包嗅探、动画控制器、音频解码和 Molang 求值。
- **`fabric`** / **`neoforge`** — 平台特定适配器，处理初始化、网络通信、组件注册和渲染钩子。
- **原生渲染层** — 从本仓库源码重新构建的 SIMD 加速；详见[原生源码与构建来源](NATIVE_SOURCES.md)。

---

## 依赖

因构建变体而异——详见 `mods.toml`（NeoForge）或 `fabric.mod.json`（Fabric）。Fabric 变体需用户自行安装 Fabric API；其他依赖通过 Jar-in-Jar 内置。

---

## 致谢与许可

- 基于 [OpenYSM](https://github.com/OpenYSM)（MIT 许可证）二次开发。
- 使用 [OpenYSMDev/YSMParser](https://github.com/OpenYSMDev/YSMParser)（MIT）进行 `.ysm` 模型解析。
- 默认模型库：[sdf123098/YSM-Model](https://github.com/sdf123098/YSM-Model)。
- Blockbench 格式由 [JannisX11/Blockbench](https://github.com/JannisX11/blockbench) 开发。

**许可证：** MIT

## 构建

1.21.1 使用 Java 21；26.x 使用 Java 25。标准包运行 `./gradlew build`，Java 回退包运行 `./gradlew build -Pdist=curseforge`。标准包内置重新构建的 SIMD 渲染库；CurseForge 包不包含本项目原生库。重建全部六个原生平台：`python scripts/rebuild-natives.py --zig <zig.exe> --ndk <Android NDK 根目录>`。详见[原生源码与构建来源](NATIVE_SOURCES.md)。
