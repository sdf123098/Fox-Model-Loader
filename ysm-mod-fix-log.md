# Yes Steve Model / fox-model-loader 26.1.2 修复记录

> 更新时间：2026-05-29 | 整体状态：全部完成 | 产物：`fabric/build/libs/fox-model-loader-fabric-1.0.3.jar` | JDK 25 + Gradle 9.5.1

## 阶段摘要 (13 项，均已完成)

| 日期 | 主题 | 要点 |
|------|------|------|
| 05-21 | MC 26 基础迁移 | 网络、渲染、tick/命令、第一人称手臂、滑翔组件适配 |
| 05-24 | GUI/动画/同步 | 动画轮盘、GUI 预览、HUD 字体、Mixin 签名、LAN 同步 |
| 05-25 | 模型面板/交互 | 导入入口、手持预览、元数据、投射物同步、tooltip、配置保存 |
| 05-25 | 物品/姿态 | 三叉戟/矛分流、剑/弓/船动画回归 |
| 05-26 | GPU direct GL | 修复崩溃/矩阵错位/空 mesh/贴图绑定；仍为实验路径 |
| 05-26 | 船/竹筏/箱船 | 禁用 YSM 对 `AbstractBoat` 载具替换，保留 Molang 变量 |
| 05-28 | Android 模型面板 | 修复内置/杂项/导入模型不可见 (zstd-jni so 加载失败) |
| 05-29 | HeadlessException | Android 跳过 AWT/Swing 文件选择器 |
| 05-29 | FCL 目录桥接 | 接入 `CallbackBridge.nativeClipboard(2002, ...)` 目录面板 fallback |
| 05-29 | Zalith 2 目录桥接 | 同上；修复取消后选择器状态残留 |
| 05-29 | .ysm 选择器/桌面 | 多选、tinyfd 桌面选择器、取消状态释放、暂存清理 |
| 05-29 | 导入/工具/鞘翅 | 导入扩展 .ysm/.zip/文件夹；工具动作拆分 pickaxe/spade/hoe/axe；默认模型跳过 YSM 鞘翅 |

## 关键结论

- **ZSTD**：Android 不依赖 native，走 raw ZSTD frame + Java fallback；远端 `openysm.cpp` 仅作参考。
- **GPU**：GUI/第一人称/手持优先回退普通流程；世界渲染用 `Camera.getViewRotationProjectionMatrix()`；实验功能，异常时确认 fallback。
- **动画/载具**：三叉戟优先 `trident`→回退 `spear`；矛优先 `lance`；工具分类先查 `ItemTags`；`AbstractBoat` 不再替换。
- **导入链路**：客户端→服务端校验 (id/size/sha256)→写入 `custom/`→`loadModels()` 重扫。支持 `.ysm`、`.zip`、文件夹；`.7z` 跳过。

## FML 武器动作进度

- 2026-05-29 已保存到 `D:\OYSM\FML-长矛-三叉戟-重锤动作彻底修复计划书.md`：除“第一人称”和“长矛第三人称动作适配”外，其余项目标记为已完成。
- 2026-05-29 第一人称评估已封存：当前第一人称走独立 `fp.arm`/手臂渲染链路，只渲染左右手臂 mesh；第三人称手持武器依赖 `CustomPlayerItemInHandLayer` 按手骨/locator 挂载物品，第一人称未接入该层。无需改代码时，只能通过 `fp_arm` 资源补手臂动作，或依赖真实第一人称/Real Camera 类外部方案复用第三人称模型；“第一人称完全像第三人称一样显示武器和动作”暂不处理。

## Android 修复要点

- **内置模型不可见** → zstd-jni so 不兼容 Android bionic；改为 raw ZSTD frame 缓存，跳过 `zstd-jni`，优先 raw-frame 解压再 Java fallback。
- **导入模型不可见** → 沙箱 `Permission denied`；新增客户端上传→服务端写入流程，配置 `AllowModelUpload`/`ModelUploadMaxMiB`。
- **文件选择** → 优先 FCL `ACTION_OPEN_DOCUMENT`→AndroidX `GetMultipleContents`→FCL `FileBrowser`；不可用时走启动器目录桥接 (`nativeClipboard 2002`)；桌面优先 tinyfd→AWT/Swing。
- **HeadlessException** → `isHeadless()` 检查 + Android 跳过 JVM 对话框；无桥时返回 `no_android_picker`。
- **多选** → AndroidX `GetMultipleContents`、`EXTRA_ALLOW_MULTIPLE`、`ClipData`；桌面 AWT `setMultipleMode`/Swing `setMultiSelectionEnabled`；Zalith 2 `OpenFolder.kt` MIME 改为 `*/*`。
- **状态残留** → `pickYsmFile()` 可替换旧请求；`removed()` 调 `cancelPicking()`；暂存目录打开前清理 + 读取后删除。

## Zalith 2 APK

- `:ZalithLauncher:assembleDebug` 成功 (`GRADLE_USER_HOME=D:\Tools\GradleHome`)
- 产物：`ZalithLauncher-Debug-2.4.4.apk` (344 MB, arm64/x86 多架构)
- SHA256: `0A1DEEC2285C0958246E72BDC694ACED84766039FFB3003DA49E75F35A8723AE`

## 使用建议

- Android 换 jar 后清理 `config/yes_steve_model/cache/server` 和 `cache/client`。
- Android 优先用导入界面 `Choose .ysm File(s)`；桌面可拖拽导入。
- 不要手动复制已报 `Permission denied` 的 `.ysm` 文件。
- GPU 异常时优先确认 fallback 是否生效；Zalith 2 目录面板 Import 需新 APK。
