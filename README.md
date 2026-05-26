<div align="center">
  <img src="images/144725232_p0.png" alt="banner"/>
  <p>图片作者：「pixiv」<a href="https://www.pixiv.net/users/76344429">法师来自未来</a></p>
  <h1>Fox Model Loader / 绯绯狐的模组加载</h1>
  <p>基于 <a href="https://gitgud.io/NoSteveModel/OpenYSM">OpenYSM</a> 项目的 1.21+ 迁移版本，适用于 <strong>Fabric</strong> 加载器</p>
</div>

## 源码地址

本项目基于上游项目 OpenYSM 进行迁移：

- **上游源码**: [https://gitgud.io/NoSteveModel/OpenYSM](https://gitgud.io/NoSteveModel/OpenYSM)

## 关于 YSM 与 OpenYSM

YSM 与 OpenYSM 之间的过往纠葛并非本项目的重点。绯绯狐的模组加载仅专注于将 OpenYSM 迁移至 Minecraft 1.21+ 平台，为社区提供一个可用的模型加载工具。请勿在本仓库中讨论与开发无关的争议话题。

## 说明

本仓库包含基于 OpenYSM 的 Minecraft 1.21+ Fabric 迁移版本。

本项目是 AI Agent 驱动模组迁移的一次成功实践。在 DeepSeek V4 Pro（1M 上下文）与 Claude Code 的协同下，原本需要一周的移植工作压缩至一天内完成。整个迁移过程消耗约 1 亿 token，花费仅 10 元人民币，却解决了大量 mixin 变更、API 重构、依赖与环境配置等问题——即使没有 Java 开发经验也能完成。

这证明了一件事：**迁移模组，人人可为。** DeepSeek 的低成本优势配合 Claude Code 的精准代码编辑，让每个有想法的开发者都能以极低门槛完成模组迁移。这就是 AI Agent 的意义——降低门槛，释放创造力。

> **注意：** 项目并非 Production Ready，可能存在命名语义错误、渲染问题等。如遇任何问题请提交 Issue，附带截图与报错日志。

---

## 迁移历程

本项目经历了两个阶段的迁移：从 **OpenYSM 1.20.1** 迁移至 **Minecraft 1.21.1 Fabric**，再从 **1.21.1** 迁移至 **Minecraft 26.1.2 Fabric**。

---

### 阶段一：1.20.1 → 1.21.1 迁移

**驱动引擎：** DeepSeek V4 Pro（1M 上下文） + Claude Code

1.21.1 是 Mojang 对游戏 API 进行大幅调整的版本，迁移过程涉及：

- **Mixin 变更** — 大量 mixin 目标方法签名改变，需要逐一适配
- **变量与方法重命名** — Mojang 在 1.21.1 中修改了多个核心类的字段与方法名，引发连锁编译错误
- **依赖与环境配置** — Fabric Loom、Gradle 插件版本升级，解决各类构建环境问题

最终成功解决所有编译与运行时问题，游戏可正常启动并加载模型。

#### 构建环境（1.21.1）

| 组件 | 版本 |
| ---- | ---- |
| Java | 21 |
| Fabric Loom | 1.8-SNAPSHOT |
| Fabric Loader | 0.16.x |
| Fabric API | 1.21.1 对应版本 |

#### 经验总结

- DeepSeek V4 Pro 的 1M 上下文窗口适合处理大规模代码迁移，能在单次对话中理解整个项目结构
- Claude Code 的精准编辑能力与 DeepSeek 的低成本形成互补，推荐作为模组迁移的标准组合
- 不懂 Java 也能完成迁移——AI Agent 承担了依赖解析、环境配置、错误修复等全部技术工作

---

### 阶段二：1.21.1 → 26.1.2 迁移

**驱动引擎：** GPT-5.5 + Codex

Mojang 在 26.x 版本中对游戏引擎几乎所有子系统做了破坏性改动，迁移难度远超 1.21.1。此阶段 GPT-5.5 在多个关键领域发挥了重要作用：

#### GPT-5.5 的核心贡献

**GPU 渲染管线重写：**
- 指导将 `GuiGraphics` 全面替换为 `GuiGraphicsExtractor`，所有渲染方法重命名（如 `drawString()` → `text()`），完成 34 个渲染文件的逐一迁移
- 协助排查 GPU 渲染管线中的着色器兼容性问题与缓冲区绑定变更，确保模型在新渲染架构下正确显示

**模型渲染适配：**
- 修复 `EntityRenderer` 新增的第三类型参数问题，协助实现 `extractRenderState()` / `createRenderState()` 抽象方法
- 解决模型加载后的骨骼绑定、纹理映射与动画状态机在新 API 下的兼容问题
- 排查并修复多个模型渲染崩溃，确保自定义模型在 26.x 下正常渲染

**动作系统修复：**
- 适配输入系统翻新——鼠标/键盘事件从原始类型改为结构化事件对象（如 `mouseClicked(MouseButtonEvent, boolean)`）
- 修复动作绑定在新输入系统下的事件分发与处理逻辑，确保模型动画动作可正常触发

**其他关键贡献：**
- 实体类层级重组 — `AbstractArrow`、`Boat` 等类的包路径变更，数十个 import 修正
- NBT/Registry API 适配 — `getString()`、`getCompound()` 等改为 Optional 返回值，`getAllKeys()` 被移除，逐一迁移调用点
- 无官方 Mojang 映射 — 26.1.2 不再提供 `client_mappings`，GPT-5.5 协助编写 ASM 9.9 字节码分析工具，生成 tiny v2 映射文件
- Fabric Loom 升级 — 从 1.8-SNAPSHOT 升级至 1.17.0-alpha.8 以支持 Java 25 字节码
- Architectury API 无 26.x 支持 — 创建 22 个 stub 类复制所需平台抽象

最终解决 **200+ 编译错误**、**40+ 运行时崩溃**，游戏可正常启动并加载模型。

#### 构建环境（26.1.2）

| 组件 | 版本 |
| ---- | ---- |
| Java | 25 (Azul Zulu 25.0.2) |
| Gradle | 9.5.1 |
| Fabric Loom | 1.17.0-alpha.8 |
| Fabric Loader | 0.19.2 |
| Fabric API | 0.149.1+26.1.2 |
| Shadow 插件 | 9.0.0-beta4 |

#### 经验总结

1. **Stub 策略至关重要** — 只为目标 JAR 中确实不存在的类创建 stub，真实存在的类必须删除 stub 用真实类替代
2. **方法签名必须精确匹配** — 参数类型精确到包名，`Object` 无法替代具体类型
3. **`javap` 是核心工具** — 每个 API 接口通过字节码检查确认，而非靠猜测
4. **批量修复优于手动修改** — 相同类型错误的自动化批处理比逐个手动修复快 10 倍
5. **GPT-5.5 在渲染与动作系统修复中表现出色** — 对 GPU 渲染管线、模型渲染架构、输入事件系统等底层变更的理解深度，使其能快速定位问题根因并给出准确修复方案
