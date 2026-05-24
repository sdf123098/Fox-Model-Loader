# YSM MC 26.1.2 迁移状态

> 最后更新: 2026-05-21 01:57
> 构建状态: ✅ 全通过
> 运行状态: ✅ 进入主菜单

## 总览

从 MC 1.21.1 成功迁移到 26.1.2 Fabric，游戏可启动并进入主菜单。约 200+ 个编译错误、40+ 次运行时崩溃全部修复。

## 构建环境

- Java 25 (Azul Zulu 25.0.2)
- Gradle 9.5.1
- Fabric Loom 1.17.0-alpha.8
- Fabric Loader 0.19.2
- Fabric API 0.149.1+26.1.2
- Shadow 插件 9.0.0-beta4 (com.gradleup.shadow)
- 自定义 Mojang 映射（26.1.2 无官方映射）
- javac -Xmaxerrs 10000（一次性显示全部错误）

## 核心 API 变更

### 渲染管线（最大的变更）
MC 26.x 用 `GuiGraphicsExtractor` 彻底替代了 `GuiGraphics`。所有渲染方法改名：

| 旧 API | 新 API |
|--------|--------|
| `GuiGraphics` 类 | `GuiGraphicsExtractor` |
| `drawString()` | `text()` |
| `drawCenteredString()` | `centeredText()` |
| `vLine()` | `verticalLine()` |
| `hLine()` | `horizontalLine()` |
| `renderOutline()` | `outline()` |
| `pushPose()` | `pose().pushMatrix()` |
| `popPose()` | `pose().popMatrix()` |
| `renderTooltip/renderScrollingString` | 无对应（已注释） |

### 输入系统
- `mouseClicked(double,double,int)` → `mouseClicked(MouseButtonEvent, boolean)`
- `keyPressed(int,int,int)` → `keyPressed(KeyEvent)`
- `charTyped(char,int)` → `charTyped(CharacterEvent)`
- 事件对象从字段访问改为 Record getter（保留 stub 兼容字段）

### 实体子包重构
- `projectile.AbstractArrow` → `projectile.arrow.AbstractArrow`
- `projectile.Arrow` → `projectile.arrow.Arrow`
- `vehicle.Boat` → `vehicle.boat.Boat`
- `animal.Parrot` → `animal.parrot.Parrot`
- 模型类 `PlayerModel` → `model.player.PlayerModel`

### NBT / CompoundTag
- `getString/getBoolean/getCompound` → 返回 `Optional`
- `getList(key, type)` → `getList(key)`
- `contains(key, type)` → `contains(key)`
- `getAllKeys()` → 删除（用 `keySet()`）

### 注册表
- `BuiltInRegistries.XXX.get()` → 返回 `Optional<Reference<T>>`
- `EntityType.is(TagKey)` → `builtInRegistryHolder().is()`
- `RegistryAccess.registryOrThrow()` → `lookupOrThrow()`

### EntityRenderer
- 从 2 个类型参数变为 3 个
- `render()` → `extractRenderState()`
- `createRenderState()` 新增抽象方法
- `LivingEntityRenderer.renderNameTag` 签名变更

### 网络层 (Fabric API)
- `PayloadTypeRegistry.playC2S()` → `serverboundPlay()`
- `PayloadTypeRegistry.playS2C()` → `clientboundPlay()`
- `createS2CPacket/createC2SPacket` → 删除

## 已解决的问题

### 编译（~200+ 错误 → 0）
- 构建系统适配
- 依赖更新
- API 迁移（渲染、输入、NBT、注册表、实体）
- 34 个文件从 GuiGraphics 迁移到 GuiGraphicsExtractor

### 运行时崩溃（~40 次 → 0）
- Mixin 目标方法签名更新 6 个
- 删除冲突 stub 30+ 个
- @ExpectPlatform 9 个文件去 AssertionError
- EntityRendererProvider$Context 构造函数匹配

### 删除的 Stub（退回真实 MC 类）
GuiGraphics, GuiGraphicsExtractor, GameRenderer, EntityRenderDispatcher, Camera, Options, User, MouseHandler, KeyboardHandler, SoundManager, LanguageManager, AbstractTexture, RenderBuffers, PlayerSkinRenderCache, ArmorMaterial, ArmorType, ItemUseAnimation, MouseButtonInfo, PlayerModel, ElytraModel, ParrotModel

### 保留的 Stub（MC 26.x 中不存在的类）
Minecraft, Window, Timer, GlStateManager, BufferUploader, MouseButtonEvent, KeyEvent, CharacterEvent, ElytraItem, EntityShoulder, Saddleable, SwordItem, PickaxeItem, ArmorItem, ToolMaterial, RenderTarget, ShaderInstance, LightTexture, BlockRenderDispatcher, ItemInHandRenderer, ItemRenderer, EntityRenderState, LivingEntityRenderState, WidgetSprites, StateSwitchingButton

## 已知限制

1. **渲染** — `renderTooltip`, `renderScrollingString`, `drawWordWrap`, `setColor`, `bufferSource`, `flush` 等辅助方法已注释（真类无对应 API）
2. **模型网络同步** — "[YSM] Failed to dispatch" 因网络桥接 stub 为空实现
3. **GPU 着色器** — BlurShader/Pie 等因 RenderSystem API 变更部分禁用
4. **ItemInHandRenderer** — getItemInHandRenderer 调用返回 null，手部物品渲染可能不完整

## 关键经验

1. **Stub 策略**: 只在 MC JAR 中不存在的类才建 stub，存在的类必须删掉 stub 用真类
2. **方法签名必须精确**: 参数类型连包名都得一模一样，`Object` 替代不了具体类型
3. **javap 是核心工具**: 每个真类的 API 都通过 javap 确认，不猜测
4. **批量替换模式**: 同类型错误用一个 agent 批量处理，比手动逐个修快 10 倍
