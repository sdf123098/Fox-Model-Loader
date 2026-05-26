# Yes Steve Model (fox-model-loader) MC 26.1.2 适配记录

更新时间：2026-05-26

目标：将 Yes Steve Model / fox-model-loader 适配到 Minecraft 26.1.2 + Fabric，并维持可构建、可运行、可继续回归测试的状态。

## 环境与产物

JDK 25 + Gradle 10 → Minecraft 26.1.2 + Fabric。产物 `dist/fox-model-loader-fabric-1.0.2.jar`，多轮 clean build 均通过。

## 阶段摘要

| 阶段 | 日期 | 摘要 |
|------|------|------|
| 1-5 | 2026-05-21 | 基础平台桥接：网络层、渲染事件、Fabric 生命周期/tick/命令、第一人称手臂渲染适配 MC 26 管线、外部纹理上传、`DataComponents.GLIDER` 替代旧 `ElytraItem`。 |
| 6 | 2026-05-24 | 导入模型动画/Molang 引号修复、Freesia 跨服协议兼容、移除内置酒狐资源。 |
| 7-10 | 2026-05-24 | 第三人称摆手动画只触发一次；模型预览旧方块 API 崩溃；`Saddleable` 引用移除（船/矿车/坐骑）；实体 ID/tag 解析与载具匹配。 |
| 11-16 | 2026-05-24 | `openysm.cpp` 评估（暂不改）；三叉戟/长矛朝向初修；局域网联机模型同步；HUD 注册/GUI 预览/缺失文字；`getFont()` 崩溃；三叉戟 180 度补偿移除。 |
| 17-18 | 2026-05-24 | 异步动画求值修复（`WorldRendererMixin` 注册、世界渲染/第一人称上下文分离）；Mixin `renderLevel` 签名崩溃修复。 |
| 19 | 2026-05-25 | 三叉戟投掷物姿态与落地抖动修复。 |
| 20-35 | 2026-05-25 | 泛用矛/三叉戟蓄力姿态迭代修复（详见下方专题）。 |
| 36-39 | 2026-05-25 | 模型文件夹图标 fallback、额外玩家渲染手持物品、Mod Menu/Catalogue 元数据、投射物模型同步时机修复。 |
| 40-42 | 2026-05-25 | 模组图标与 GUI 图标表拆分、作者页文字透明色修复、模型/作者/按钮 tooltip 恢复。 |
| 43 | 2026-05-25 | 19:05 崩溃包：模型 tooltip 读取旧 `Minecraft.window` 字段导致 `NoSuchFieldError`，改用 `getWindow()`。 |
| 44 | 2026-05-25 | 恢复剑的专属持有/挥动动画：适配 MC 26.1.2 单数 tag 目录 `tags/item`，并用原版 `ItemTags.SWORDS` 等作为分类兜底。 |
| 45 | 2026-05-25 | 配置界面修改重启丢失、拉弓时上半身/腰部大角度旋转修复。 |
| 46 | 2026-05-25 | 纸娃娃配置界面拖拽/裁剪、第三人称划船桨动作、拉弓手臂穿模收敛。 |
| 47 | 2026-05-26 | GPU direct GL 世界渲染矩阵错位与默认模型空绘制回退修复。 |

## 专题：05-24 其他修复汇总

| 问题 | 根因 | 修复 |
|------|------|------|
| Z 键打不开轮盘、GUI 预览不播动画 | `AnimationRouletteKey` 接旧 Screen；`shouldSkipAnimation()` 阻止预览 controller 推进 | 改接 `ModernAnimationRouletteScreen`，移除 `CAN_SWITCH_MODEL` 误拦截，预览实体 `shouldSkipAnimation()` 返回 `false` |
| PiP 崩溃（00:04:58 ClassCastException） | `renderFreeGuiPreview` 无条件转 `IPreviewAnimatable` | 仅 `IPreviewAnimatable` 实例应用 GUI 预览姿态 |
| HUD `getFont()` NoSuchMethodError（22:44） | 调用运行时不存在的 `Minecraft.getFont()` | 改用 `mc.font` 字段，移除本地占位 `getFont()` |
| `LevelRenderer.renderLevel` Mixin apply 崩溃（23:34） | `@Inject` 描述符用旧签名 | 改为 MC 26.1.2 实际签名 |
| 动画低帧/不流畅 | `WorldRendererMixin` 未注册，异步预计算未启用；世界渲染标记与第一人称混用 | 注册 mixin，新增 `worldRenderMode` 分离上下文，复用异步 future |
| 投掷三叉戟姿态异常 & 落地抖动 | `AbstractProjectileRenderer` 缺三叉戟 `xRot+90`；`AbstractArrowEntityMixin` 覆盖原版落地判断 | 补原版姿态，`@Shadow isInGround()` 读原版状态 |

