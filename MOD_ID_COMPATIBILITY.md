# Mod ID compatibility after Revival

The loader identity is `foxmodelloader`. A rename does not require reimplementing every optional integration: adapters detecting another mod's ID or classes continue to use that mod's identity. Checks for the original YSM implementation must still use `yes_steve_model`, rather than detecting Fox itself.

## Touhou Little Maid

- Fabric uses [TouhouLittleMaid-Orihime](https://github.com/Sh1roCu/TouhouLittleMaid-Orihime). The 1.21.1 adapter uses its existing YSM screen callback; 26.x uses `MaidContainerGuiEvent.INIT` and adds the model-selection button through `Init.addButton`.
- NeoForge 1.21.1 retains the original YSM screen event integration. NeoForge 26.x also supports the [new repository](https://github.com/TouhouLittleMaid/TouhouLittleMaid-26.1): when `OpenYsmMaidScreenEvent` is absent, it registers `MaidContainerGuiEvent.Init` on the NeoForge event bus instead.
- Selection uses Fox's `C2SSetMaidModelPacket`, followed by server ownership, model authorization and texture checks. It does not require the newer maid repository to implement the old YSM network packet.
- These adapters remain optional and client-side. When original YSM is installed, they defer to its integration. No loader alias for `yes_steve_model` is advertised.

Upstream interfaces were checked on 2026-10-06 against Orihime branches `1.21.1`, `26.1`, `26.2`, and the new NeoForge repository's `master` (26.1.2) and `gizmo_fix_26.2` branches. This is source/API verification; automated fixtures check event registration, opening the model screen and sending Fox's packet. Full in-game visual verification with those mod combinations is separate.

## Existing extension interfaces

Fabric discovers both `foxmodelloader_render_compat` and legacy `sparkle_morpher_render_compat`. If a renderer module advertises both keys, it initializes once, with the Revival entry taking precedence.

Model scripts may continue querying `ysm.mod_version('sparkle_morpher')`: when that ID is absent, Fox's actual installed version is returned. An independently installed mod under the requested ID takes precedence. Unrelated or absent mods are not reported as installed; `yes_steve_model` remains a distinct implementation. NeoForge 1.21.1 now queries real loader metadata rather than returning a constant version for every mod.

Where supported, existing `sparkle_morpher.mixin.*`, `sparkle_morpher.disableMixins` and `sparkle_morpher.graphicsBackend` JVM overrides remain accepted. The corresponding `foxmodelloader.*` setting takes precedence; existing `ysm.*` and environment fallbacks remain available.

External addons that declare a hard dependency on `sparkle_morpher`, use loader-level old-ID detection, old resource namespaces or old network channels still need a Fox-aware release. The internal script fallback and entrypoint key do not create a loader alias or make SPM/YSM network protocols interchangeable. Target `foxmodelloader` in addon metadata and use the matching Minecraft/loader API.

## Regression checks

Run with Python 3.11+ and JDK 21 or newer (`JAVA_HOME` or `FOX_JAVA21_HOME`):

```sh
python scripts/tests/test_mod_id_compat.py
./gradlew clean build
./gradlew clean build -Pdist=curseforge
```

The Python fixtures compile the actual query/discovery/override code with loader doubles. NeoForge 26.x additionally exercises its actual maid adapter against the current GUI event contract, including the button, parent screen and Fox network packet.
