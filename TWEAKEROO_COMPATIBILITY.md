# Tweakeroo free camera compatibility

Fox Model Loader: Revival keeps the selected custom model on the player's body during Tweakeroo free camera (also called freecam). This compatibility is optional, client-side, and does not change the mod ID, model format, server synchronization or version.

## Fix

Tweakeroo's `fi.dy.masa.tweakeroo.util.CameraEntity` is a separate local-player instance sharing the body's profile/UUID and entity ID. It is a camera, not a second model owner. Fox excludes it, including subclasses, from the client capability cache before UUID lookup. This prevents a camera lookup from replacing the body's selected-model state. Real respawns still create a new capability and disconnects still clear the cache.

Tweakeroo can also temporarily spoof the local player's `isSpectator()` result for terrain updates. While a Tweakeroo camera is active, Fox's custom-model render gate checks `Minecraft.gameMode.getPlayerMode()` for the body instead. A genuine spectator remains excluded; self/other model-disable settings retain their effect. Outside that camera mode, and for other players, the usual spectator check applies.

The implementation detects the camera's class name/hierarchy without importing Tweakeroo classes or registering loader aliases. It is shared across the six Fox branches. This does not establish availability of a Tweakeroo build or port for every loader/version.

## Verification

```sh
python scripts/tests/test_tweakeroo_freecam.py
```

The fixture compiles the actual client cache, render event, render policy and compatibility helper against small Minecraft/Tweakeroo contract doubles. It covers camera lookups, selected-model retention, exiting free camera, respawn/disconnect, temporary spectator spoofing, real spectators and model-disable settings. Both regression cases fail against the pre-fix sources and pass with the fix.

Upstream contracts were inspected on 2026-10-06 in the [Tweakeroo maintenance repository](https://github.com/sakura-ryoko/tweakeroo):

| Branch | Verified commit |
| --- | --- |
| LTS/1.21 | `18459b58dab5f71855da6e21db4a5ffda6c7194e` |
| LTS/26.1 | `9e62daffe2ef3aea4f3ff7b4db130df468af4e14` |
| LTS/26.2 | `e6f0c04819f60d361d94c24599a0974d631b2459` |

See [CameraEntity](https://github.com/sakura-ryoko/tweakeroo/blob/e6f0c04819f60d361d94c24599a0974d631b2459/src/main/java/fi/dy/masa/tweakeroo/util/CameraEntity.java) and [the spectator mixin](https://github.com/sakura-ryoko/tweakeroo/blob/e6f0c04819f60d361d94c24599a0974d631b2459/src/main/java/fi/dy/masa/tweakeroo/mixin/freecam/MixinPlayer_freeCam.java). Older branches use different mixin names and do not necessarily enable the same hooks.

Fixtures and clean standard/CurseForge builds verify these code paths and packaging. Full in-game visual verification with exact Tweakeroo, Sodium/Iris versions and shader packs remains separate. For a manual check, select a Fox model, enable free camera in first person, orbit the body, exit, respawn and reconnect. Repeat with model rendering disabled and with actual spectator mode.
