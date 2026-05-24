# Yes Steve Model (fox-model-loader) MC 26.1.2 适配记录

更新时间：2026-05-24

目标：将 Yes Steve Model / fox-model-loader 适配到 Minecraft 26.1.2 + Fabric，并维持可构建、可运行、可继续回归测试的状态。

## 环境与产物

- JDK：25（`JAVA_HOME=C:\Program Files\Java\jdk-25`）
- Gradle：10（兼容性提示目前非阻塞）
- 目标：Minecraft 26.1.2 + Fabric
- 当前产物：`dist/fox-model-loader-fabric-1.0.2.jar`
- 最近状态：多轮 `.\gradlew.bat clean build` 均通过

## 阶段摘要

| 阶段 | 日期 | 摘要 |
|------|------|------|
| 1 | 2026-05-21 | 修复基础平台桥接：网络层、渲染事件、服务端判断、按键/鼠标事件、ExpectPlatform 占位。 |
| 2 | 2026-05-21 | 接入 Fabric 生命周期、tick、命令、reload listener，并恢复部分服务端同步与 Molang 查询能力。 |
| 3 | 2026-05-21 | 调整第一人称手臂渲染，适配 MC 26 提交式渲染管线。 |
| 4 | 2026-05-21 | 补齐外部纹理上传逻辑，修复 YSM 贴图注册后缺少 `GpuTextureView` 的崩溃。 |
| 5 | 2026-05-21 | 移除旧版 `ElytraItem` 引用，改用 `DataComponents.GLIDER` 判断鞘翅/滑翔装备。 |
| 6 | 2026-05-24 | 修复导入模型动画/Molang 字符串带引号、Freesia 跨服协议兼容，并移除内置酒狐资源。 |
| 7 | 2026-05-24 | 修复第三人称左键/右键摆手动画只触发一次的问题。 |
| 8 | 2026-05-24 | 移除模型预览界面对旧方块渲染 API 的调用，避免地面/床预览崩溃。 |
| 9 | 2026-05-24 | 移除旧版 `Saddleable` 引用，修复船、矿车、坐骑乘坐时的运行时崩溃。 |
| 10 | 2026-05-24 | 修复实体 ID/tag 解析与载具匹配，覆盖骷髅马、鹦鹉螺、僵尸鹦鹉螺等特殊载具。 |
| 11 | 2026-05-24 | 只读评估 `OpenYSMDev/openysm.cpp`，确认当前项目已基本匹配 native SIMD ABI，暂不改源码。 |
| 12 | 2026-05-24 | 修复第三人称长按右键使用长矛/三叉戟时矛尖朝后的问题，调整内置默认 `use_mainhand:spear` / `use_offhand:spear` 手部挂点旋转。 |
| 13 | 2026-05-24 | 修复局域网联机中客机只能看到自己模组模型的问题：服务端按观察者补发被跟踪玩家的完整模型/贴图状态。 |
| 14 | 2026-05-24 | 修复 Fabric 26 HUD 注册、GUI 当前模型预览队列、三叉戟长按右键物品朝向补偿，以及模型文件夹/作者卡片缺失文字。 |
| 15 | 2026-05-24 | 根据 HMCL 导出的 22:44:53 崩溃包，修复 Fabric HUD overlay 调用运行时不存在的 `Minecraft.getFont()`，改用 MC 26 仍存在的 `Minecraft.font` 字段，并移除本地占位方法。 |
| 16 | 2026-05-24 | 根据截图反馈，移除三叉戟长按右键使用时额外绕 Y 轴 180 度的运行时补偿，避免三叉戟头前后完全反向。 |
| 17 | 2026-05-24 | 修复世界渲染异步动画求值与第一人称标记混用的问题，恢复动画 controller 正常推进，并注册 `WorldRendererMixin` 启用每帧预计算。 |
| 18 | 2026-05-24 | 根据 HMCL 导出的 23:34:41 崩溃包，修复 `WorldRendererMixin` 仍使用旧 `LevelRenderer.renderLevel` 方法签名导致的启动期 Mixin apply 崩溃。 |

## 当前结论

- Java/Fabric 侧主要阻塞已经处理，项目当前可以构建。
- 旧运行时类引用是 MC 26 迁移中的主要崩溃来源，后续遇到崩溃优先检查是否仍依赖本地占位类或旧 Mojang API。
- GPU 直连 GL 路径尚未完成 MC 26 迁移，当前默认关闭是合理状态。
- native SIMD 渲染路径与 `openysm.cpp` 的 JNI 签名基本匹配，但建议继续作为可选加速，不作为默认稳定路径。

## 2026-05-24 22:44 崩溃包记录

来源：`D:\Games\Minecraft-HMCL\minecraft-exported-crash-info-2026-05-24T22-44-53.zip`

结论：