## 专题：05-25 四项回归修复

| 问题 | 根因 | 修复 |
|------|------|------|
| 模型选择界面用户导入文件夹图标显示为紫黑方块 | `TextureManager.getTexture()` 对未注册路径返回缺失纹理，旧逻辑只判断 texture view 有效，误把缺失纹理当成文件夹图标 | `PackIconButton` 仅在模型包确有 `OuterFileTexture` 且已成功加载时使用自定义图标，否则回退 `default_pack_icon.png` |
| “额外玩家渲染”纸娃娃不显示当前手持物品 | 自定义 GUI/PiP 预览绕过 MC 26 的 `FeatureRenderDispatcher` / `SubmitNodeCollector`，`ItemInHandRenderer` 提交的手持物品没有被渲染 | `GuiEntityRendererMixin` 将 SubmitNodeStorage 传入 YSM 预览，预览期间设置 `SubmitRenderContext`，并在取消原版渲染前调用 `renderAllFeatures()` |
| 安装 Mod Menu 或 Catalogue 后“网站”按钮为空，Catalogue 不显示模组图标 | `fabric.mod.json` 的 `contact.homepage` 为空；icon 使用根目录图片路径，对 Catalogue 兼容性不足 | 补 `homepage`/`sources`/`issues` 指向 `sdf123098/Fox-Model-Loader`，将 icon 指向已打包的 `assets/yes_steve_model/texture/mod_icon.png`，并用 `C:\Users\T.H.E Herta\Pictures\iconx.png` 生成该独立模组图标资源 |
| 模型自定义弹射物模型消失，显示原版投射物 | 投射物 owner 设置时可能早于实体加入世界和客户端 tracking；同步包只发给 tracking entity，没有发给发射者自身 | 投射物加入世界后按 owner 再同步一次，并在同步时同时发给发射者与 tracking 玩家 |

## 专题：05-25 GUI 图标、作者页文字与提示框修复

| 问题 | 根因 | 修复 |
|------|------|------|
| 模型选择面板贴图异常，顶部图标显示成异常色块 | `assets/yes_steve_model/texture/icon.png` 同时是 GUI 图标表，先前将其替换为模组图标导致所有按钮从错误贴图采样 | 恢复原 `texture/icon.png` GUI 图标表，新增 `texture/mod_icon.png` 存放 `iconx.png`，`fabric.mod.json` 仅引用 `mod_icon.png` |
| 导入模型作者页面缺少作者文字/占位文字 | 作者卡片用 `ChatFormatting.*.getColor()` 的 RGB 值直接绘制；MC 26 GUI 文本颜色需要 ARGB，缺少 alpha 时文本透明 | `AuthorButton` 绘制作者名、角色和空槽占位时统一补 `0xFF` alpha |
| 鼠标指向模型卡片、作者卡片或部分面板按钮时没有提示框 | 旧版 `GuiGraphicsExtractor.renderTooltip/renderComponentTooltip` 静态调用在迁移时被注释，未替换为 MC 26 的实例 tooltip 提交接口 | 改用 `setComponentTooltipForNextFrame` / `setTooltipForNextFrame`，恢复模型详情、作者联系方式、搜索框、模型包说明、贴图页按钮和动画轮盘说明 tooltip |
| 打开模型选择界面后渲染屏幕崩溃（`minecraft-exported-crash-info-2026-05-25T19-05-18.zip`） | `ModelButton.renderTooltip` 为判断 Shift 读取 `Minecraft.getInstance().window`，该字段运行时为 private，映射下直接字段访问会触发 `NoSuchFieldError` | 改用运行时存在的 `Minecraft.getWindow()`，并使用 `InputConstants.KEY_LSHIFT/RSHIFT` 判断详细 tooltip；扫描确认无其他 `Minecraft.window` 旧字段访问 |

