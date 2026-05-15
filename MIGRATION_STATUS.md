# OpenYSM → Minecraft 1.21.1 Fabric 迁移

## 当前状态

`./gradlew build` ✅ | 单人世界 ✅ | 动画测试中

---

## 迁移 Checklist（后续版本升级按此顺序执行）

### 阶段 A — 构建系统

- [ ] **A1** 更新 `gradle.properties`
  - `minecraft_version`
  - `architectury_version`
  - `fabric_api_version`
  - `cardinal_components_version`
  - `forge_config_api_port_version`
  - `java_version`（MC 1.21+ 需要 Java 21）
- [ ] **A2** 更新 `build.gradle`
  - Loom 插件版本
  - Java targetCompatibility / sourceCompatibility
- [ ] **A3** 更新 `fabric/build.gradle`
  - CCA maven group（`dev.onyxstudios` → `org.ladysnake`）
  - Iris / Sodium 兼容依赖版本
- [ ] **A4** 更新 `fabric.mod.json`
  - `minecraft` 版本范围
  - `java` 版本要求
  - 添加/更新 `depends` 中 `fabric-api` 声明
- [ ] **A5** 更新 `gradle/wrapper/gradle-wrapper.properties` — Gradle 版本
- [ ] **A6** 更新 2× `mixins.json`
  - `compatibility_level`（如 `JAVA_17` → `JAVA_21`）
  - **确保 `refmap` 字段存在**（Loom 1.4+ 强制要求）
- [ ] **A7** `./gradlew build`，看 common:compileJava 报错数量

### 阶段 B — 泛型与核心 API（compileJava 报错最多的部分）

- [ ] **B1** `Holder<T>` 迁移 — MC 1.19.3+ 将注册项包装为 Holder
  - `MobEffect` → `Holder<MobEffect>`
  - `Enchantment` → `Holder<Enchantment>`
  - `Attribute` → `Holder<Attribute>`
  - 搜索方法：`grep "MobEffect[^.]"` 找出未迁移的引用
- [ ] **B2** `ResourceLocation` 构造 — MC 1.21+ 废弃构造函数
  - `new ResourceLocation(a, b)` → `ResourceLocation.fromNamespaceAndPath(a, b)`
  - `new ResourceLocation(s)` → `ResourceLocation.parse(s)` 或 `withDefaultNamespace(s)`
  - `isValidResourceLocation` → `isValidPath`
  - 搜索方法：`grep "new ResourceLocation("`
- [ ] **B3** NBT / DataComponent — MC 1.20.5+ 物品系统重写
  - `CompoundTag` → `DataComponent`
  - `ItemStack.getTag()` → `get(...)`
  - CCA 组件：`writeToNbt` / `readFromNbt` 新增 `HolderLookup.Provider` 参数
- [ ] **B4** Registry 访问方式
  - `readById(Registry)` → `readById(Registry::byId)`
  - `ParticleArgument.readParticle` → 新增 `HolderLookup.Provider` 参数
- [ ] **B5** 实体/玩家 API
  - `AbstractClientPlayer` cape/elytra → `PlayerSkin` record
  - `getUser().getGameProfile()` → `getGameProfile()` 直接调用
  - `DisplaySlot.PLAYER_LIST` → `DisplaySlot.LIST`
  - `Score.getScore()` → `get()`

### 阶段 C — 渲染 API

- [ ] **C1** Tesselator — MC 1.21 重写
  - `Tesselator.getInstance().getBuilder()` → `Tesselator.getInstance().begin(...)` 直接返回 BufferBuilder
  - `vertex(x,y,z)` → `addVertex(buffer.putFloat(x).putFloat(y).putFloat(z))`
  - `endVertex()` → 移除（`addVertex` 已隐含)
  - `Tesselator.getInstance().end()` → `BufferUploader.drawWithShader(builder.buildOrThrow())`
  - 模式变枚举：`DrawMode.QUADS`, `DrawMode.TRIANGLE_STRIP` 等
- [ ] **C2** RenderSystem / Matrix4f
  - `RenderSystem.getModelViewStack()` → 返回 `org.joml.Matrix4fStack`
  - `pushPose()` → `pushMatrix()` / `popPose()` → `popMatrix()`
  - `RenderSystem.assertOnGameThread()` → 移除（或改为 `assertOnRenderThread()`）
- [ ] **C3** 帧时间 — **最容易遗漏，运行时动画卡顿的常见原因**
  - `Minecraft.getFrameTime()` 已移除
  - 渲染方法中：用 `partialTick` 参数（screen.render / entityRenderer.render）
  - 无参数时：用 `Minecraft.getInstance().getTimer().getGameTimeDeltaTicks()`
  - **不要** 用 `getFrameTimeNs() / 1.0E9f`（那是总运行秒数，不是 partial tick）
  - 搜索方法：`grep "getFrameTimeNs\|getFrameTime("`
- [ ] **C4** GuiGraphics / UI
  - `renderBackground(g)` → `renderTransparentBackground(g)`
  - `Checkbox` 构造函数 → `Checkbox.builder(Component, Font)` Builder 模式
  - `EditBox.moveCursorToEnd()` → `moveCursorToEnd(false)`
  - `EditBox.tick()` → 移除
  - `mouseScrolled(x, y, d)` → `mouseScrolled(x, y, dX, dY)`
  - `StateSwitchingButton.initTextureValues` → `WidgetSprites`
