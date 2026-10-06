# Fox Model Loader: Revival 2.1 — First LTS Release

**Version 2.1 is the first LTS release in the project's quarterly release cycle.** Each quarter will have one planned feature release, with critical bug, compatibility, or security fixes available between quarterly releases when needed. This cadence prioritizes stability, compatibility, and predictable server deployment.

Fox Model Loader: Revival continues the traditional Minecraft client/server model synchronization architecture restored from the released Sparkle Morpher **1.2.9** line. The mod ID and resource namespace are `foxmodelloader`; model IDs and the `.ysm` format remain separate.

## What's included

- Adds the **Minecraft 26.3** Fabric and NeoForge targets alongside 1.21.1, 26.1.2, and 26.2.
- Migrates 26.3 rendering, player pose and hand rendering, GUI previews, and keyboard/mouse input to the updated game APIs while retaining server-side model synchronization.
- Fixes a 26.3 crash when creating or entering a world if Fox's extra animation keys were saved as unbound. The game now uses the canonical unbound key and repairs the old saved value on load.
- Restores compatibility paths affected by the Mod ID change, including legacy renderer extensions, model version queries, and startup compatibility switches.
- Updates TouhouLittleMaid integration for the new NeoForge 26.x GUI event and repairs the Fabric Orihime integration.
- Fixes Fabric 26.x Iris detection so the mod no longer packages a placeholder Iris API class; adds Carpet fake-player recognition and NeoForge ParCool action integration.
- Preserves custom player models while Tweakeroo free camera is active.
- Uses the refreshed pixel-art mod icon across the release targets.

## Current release targets

Install one JAR matching both your Minecraft version and loader. Fabric requires Fabric API; other declared dependencies are bundled where applicable. The standard JARs include the six-platform native renderer.

| Minecraft | Loader | Java | Download |
| --- | --- | --- | --- |
| 1.21.1 | Fabric | 21 | [fox-model-loader-revival-2.1-fa1.21.1.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.1/fox-model-loader-revival-2.1-fa1.21.1.jar) |
| 26.1.2 | Fabric | 25 | [fox-model-loader-revival-2.1-fa26.1.x.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.1/fox-model-loader-revival-2.1-fa26.1.x.jar) |
| 26.2 | Fabric | 25 | [fox-model-loader-revival-2.1-fa26.2.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.1/fox-model-loader-revival-2.1-fa26.2.jar) |
| 26.3 | Fabric | 25 | [fox-model-loader-revival-2.1-fa26.3.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.1/fox-model-loader-revival-2.1-fa26.3.jar) |
| 1.21.1 | NeoForge | 21 | [fox-model-loader-revival-2.1-neo1.21.1.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.1/fox-model-loader-revival-2.1-neo1.21.1.jar) |
| 26.1.2 | NeoForge | 25 | [fox-model-loader-revival-2.1-neo26.1.x.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.1/fox-model-loader-revival-2.1-neo26.1.x.jar) |
| 26.2 | NeoForge | 25 | [fox-model-loader-revival-2.1-neo26.2.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.1/fox-model-loader-revival-2.1-neo26.2.jar) |
| 26.3 | NeoForge | 25 | [fox-model-loader-revival-2.1-neo26.3.jar](https://github.com/sdf123098/Fox-Model-Loader/releases/download/v2.1/fox-model-loader-revival-2.1-neo26.3.jar) |

For multiplayer model sharing, install the matching mod version on both server and client. The release retains the traditional server-pushed model flow and does not use the later SPM Cloud architecture.

## Compatibility notes

- Fabric ParCool remains an inactive placeholder; the action adapter is available on NeoForge for compatible ParCool APIs.
- Sodium and Iris behavior still depends on the exact renderer and shader combination. Automated compatibility checks and clean builds do not replace in-game testing of every mod combination.
- No change was made to model IDs or the `.ysm` file format.

## Build verification

The eight standard JARs and eight CurseForge Java-fallback JARs are clean-built from their matching branches. The release process checks metadata, icon, archive integrity, native contents or fallback loader, and SHA-256 values. The game was not started as part of packaging, so loader and mod-combination gameplay remains subject to user testing.

## SHA-256

| File | SHA-256 |
| --- | --- |
| `fox-model-loader-revival-2.1-fa1.21.1.jar` | `f28cc24786a52fdf0ef231a9218f3e1a497251a108f7a0f5f2e27281f55da979` |
| `fox-model-loader-revival-2.1-fa26.1.x.jar` | `08a60f62bd1adfc3ab86f2dd3dddc4ff5d254bd7363fdb7007c58d73eaa2f337` |
| `fox-model-loader-revival-2.1-fa26.2.jar` | `0da1a391b9258adcd9b386811ad86f67e462e197dcd7b3a07b2e099861a03512` |
| `fox-model-loader-revival-2.1-fa26.3.jar` | `4abdc53da127bfde13df11308b286b5e0700f373998fbfc19b173e566f00ab7e` |
| `fox-model-loader-revival-2.1-neo1.21.1.jar` | `7abd20cbd53237b0c4b4caa8298a833b60bb2298c572fc071bbc4ea8c675c132` |
| `fox-model-loader-revival-2.1-neo26.1.x.jar` | `8881256f993061dada725e510fdb91edfd9344b954eaa6db709073e266567b5b` |
| `fox-model-loader-revival-2.1-neo26.2.jar` | `211023df01ba754d90df16dc7627431907c52c0f78a8726b60d3c7916de57166` |
| `fox-model-loader-revival-2.1-neo26.3.jar` | `71651a88ea4b21fb11ec22961b6f62c0a03ed995c038aaef154ad571e184c6ae` |