## 专题：05-25 剑专属动画恢复

| 问题 | 根因 | 修复 |
|------|------|------|
| 剑的专属持有/挥动动画无法触发，回落到普通摆手 | MC 26.1.2 原版数据包使用 `data/<namespace>/tags/item/*.json`，旧资源仍在 `tags/items`；`InnerClassify` 仅依赖 YSM 自定义 tag，tag 未加载时 `:sword` 条件匹配不到 | 保留旧 `tags/items`，新增 MC 26 单数目录 `tags/item`；`InnerClassify` 对剑、斧、镐、锹、锄优先检查原版 `ItemTags`，再回退 YSM 自定义 tag，恢复 `hold_*:sword` / `swing:sword` / 默认控制器连段动画 |

## 专题：05-25 配置保存与拉弓躯干回正

| 问题 | 根因 | 修复 |
|------|------|------|
| 更改设置后重启 MC，设置变回未更改状态 | 新配置页 `Option.apply()` 只调用 `ModConfigSpec.ConfigValue#set`，未调用 `save()`；旧额外玩家渲染、免责声明、加载状态位置等入口也有直接 `set` 未写盘 | `Option.ofBoolean/ofDouble/ofEnum` 的 setter 统一 `set + save`；补齐旧入口的 `save()`；GPU 渲染被运行时自动关闭时也同步写盘 |
| 拉弓时上半身/腰部直接旋转约 90 度 | 内置 `use_*:bow` 和 `bow_shoot` 动画在 `UpBody/AllBody` 上保留了 `±64~75` 度 Y 轴扭腰与 `head_yaw` 反向补偿 | 将默认模型与 `4_default_controllers` 中弓使用/射后动画的躯干 Y 轴关键帧归零，保留手臂、手部 locator 和表情关键帧 |

## 专题：05-25 纸娃娃、划船与弓手臂穿模

| 问题 | 根因 | 修复 |
|------|------|------|
| P 打开纸娃娃渲染配置界面后无法移动/缩放 | `ExtraPlayerRenderScreen` 仍使用旧版 `mouseDragged(double, double, int, ...)` 签名，MC 26 不再调用该方法 | 改为 `mouseDragged(MouseButtonEvent, double, double)`，保留左上角移动、右下角缩放和旋转拖拽逻辑，并限制缩放下限避免反向矩形 |
| 纸娃娃超出红框部分被切掉 | 配置界面复用 HUD 纸娃娃渲染路径，`GuiGraphics.entity` 和 fallback scissor 都按红框裁剪 | 为 `ModelPreviewRenderer.renderPlayerOverlay` 增加可选 `clipToFrame` 参数，配置界面传 `false`，渲染边界扩展到当前 GUI 屏幕 |
| 第三人称划船/竹筏时手没有拿桨、也没有划桨动作 | 玩家 `boat` 动画是静态姿态；内置船模型已有 `LeftShovel/RightShovel` 骨骼但动画未绑定原版划桨状态 | 新增 `ysm.boat_*_paddle` 与 `ysm.boat_*_rowing_time` Molang 变量，玩家手臂和船桨骨骼按 `AbstractBoat` 的 paddling state/rowing time 驱动 |
| 部分模型拉弓时手臂穿到身体后面 | 内置弓动画的拉弦侧上臂 yaw 约 `±61` 度，持弓侧前臂弯曲 `-155` 度，对部分骨架过于极端 | 将拉弦侧 yaw 收敛到 `±38` 度，持弓侧前臂收敛到 `-120` 度，并同步 `4_default_controllers` 的射后过渡关键帧 |

## 专题：`openysm.cpp` 评估

远端核心 `dllmain.cpp` + `build.zig` → `ysm-core` 动态库，JNI 签名与本地 `GeoModel.java` 一致。已含多平台 `natives/` 库，不含 CityHash/Zstd/XChaCha 等算法实现。**决策：暂不改源码、GPU 路径和构建链。**

## 专题：泛用矛 / 三叉戟蓄力姿态修复（阶段 20-35）