- [ ] **C5** Entity Rendering
  - `renderEntityInInventory` 签名变更（1.21 新增 `Vector3f`, `Quaternionf` 参数）
  - `LivingEntityRenderer.setupRotations` — 新增 `float scale` 参数
  - `renderNameTag` — 新增 `float partialTick` 参数
  - `renderToBuffer` — packed color `int` 替代 RGBA `float`
  - `getPassengersRidingOffset/getMyRidingOffset` → mixin 访问
- [ ] **C6** 方块/槽位/方向
  - `Blocks.GRASS` → `Blocks.SHORT_GRASS`
  - `EquipmentSlot.Type.ARMOR` → `EquipmentSlot.isArmor()`
  - `Direction.get2DDataValue()` → 判断逻辑适配
- [ ] **C7** 音频
  - `OggAudioStream` → LWJGL STBVorbis

### 阶段 D — 网络层

- [ ] **D1** 实现 `CustomPacketPayload` 接口（MC 1.20.5+ 要求）
  - 新建 `YSMPayload.java` 包装类
  - 实现 `type()` 返回 `CustomPacketPayload.Type`
  - 实现 `write(FriendlyByteBuf)` 序列化
- [ ] **D2** 注册与发送
  - 服务端：`PayloadTypeRegistry.playS2C().register(...)` + `ServerPlayNetworking.send()`
  - 客户端：`PayloadTypeRegistry.playC2S().register(...)` + `ClientPlayNetworking.send()`
  - `PacketDistributor` → 新版 API
- [ ] **D3** 编解码
  - `FriendlyByteBuf.readBytes` / `writeBytes` 改为 `readBytes(copy)` / `writeBytes(src)`
  - 注意 buffer 复用问题：每次 send 新建 payload 实例

### 阶段 E — Mixin

- [ ] **E1** 检查所有 `@Inject` 目标方法签名是否与 MC 新版本一致
  - 搜索：`grep -r "@Inject\|@ModifyArg\|@Redirect" common/src --include="*.java"`
- [ ] **E2** 检查所有 `@Shadow` / `@Accessor` 字段/方法可访问性
  - `public` → `protected` / `private` 变化
  - 泛型签名变化（如 `Map<A,B>` → `SequencedMap<A,B>`）
- [ ] **E3** 关注经常变更的注入点：
  - `LivingEntityRenderer.render` — 签名参数变化
  - `LevelRenderer.renderLevel` — MC 1.21 新增 `DeltaTracker` 参数
  - `ServerPlayer.startRiding` — 调用链重构可能导致 `@At` 找不到

### 阶段 F — 配置系统

- [ ] **F1** ForgeConfigApiPort 导入：`api.config.v2` → `fabric.api.forge.v4`
- [ ] **F2** 规避编译依赖：参数类型用 `Object`，运行时 `(IConfigSpec<?>)` 转型

### 阶段 G — 运行时验证

- [ ] **G1** 启动游戏 → 主菜单
- [ ] **G2** 创建/加载单人世界
- [ ] **G3** 验证模组功能：模型显示、动画播放、配置保存
- [ ] **G4** 多玩家场景（如有）
- [ ] **G5** 压力测试：大量实体、长时间运行

---

## 本次迁移参考资料

### 编译错误统计

| 阶段 | 初始错误数 | 说明 |
|------|-----------|------|
| 构建系统升级后 | common 77, fabric ~10 | 一次性编译，按阶段修复 |
| 最终 | 0 | 全部解决 |

### 依赖版本（MC 1.21.1 Fabric）

| 组件 | 版本 | 说明 |
|------|------|------|
| Fabric Loader | ≥ 0.16.10 | |
| Minecraft | 1.21.1 | |
| Java | ≥ 21 | |
| Fabric API | ≥ 0.116.8 | 用户自行安装 |
| Architectury API | 13.0.8 | 内置打包 |
| Cardinal Components API | 6.1.3 | 内置打包 |
| Forge Config API Port | 21.1.6 | 内置打包 |
| Iris | 1.8.8+1.21.1 | 编译依赖 |

### 常用搜索命令（下次迁移用）

```bash
# 找所有 ResourceLocation 构造
grep -rn "new ResourceLocation(" common/src --include="*.java"

# 找所有过时渲染调用
grep -rn "getFrameTime\|getFrameTimeNs\|Tesselator\|RenderSystem" common/src --include="*.java"

# 找所有 Mixin 注入
grep -rn "@Inject\|@ModifyArg\|@Redirect\|@Shadow\|@Accessor" common/src --include="*.java"

# 找 Holder<T> 迁移遗漏
grep -rn "MobEffect[^.]" common/src --include="*.java"
grep -rn "Enchantment[^.]" common/src --include="*.java"
```

### 网络层迁移参考模式

服务端注册与发送：
```java
// 注册
PayloadTypeRegistry.playS2C().register(YSMPayload.TYPE, YSMPayload.STREAM_CODEC);
// 发送
ServerPlayNetworking.send(player, new YSMPayload(encode(packet)));
```

客户端注册与发送：
```java
// 注册
PayloadTypeRegistry.playC2S().register(YSMPayload.TYPE, YSMPayload.STREAM_CODEC);
// 发送
ClientPlayNetworking.send(new YSMPayload(encode(packet)));
```

Payload 实现：
```java
public record YSMPayload(byte[] data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<YSMPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "channel"));
    
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    
    // 关键：用 copy 而非 writeBytes，否则 reader index 不推进
    public YSMPayload(FriendlyByteBuf buf) {
        this(buf.readBytes(buf.readableBytes()).array());
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBytes(this.data);
    }
}
```
