# Fox Model Loader

**Version**: 1.0.3 | **Platform**: Minecraft 26.1.2 Fabric | **Based on**: [OpenYSM](https://github.com/KingOfTheKongMC/openysm-26.1.2)

---

## English

### What is Fox Model Loader?

Fox Model Loader is a custom model loading mod for Minecraft 26.1.2 (Fabric), forked from and built upon the **OpenYSM** (Open Yes Steve Model) project. It enables players to load, manage, and render custom 3D character models in-game, replacing the default player skin with fully animated geometric models.

### The Migration Story

This project was born out of necessity. OpenYSM was originally targeting Minecraft 1.20.1, and migrating it to **Minecraft 26.1.2 Fabric** was nothing short of a battle. Mojang's 26.x update brought sweeping, breaking changes across nearly every subsystem of the game engine:

- **The entire rendering pipeline was rewritten.** `GuiGraphics` was replaced by `GuiGraphicsExtractor`. Every single rendering method was renamed — `drawString()` became `text()`, `pushPose()` became `pose().pushMatrix()`, and so on. Thirty-four files had to be painstakingly migrated one by one.

- **The input system was overhauled.** Mouse and keyboard events changed from raw primitives (`mouseClicked(double,double,int)`) to structured event objects (`mouseClicked(MouseButtonEvent, boolean)`). Every screen and widget needed updating.

- **Entity hierarchies were restructured.** Classes like `AbstractArrow` moved from `projectile` to `projectile.arrow`, `Boat` from `vehicle` to `vehicle.boat`. Dozens of imports had to be corrected.

- **NBT and Registry APIs became Optional-based.** Methods like `getString()`, `getBoolean()`, and `getCompound()` now return `Optional`, and the old `getAllKeys()` was removed entirely.

- **EntityRenderer grew a third type parameter.** The render pipeline added `extractRenderState()` and `createRenderState()` abstractions that didn't exist before.

- **No official Mojang mappings existed for 26.1.2.** The version manifest no longer ships `client_mappings`. A custom bytecode analysis tool (using ASM 9.9) had to be written from scratch to extract class/field/method names from the merged JAR and generate tiny v2 mapping files.

- **Fabric Loom itself needed upgrading** from 1.8-SNAPSHOT to 1.17.0-alpha.8 just to support Java 25 bytecode (class version 69).

- **Architectury API had no 26.x support**, so 22 stub classes were created to replicate the needed platform abstractions.

The result: **200+ compilation errors** and **40+ runtime crashes**, all resolved. The game boots, reaches the main menu, and loads models.

### Build Environment

| Component        | Version                 |
| ---------------- | ----------------------- |
| Java             | 25 (Azul Zulu 25.0.2)   |
| Gradle           | 9.5.1                   |
| Fabric Loom      | 1.17.0-alpha.8          |
| Fabric Loader    | 0.19.2                  |
| Fabric API       | 0.149.1+26.1.2          |
| Shadow Plugin    | 9.0.0-beta4             |

### Lessons Learned

1. **Stub strategy matters.** Only create stubs for classes that genuinely don't exist in the target MC JAR. If the class exists in the real JAR, delete the stub and use the real one — otherwise you get silent conflicts.
2. **Method signatures must be exact.** Parameter types must match down to the package name. `Object` cannot substitute for a concrete type.
3. **`javap` is your best friend.** Every API surface was verified through bytecode inspection, not guesswork.
4. **Batch fixes beat manual edits.** When dozens of files have the same type of error, automated batch processing is 10x faster than fixing them one by one.

---

## 中文

### 什么是 Fox Model Loader？

Fox Model Loader 是一个面向 Minecraft 26.1.2 Fabric 的自定义模型加载模组，基于 **OpenYSM**（Open Yes Steve Model）项目分叉构建。它允许玩家在游戏内加载、管理和渲染自定义 3D 角色模型，用带有完整动画的几何模型替换默认的玩家皮肤。

### 迁移历程

这个项目诞生于一次不得不打的硬仗。OpenYSM 最初面向 Minecraft 1.20.1，将其迁移到 **Minecraft 26.1.2 Fabric** 的过程堪称一场艰苦卓绝的战斗。Mojang 的 26.x 版本对游戏引擎几乎所有子系统都做了彻底的、破坏性的改动：

- **渲染管线完全重写。** `GuiGraphics` 被替换为 `GuiGraphicsExtractor`。每一个渲染方法都被改名 —— `drawString()` 变成了 `text()`，`pushPose()` 变成了 `pose().pushMatrix()`，诸如此类。34 个文件不得不逐一迁移，工作量巨大。

- **输入系统全面翻新。** 鼠标和键盘事件从原始基本类型（`mouseClicked(double,double,int)`）改为结构化事件对象（`mouseClicked(MouseButtonEvent, boolean)`）。每一个屏幕和控件都需要更新。

- **实体类层级结构重组。** 像 `AbstractArrow` 从 `projectile` 移到了 `projectile.arrow`，`Boat` 从 `vehicle` 移到了 `vehicle.boat`。数十个 import 语句需要修正。

- **NBT 和注册表 API 改为 Optional 返回值。** `getString()`、`getBoolean()`、`getCompound()` 等方法现在返回 `Optional`，旧的 `getAllKeys()` 被彻底移除。

- **EntityRenderer 新增第三个类型参数。** 渲染管线加入了 `extractRenderState()` 和 `createRenderState()` 两个全新的抽象方法。

- **26.1.2 没有官方 Mojang 映射文件。** 版本清单不再提供 `client_mappings`。为此专门编写了一个基于 ASM 9.9 的字节码分析工具，从合并后的 JAR 中提取类名、字段名和方法名，生成 tiny v2 格式的映射文件。

- **Fabric Loom 本身也需要升级**，从 1.8-SNAPSHOT 升级到 1.17.0-alpha.8，才能支持 Java 25 字节码（class version 69）。

- **Architectury API 尚不支持 26.x**，因此创建了 22 个 stub 类来复制所需的平台抽象层。

最终成果：**200 多个编译错误**和**40 多次运行时崩溃**，全部修复。游戏可以正常启动、进入主菜单并加载模型。

### 构建环境

| 组件           | 版本                    |
| -------------- | ----------------------- |
| Java           | 25 (Azul Zulu 25.0.2)  |
| Gradle         | 9.5.1                   |
| Fabric Loom    | 1.17.0-alpha.8          |
| Fabric Loader  | 0.19.2                  |
| Fabric API     | 0.149.1+26.1.2          |
| Shadow 插件    | 9.0.0-beta4             |

### 经验总结

1. **Stub 策略至关重要。** 只在目标 MC JAR 中确实不存在的类才创建 stub。如果真实 JAR 中存在该类，必须删除 stub 并使用真实类 —— 否则会产生隐蔽的冲突。
2. **方法签名必须精确匹配。** 参数类型必须精确到包名，`Object` 无法替代具体类型。
3. **`javap` 是核心工具。** 每个 API 接口都通过字节码检查来确认，而不是靠猜测。
4. **批量修复优于手动逐个修改。** 当数十个文件存在相同类型的错误时，自动化批处理比逐个手动修复快 10 倍。