- 直接崩溃为 `java.lang.NoSuchMethodError: 'net.minecraft.client.gui.Font net.minecraft.client.Minecraft.getFont()'`。
- 调用点为 `fabric/src/main/java/com/elfmcys/yesstevemodel/fabric/client/YesSteveModelFabricClient.java:26`，发生在 Fabric HUD element 提取渲染状态时。
- `latest.log` / `minecraft.log` 里 Realms 401 与 refmap 警告不是本次崩溃原因；HMCL 也归因为 `NO_SUCH_METHOD_ERROR`。

修复：

- `YesSteveModelFabricClient` 改为缓存 `Font font = mc.font` 后传给三个 HUD overlay。
- 移除 `common/src/main/java/net/minecraft/client/Minecraft.java` 本地占位类中的 `getFont()`，避免再次编译出运行时不存在的方法调用。
- 已使用 `JAVA_HOME=C:\Program Files\Java\jdk-25` 执行 `.\gradlew.bat clean build`，构建通过，并同步 `dist/fox-model-loader-fabric-1.0.2.jar`。

## 2026-05-24 三叉戟长按右键朝向记录

来源：截图 `屏幕截图(525).png`，第三人称长按右键使用三叉戟时，三叉戟头朝向玩家身后，前后完全相反。

结论：

- `CustomPlayerItemInHandLayer` 中的 `applyTridentUseOrientation()` 会在使用原版三叉戟时额外执行 `Axis.YP.rotationDegrees(180.0f)`。
- 内置 `use_mainhand:spear` / `use_offhand:spear` 动画已经通过手部 locator 控制持矛姿态和位移，这个额外 180 度补偿会直接翻转物品前后轴。

修复：

- 删除 `applyTridentUseOrientation()` 及其调用，让三叉戟使用已有手部挂点变换渲染。
- 已使用 `JAVA_HOME=C:\Program Files\Java\jdk-25` 执行 `.\gradlew.bat clean build`，构建通过，并同步 `dist/fox-model-loader-fabric-1.0.2.jar`。

## 2026-05-24 动画低帧/不流畅修复记录

结论：

- `WorldRendererMixin` 已存在但未注册到 common mixin 配置，导致 `EntityRenderCache` 的每帧异步动画预计算没有实际启用。
- 旧实现把世界渲染阶段复用为 `firstPersonMode`，并且 `GeoEntity.submitAsyncUpdate()` 调用 `processAnimationImpl(partialTick, true)`，会让 `CustomPlayerEntity.shouldSkipAnimation()` 把第三人称模型误判为第一人称，从而跳过 controller 推进。
- 渲染线程在拿到异步结果后原本仍会重新同步求值，复杂模型和多人场景下会放大 CPU 压力。

修复：

- 新增 `ModelPreviewRenderer` 的 `worldRenderMode`，让世界渲染上下文和真正第一人称动画上下文分离。
- `WorldRendererMixin` 改为设置 `worldRenderMode`，并注册到 `yes_steve_model.mixins.json`。
- `GeoEntity.submitAsyncUpdate()` 改为按第三人称语义预计算动画，`GeoEntity.processAnimationImpl()` 在已有 future 时直接复用异步结果。
- `HandItemRenderer` 明确以第一人称语义处理手臂动画，避免依赖世界渲染标记。
- 已使用 `JAVA_HOME=C:\Program Files\Java\jdk-25` 执行 `.\gradlew.bat clean build`，构建通过，并同步 `dist/fox-model-loader-fabric-1.0.2.jar`。

## 2026-05-24 23:34:41 崩溃包记录

来源：`D:\Games\Minecraft-HMCL\minecraft-exported-crash-info-2026-05-24T23-34-41.zip`

结论：

- 直接崩溃为 `java.lang.RuntimeException: Mixin transformation of net.minecraft.client.renderer.LevelRenderer failed`。
- 根因是 `yes_steve_model.mixins.json:client.WorldRendererMixin` 的 `@Inject renderLevelPre(...)` 描述符仍是旧版 `LevelRenderer.renderLevel` 参数。
- MC 26.1.2 实际期望签名为 `GraphicsResourceAllocator, DeltaTracker, boolean, CameraRenderState, Matrix4fc, GpuBufferSlice, Vector4f, boolean, ChunkSectionsToRender, CallbackInfo`。

修复：

- 将 `WorldRendererMixin.renderLevelPre` / `renderLevelPost` 参数改为 MC 26.1.2 的 `renderLevel` 签名。
- 已用 `javap -p -s` 确认生成 class 的 descriptor 与崩溃日志 Expected descriptor 一致。
- 已使用 `JAVA_HOME=C:\Program Files\Java\jdk-25` 执行 `.\gradlew.bat clean build`，构建通过，并同步 `dist/fox-model-loader-fabric-1.0.2.jar`。

## `openysm.cpp` 评估记录

只读分析结果：

