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

## Other optional mods: current implementation scope

Audited on 2026-10-06 against the six released SPM 1.2.9 source archives. The Mod ID rename does not, by itself, require rewriting adapters that detect the other mod's ID or provider classes. It also does not turn inherited placeholders into active integrations.

| Mod | Fabric 1.21.1 / 26.1.2 / 26.2 | NeoForge 1.21.1 / 26.1.2 / 26.2 |
| --- | --- | --- |
| Carpet and fake-player providers | Runtime provider-class recognition, including `carpet.patches.EntityPlayerMPFake`; existing model selection/sync uses Fox's server-authoritative path. | Same recognition strategy for compatible forks; a port's availability and runtime behavior must be checked separately. |
| Sodium | No direct Sodium mixin targets or Sodium implementation-class imports. No rename-induced detection regression found. | Same boundary. This is a source audit, not a guarantee for every renderer/mod combination. |
| Iris | Detects `iris`; 1.21.1 uses compile-only Iris API, while 26.x uses optional reflection. No Iris API placeholder is shipped. | Detects `iris` or `oculus` and uses an optional reflective API bridge. |
| ParCool | Inactive placeholder: detection/action/controller methods return false, empty or no action. | Active reflective adapter, gated by `parcool` and `com.alrex.parcool.common.attachment.common.Parkourability`. Uses `get(Player)`, `getList()` and `isDoingNothing()`. |

The [Carpet provider class](https://github.com/gnembon/fabric-carpet/blob/master/src/main/java/carpet/patches/EntityPlayerMPFake.java), [Iris public API](https://github.com/IrisShaders/Iris/blob/26.1/common/src/api/java/net/irisshaders/iris/api/v0/IrisApi.java), and [ParCool 1.21.1 NeoForge attachment API](https://github.com/alRex-U/ParCool/blob/1.21.1-NF/src/main/java/com/alrex/parcool/common/attachment/common/Parkourability.java) were checked. The ParCool API check does not establish that a matching upstream release exists for Minecraft 26.x.

### Iris packaging fix

The inherited Fabric 26.1.2 and 26.2 builds compiled and packaged a local `net.irisshaders.iris.api.v0.IrisApi` placeholder whose `getInstance()` returned null. This could shadow Iris's actual API and disable shader detection, depending on classpath order. The placeholder is removed and the bridge now resolves the public API reflectively. Iris remains optional; absent APIs and invocation/linkage failures return false safely. The historical `isPBRActive()` predicate still represents the shadow pass, not general material/PBR support.

The regression fixture puts Fox before a separately compiled Iris API on the classpath, exercises enabled/disabled shader packs and shadow passes, and checks missing/broken APIs. CI also inspects the built standard and CurseForge JARs to reject packaged `net/irisshaders/**/*.class` entries.

### Historical placeholders

Better Combat, Create, Curios, Carry On, Player Animator, First Person, Real Camera, Simple Hats, Simple Planes, SWEM, Immersive Aircraft, Immersive Melodies, Elytra Slot, Iron's Spellbooks, `SBackpack` and SWarfare have inactive `isLoaded()` placeholders in these restored branches. This was already true in the archived SPM 1.2.9 sources. Fabric additionally has the ParCool placeholder. TaCZ and SlashBlade vary by loader/version, so they must be assessed per branch.

These stubs do not prove a mod combination crashes or cannot coexist. They mean dedicated item-slot, movement or animation integration is not implemented there. Their names and inherited animation assets are not evidence of full compatibility. Full in-game tests with exact mod versions and shader packs remain separate from source checks, adapter fixtures and clean builds.

## Tweakeroo free camera

Free camera is handled independently of the Mod ID rename. The camera surrogate is excluded from the player model cache, and the local body's render gate uses its real game mode while that camera is active. See [Tweakeroo compatibility](TWEAKEROO_COMPATIBILITY.md) for the upstream contracts, regression coverage and in-game verification limits.

## Regression checks

Run with Python 3.11+ and JDK 21 or newer (`JAVA_HOME` or `FOX_JAVA21_HOME`):

```sh
python scripts/tests/test_mod_id_compat.py
python scripts/tests/test_optional_shader_compat.py
python scripts/tests/test_tweakeroo_freecam.py
./gradlew clean build
./gradlew clean build -Pdist=curseforge
```

The Python fixtures compile the actual query/discovery/override code with loader doubles. NeoForge 26.x additionally exercises its actual maid adapter against the current GUI event contract, including the button, parent screen and Fox network packet.
