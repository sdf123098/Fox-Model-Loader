# Fox Model Loader: Revival

<img src="src/main/resources/foxmodelloader.png" alt="Fox Model Loader: Revival" width="160">

Fox Model Loader: Revival follows the Sparkle Morpher **1.2.9** lineage. Its Mod ID and resource namespace are `foxmodelloader`. It retains the traditional Minecraft server synchronization architecture; multiplayer model sharing requires the mod on both server and clients. Individual model IDs and the `.ysm` format are preserved.

Official Chinese name: **绯绯狐的模型加载器：复兴**.

> **English** | [中文](README_zh.md) | [日本語](README_ja.md) | [한국어](README_ko.md)

**QQ:** 1104823534 | **Discord:** [Join Discord](https://discord.gg/3KqK7USF39) | **Patreon:** [cw/Soid211](https://www.patreon.com/cw/Soid211) | **Afdian:** [Micaftic](https://afdian.com/a/Micaftic)

A comprehensive Minecraft custom model loader that lets players mount custom models, animations, and sound effects onto players (and select entities, vehicles, and projectiles) — say goodbye to the default blocky character.

> Fox Model Loader: Revival is a **universal model loader**. It currently supports the `.ysm` format (based on OpenYSM, MIT licensed) and `.bbmodel` format (Blockbench), with support for additional mainstream model formats planned for future releases.

## Release Policy

The project follows a **quarterly LTS-style release cycle**: one planned feature release each quarter. Between quarterly releases, maintenance releases may address critical bugs, compatibility problems, or security issues.

This cadence is deliberate. Stability, compatibility, and predictable server deployments take priority over frequent feature releases.

See [Releases](https://github.com/sdf123098/Fox-Model-Loader/releases) for published versions, release notes, and changelogs.

## Project History

Fox Model Loader originally evolved into Sparkle Morpher. The development paths diverged after Sparkle Morpher **1.2.9**. Beginning with **2.0**, Fox Model Loader resumed maintenance under the **Revival** identity, continuing the traditional Minecraft server synchronization architecture.

## Features

### Custom Player Models & Skins

Replace vanilla player models with fully custom 3D models. All custom models are **visible to other players in multiplayer**

### Model Format Support

- **`.ysm`** — The native format powered by OpenYSM/YSMParser, supporting full skeletal models with weighted animations.
- **`.bbmodel`** — Direct import of Blockbench project files. Includes mesh triangulation (N-gon fan triangulation), UV normalization, face rotation, inflate expansion, embedded base64 texture extraction, and PNG IHDR header parsing.
- **Figura Avatar Archives** — Import Figura `.zip` packages directly. The built-in `ZipModelSniffer` automatically detects and routes YSM folders, Figura avatars, and plain BBModel zips.

### Animation System

- **Animation Carousel** (default key: Z) — A radial menu to quickly switch between animations and actions for the current model.
- **Animation Controllers** — Full support for state-machine-based animation controllers with `loop`, `once`, and `hold` playback modes.
- **Molang Expressions** — Data points support both raw numeric values and Molang expression strings for dynamic animation blending.

### Sound Effects

Play model-bundled voice lines and sound effects triggered by skills or actions. Audio decoding uses **Opus** with the bundled Java Concentus decoder.

### Multi-model Management

- Import models from local files, directories, or URLs with accelerated downloading.
- Organize models with grouping and favorites.
- Automatic directory scanning recognizes `.ysm`, `.zip`, and `.bbmodel` files.

### Server-side Features

- Server operators can define model manifests and push models to clients.
- A configurable blacklist (`config/foxmodelloader/blacklist.txt`) lets servers restrict specific models.
- Client-server model state synchronization via Cardinal Components entity data.

### Server Bandwidth Limit

Server operators can configure the global model-transfer limiter in `config/foxmodelloader-server.toml`:

```toml
[server_scheduler]
EnableGlobalBandwidthLimit = false
BandwidthLimit = 5
```

`BandwidthLimit` is in Mbps. When `EnableGlobalBandwidthLimit` is enabled, the limit is shared globally by server-to-client model sync packets and client-to-server model upload chunks. Small control packets, including favorite sync, are not throttled.

### Mod Compatibility

Compatibility depends on the Minecraft version, loader and installed mod release. Current adapter status:

| Mod | Current status |
| --- | --- |
| Carpet / fake-player providers | Class-based fake-player recognition and server-authoritative model selection; individual forks need matching-version validation. |
| Sodium | Uses Minecraft rendering paths without Sodium-specific mixin targets; source audit does not certify every rendering combination. |
| Iris | Optional shader-pack and shadow-pass detection. Fabric 26.x uses a reflective API bridge and does not bundle Iris API classes. |
| ParCool | NeoForge has an action adapter for compatible `Parkourability` APIs; Fabric retains an inactive placeholder. |
| Better Combat, Create, Curios and other historical adapters | Several inherited adapters remain placeholders; their class names do not imply full integration. |

See [compatibility scope and verification](MOD_ID_COMPATIBILITY.md) for loader differences, the Mod ID migration and testing limits. Installing two mods together and supporting their special animations or item slots are separate compatibility claims.

### Current release targets

Current release targets cover Fabric and NeoForge on Minecraft 1.21.1, 26.1.2 and 26.2. The supported versions and downloadable builds are listed in [Releases](https://github.com/sdf123098/Fox-Model-Loader/releases).

| Variant | Loader | Minecraft |
| --- | --- | --- |
| Fox-Model-Loader-Fa1.21.1 | Fabric | 1.21.1 |
| Fox-Model-Loader-Fa26.1.2 | Fabric | 26.1.2 |
| Fox-Model-Loader-Fa26.2 | Fabric | 26.2 |
| Fox-Model-Loader-Neo1.21.1 | NeoForge | 1.21.1 |
| Fox-Model-Loader-Neo26.1.2 | NeoForge | 26.1.2 |
| Fox-Model-Loader-Neo26.2 | NeoForge | 26.2 |

## How It Works

### Model Import Pipeline

When you import a model file, Fox Model Loader: Revival runs it through an intelligent pipeline:

1. **Zip Sniffing** — Archives are classified by content: YSM folder, Figura avatar (contains `avatar.json` + `.bbmodel`), plain BBModel zip, or unknown.
2. **Parsing** — `.ysm` files go through YSMParser; `.bbmodel` files are parsed by the built-in `BBModelParser` which handles outliner trees, cube/mesh elements, textures, animations, and controller states.
3. **Conversion** — Parsed data is converted to the engine's internal `RawGeometry` format. Mesh faces with N vertices are triangulated via fan triangulation; UV coordinates are normalized against texture resolution; external PNG textures in zip archives override embedded base64 sources.
4. **Rendering** — The converted model replaces the vanilla player renderer when active, with automatic hiding of the default player model.

### BBModel Compatibility

Full support for Blockbench's format including:

- Outliner tree with nested bone hierarchy and parent-child relationships
- Cube and mesh elements with proper face UV mapping
- Embedded textures (base64) with PNG header dimension detection
- Animation playback with loop mode mapping
- Blockbench 5 "free" format compatibility (thin outliner nodes with `groups[]` fallback)
- Orphan element handling (unreferenced elements auto-assigned to default bone)

## Architecture

Fox Model Loader: Revival uses a **common + platform adapter** layered architecture:

- **`common`** — Core logic shared across all variants: model parsing, mesh processing, zip sniffing, animation controllers, audio decoding, and Molang evaluation.
- **`fabric`** / **`neoforge`** — Platform-specific adapters for initialization, networking, component registration, and rendering hooks.
- **Native renderer** — SIMD acceleration rebuilt from this repository; see [native source and build provenance](NATIVE_SOURCES.md).

## Dependencies

Varies by build variant — see `mods.toml` (NeoForge) or `fabric.mod.json` (Fabric) for specifics. Fabric variants require Fabric API installed separately; other dependencies are bundled via Jar-in-Jar.

## Credits & License

- Built upon [OpenYSM](https://github.com/OpenYSM) (MIT License).
- Uses [OpenYSMDev/YSMParser](https://github.com/OpenYSMDev/YSMParser) (MIT) for `.ysm` model parsing.
- Default model library: [sdf123098/YSM-Model](https://github.com/sdf123098/YSM-Model).
- Blockbench format is a product of [JannisX11/Blockbench](https://github.com/JannisX11/blockbench).

**License:** MIT

## Building

Java 21 is required for 1.21.1; Java 25 for 26.x. Run `./gradlew build` for the standard distribution, or `./gradlew build -Pdist=curseforge` for the Java fallback distribution. The standard distribution bundles the rebuilt SIMD renderer; the CurseForge distribution excludes project native libraries. To rebuild all six native platforms, run `python scripts/rebuild-natives.py --zig <zig.exe> --ndk <Android NDK root>`. See [native source and build provenance](NATIVE_SOURCES.md).