MC 26.1.2 新增 `ItemUseAnimation.SPEAR`（泛用矛）和 `TRIDENT`（三叉戟），但 YSM 旧 `use_*:spear` 命名实际承载三叉戟/投掷姿态，与 `SPEAR` 语义冲突。经 16 轮迭代，最终方案：

**动画分流：** `TRIDENT` → 显式 `use_*:trident`，无则回退旧 `use_*:spear` 投掷姿态，不走 `SpearAnimations`；`SPEAR` → 优先 `use_*:lance`（内置蓄力/疲劳姿态），无则回落普通 `use_*`，不再抢旧 `spear`。

**物品层：** `SPEAR` 补 Y 轴 180 度 + `SpearAnimations.thirdPersonUseItem(...)`；模型自带 `use_*:spear` 时只做前后轴补偿不叠加原版姿态；钳制 `ticksUsingItem` 在 `maxDurationTicks()` 内防疲劳后方向突变。`TRIDENT` 不加额外 Y 轴翻转。

**主手适配：** 改用 `entity.getMainArm()` 决定渲染到左/右臂，动画 predicate 按实际物理手臂选择 `use_mainhand*` / `use_offhand*`。

**内置 `lance` 动画：** 手部 locator 固定 `[0,0,-90]`/`[0,0,90]` 横持到疲劳段；父级手臂 yaw/roll 从旧侧向（`±40`/`±20`）收敛为前向（`-5.7`/`~0`）；疲劳段 X 轴回稳定段 `-85`；`animation_length` 13→12 停在蓄力帧。

**默认模型 fallback：** `arm.animation.json` 中 `use_*:spear` locator 补 Y 轴 180 度修正；导入模型默认控制器补入 `use_*:spear`。

## 结论与风险

Java/Fabric 侧主要阻塞已处理，项目可构建。旧运行时类引用是主要崩溃来源，后续优先检查本地占位类或旧 API 依赖。GPU renderer 仍为实验路径（MC 26 纹理/shader 迁移未补完），默认关闭。native SIMD 与 `openysm.cpp` 签名匹配，作为可选加速保留。Gradle 10 兼容提示为非阻塞警告。发布前建议同步 dist jar 并做回归测试。

## 专题：2026-05-26 GPU crash
2026-05-26 13:15 的 crash zip 显示，GPU 路径已经进入 `GpuRenderPath.tryRender()`，但在 `refreshLights()` 里直接引用了 `com.elfmcys.yesstevemodel.mixin.client.RenderSystemAccessor`。

这个类只是 mixin 包里的占位接口，Mixin 运行时拒绝直接加载，触发 `IllegalClassLoadError`，最终表现为渲染线程崩溃。

已修复：移除对 `RenderSystemAccessor` 的直接引用，改为 GPU 路径内联稳定默认灯光方向；`./gradlew --no-daemon build` 已通过。

## 专题：2026-05-26 GPU GUI 预览
开 GPU 后，世界内导入模型能出图，但模型选择页、皮肤/衣服页的实体预览区域只剩辅助地面或空按钮，说明 GPU direct GL 路径在 GUI 预览上下文里使用了不匹配的世界相机投影。

已修复：`NativeModelRenderer` 在 `ModelPreviewRenderer.isPreview()` 或 `isExtraPlayer()` 场景下不再走 GPU 直绘，改回原始/SIMD 提交流程；世界渲染和第一人称/手持等非 GUI 预览路径仍可继续尝试 GPU。`./gradlew --no-daemon build` 已通过。

## 专题：2026-05-26 GPU 世界渲染错位
用户截图确认：第一张并非空模型，而是导入模型相对阴影/实体位置明显错位；第二张为默认模型未渲染出来。

根因：GPU shader 只使用 `Camera.projection` 的投影矩阵，没有乘入 MC 26 世界实体渲染实际使用的相机 view rotation；同时 GPU mesh 在当前 `renderPartMask` 下若索引数为 0，仍会返回 `true`，导致默认模型不会回退到原始/SIMD 渲染。