- 远端仓库核心是 `dllmain.cpp` + `build.zig`，输出 `ysm-core` 动态库。
- 远端 JNI 绑定 `com/elfmcys/yesstevemodel/geckolib3/geo/render/built/GeoModel`。
- 本地 `GeoModel.java` 的 native 方法签名与远端 `gMethods` 一致。
- 本地已包含 `natives/windows-x64`、`windows-x86`、`linux-x64`、`macos-x64`、`macos-arm64`、`android-arm64` 对应库。
- `openysm.cpp` 不包含 `YSMParserJNI` / `YSMNative` 的解析、CityHash、Zstd、XChaCha 等算法 native 实现。

决策：

- 暂时不改源码。
- 暂不调整 GPU 路径。
- 暂不改 native 构建链；如后续要纳入构建，应从 Zig 构建流程入手。

## 待测项

- [ ] 第一人称手臂渲染
- [ ] 地图渲染
- [ ] F5 视角切换
- [ ] 生存模式物品栏
- [ ] 多人联机模型同步（已修复局域网追踪补发逻辑，仍需实机回归）
- [ ] 文件夹 / zip / ysm 模型导入后的动画、额外动作、controller 状态切换
- [ ] Velocity 3.4+ FreesiaII 跨服进入、切服、模型同步、轮盘动作播放
- [ ] 船、矿车、普通坐骑、生物坐骑、特殊载具的乘坐动画与模型匹配
- [ ] 骷髅马、鹦鹉螺、僵尸鹦鹉螺等命名空间 ID 载具识别

## 剩余风险

- GPU renderer 仍是实验路径，MC 26 纹理绑定、light texture、overlay texture、shader/RenderType 迁移还未补完。
- native SIMD 依赖平台库与 CPU 指令集，老 CPU 可能加载失败并回退 Java 渲染。
- Java native access、Unsafe API、Gradle 10 兼容提示目前为非阻塞警告。
- 发布前建议统一同步最新 dist jar 到目标启动器实例，并按待测项做回归。

## 2026-05-24 23:59 轮盘与预览动画修复记录

来源：截图 `屏幕截图(526).png` 与实机反馈：`Z` 键打不开轮盘动画；材质/动画预览界面选中动画项后预览模型不播放动画。

结论：
- `AnimationRouletteKey` 仍打开旧版 `AnimationRouletteScreen`，没有接到已迁移的 `ModernAnimationRouletteScreen`。
- 轮盘入口被 `ServerConfig.CAN_SWITCH_MODEL` 误限制；播放额外动画不应被“是否允许切换模型”拦截。
- `PlayerPreviewEntity.shouldSkipAnimation()` 返回 `true`，导致 GUI 预览实体跳过 animation controller 的 `process()`，左侧动画列表只会改变选中状态，不会推进预览动画。

修复：
- `AnimationRouletteKey` 改为打开/关闭 `ModernAnimationRouletteScreen`，并移除 `CAN_SWITCH_MODEL` 对轮盘动画入口的限制。
- 暂停菜单轮盘按钮同步改为打开 `ModernAnimationRouletteScreen`。
- `PlayerPreviewEntity.shouldSkipAnimation()` 改为返回 `false`，允许预览实体运行专用 GUI 动画 controller。
- 已使用 `JAVA_HOME=C:\Program Files\Java\jdk-25` 执行 `./gradlew.bat clean build`，构建通过，并同步 `dist/fox-model-loader-fabric-1.0.2.jar`。

## 2026-05-25 00:04:58 崩溃包记录

来源：`D:\Games\Minecraft-HMCL\minecraft-exported-crash-info-2026-05-25T00-04-58.zip`

结论：
- 直接崩溃为 `java.lang.ClassCastException: PlayerCapability cannot be cast to IPreviewAnimatable`。
- 调用点为 `ModelPreviewRenderer.renderFreeGuiPreview(ModelPreviewRenderer.java:341)`，发生在 MC 26 GUI Picture-in-Picture 渲染预览阶段。
- 根因是 `renderEntityPreview(...)` 可接收普通 `AnimatableEntity`，但内部无条件把实体转成 `IPreviewAnimatable` 来读取 GUI 预览动画状态；轮盘齿轮设置界面传入的是真实玩家 `PlayerCapability`，不是 `PlayerPreviewEntity`。
- 日志中的 Realms 401 与 refmap 警告不是本次崩溃原因。

修复：
- `ModelPreviewRenderer` 新增安全读取预览动画状态的分支，只有实现 `IPreviewAnimatable` 的预览实体才应用 sleep/swim/sneak/ride/boat 等 GUI 预览姿态。
- 普通 `PlayerCapability` 走同一 PIP 渲染路径时不再强转，只渲染模型本体，避免设置界面打开即崩溃。
- 已使用 `JAVA_HOME=C:\Program Files\Java\jdk-25` 执行 `.\gradlew.bat clean build`，构建通过。
- 已同步 `dist/fox-model-loader-fabric-1.0.2.jar`，并更新 `D:\Games\Minecraft-HMCL\.minecraft\versions\26.1.2-Fabric\mods\fox-model-loader-fabric-1.0.2.jar`。