已修复：`GpuRenderPath` 改用 `Camera.getViewRotationProjectionMatrix()` 给 shader，匹配世界实体提交阶段的相机矩阵；`drawCount <= 0` 时立即返回 `false` 触发 fallback；`NativeModelRenderer` 将 GPU direct GL 限制在 `ModelPreviewRenderer.isWorldRender()` 且非预览、非第一人称场景，避免 GUI/手持渲染继续套用世界矩阵。`./gradlew --no-daemon build` 已通过。

补充修复：默认模型仍不显示时，确认不能只禁用默认模型 GPU。`GpuMeshBuilder` 改为保留 native mesh handle 用于骨骼矩阵计算，但顶点/索引缓冲改由 Java 从 `bakedBones` 直接生成，避免 native GPU mesh 生成器在默认模型大量 face-level UV/零面积 UV/复杂 cube 数据上产生不可见 mesh；同时 `GpuRenderPath` 对 `renderPartMask` 的 1/2 手臂渲染改为分别绘制自身段和 Background 段，不再依赖 Background 与左右手索引在 IBO 中连续。`./gradlew --no-daemon build` 已通过，`dist/fox-model-loader-fabric-1.0.2.jar` 已同步。

## 专题：2026-05-26 GPU 默认模型主贴图采样
用户回归确认默认模型在开启 GPU 渲染时仍不显示。继续排查后发现：`GpuRenderPath` 的主模型贴图仍绕过 MC 26 的 `GpuTextureView`，直接从 `AbstractTexture.getTexture()` 取裸 GL id；而 MC 26 的标准 `GlRenderPass` 绑定贴图时会同时应用 `baseMipLevel/mipLevels` 到 `GL_TEXTURE_BASE_LEVEL/MAX_LEVEL`。直接绑定裸纹理可能采样到错误 mip 层，导致 shader 正常 draw 但实际采样为空，并阻止回退路径。

已修复：主模型贴图、overlay、lightmap 全部改为解析 `GpuTextureView`，绑定时同步 texture view 的 mip 范围；若任一视图或 sampler 无效则返回 `false` 走原有 fallback。`./gradlew --no-daemon clean build` 已通过，`dist/fox-model-loader-fabric-1.0.2.jar` 已同步最新构建。
## 涓撻锛?026-05-26 GPU world submit fallback
GPU direct GL 在世界提交上下文里会绕开正常的 `SubmitNodeCollector` 提交流程，导致默认玩家模型看起来像“整个人都没了”。这次补了一个保守回退：当 `SubmitRenderContext` 已经存在时，不再走 GPU 直绘，改回正常几何提交，以先恢复模型和阴影。

## 专题：2026-05-26 船/箱船/竹筏/运输竹筏渲染
用户反馈：兼容模式下四类水上载具被替换成同一个异常模型，且玩家模型压到船下方；GPU 渲染下四类载具消失，玩家仍在消失载具下方。

已修复：`GeckoVehicleEntity` 类型的自定义载具模型不再走 GPU direct GL 直绘，改回普通几何提交流程，避免 GPU 路径返回成功但实际没有可见载具时吞掉渲染；乘客预览偏移移除额外 `-0.5` 修正，改以 MC 26 的 `getPassengerAttachmentPoint` 为准；内置 `boat`、`chest_boat`、`raft`、`chest_raft` 的 `PassengerLocator` 高度同步上调，避免玩家模型落到船/竹筏下方。`./gradlew --no-daemon build` 已通过，`dist/fox-model-loader-fabric-1.0.2.jar` 已同步。

追加决策：继续回归后确认普通船/竹筏仍会按旧兼容 ID 合并成同一个 YSM 载具模型，箱船/运输竹筏也会合并且人物偏低。最终改为对 `AbstractBoat` 系列禁用 YSM 载具替换，让普通船、箱船、竹筏、运输竹筏全部恢复原版模型、材质和原版乘客挂点；矿车及非船类载具仍保留 YSM 载具替换能力。

动作贴合补强：在保留原版船模型的前提下，新增 `ysm.boat_is_chest`、`ysm.boat_is_raft`、`ysm.boat_body_offset_y`、`ysm.boat_body_offset_z`、`ysm.boat_paddle_scale` Molang 变量，默认 `boat` 动画改用这些变量控制人物身体校准和手臂划桨幅度。后续若某类船/竹筏与原版座位或船桨不贴合，可只调 Java 侧校准常量，不再改原版船渲染。
