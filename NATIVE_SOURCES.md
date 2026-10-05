# Native source and build provenance

Fox Model Loader 2.0 rebuilds the original Sparkle Morpher 1.2.9 native renderer from the source in this repository.
Upstream: [OpenYSMDev/openysm.cpp](https://github.com/OpenYSMDev/openysm.cpp); historical fork: [sdf123098/openysm.cpp](https://github.com/sdf123098/openysm.cpp). The packaged files below are local rebuilds, not the historical fork's binaries.

Source: `common/src/main/native/openysm.cpp`. Family: **vulkan**; JNI ABI: **3**.
Source bundle SHA-256: `c9c54d82ff493c967365570f0c89609b478dc8467077db25df75edd27842910e` (SHA-256 of sorted JSON filename/digest map in `native-manifest.json`).

Toolchain: Zig `0.17.0-dev.1099+7db2ef610`, Android NDK `25.2.9519653`, Android API `21`, `ReleaseFast`.
Rebuild: `python scripts/rebuild-natives.py --zig <zig.exe> --ndk <NDK root>`.
The script verifies all six outputs before installing them and refreshes the manifest. Gradle checks digests and JNI markers before packaging.
Classic branches (1.21.1/26.1) and Vulkan branches (26.2) must use their corresponding source and Java bridge.
Internal JNI package names and `ysm-core` library filenames remain stable for ABI compatibility; the mod ID/resource namespace is `foxmodelloader`.

| Target | Packaged file | SHA-256 |
| --- | --- | --- |
| windows-x64 | `natives/windows-x64/ysm-core.dll` | 5929C863177FD46433F486234ECE2CF225B5F75AFB2D4A04877D60D349186C8D |
| windows-x86 | `natives/windows-x86/ysm-core.dll` | B38B5743C55F73F9255094C001A890A8B2BC02C9AAA5BCF74BAD38FCAD7E5091 |
| linux-x64 | `natives/linux-x64/libysm-core.so` | 68E3EE6689CF7A6326E8D073F680AA074DEDAFBDB31EAEC968C7C30B4C55EF04 |
| macos-x64 | `natives/macos-x64/libysm-core.dylib` | AC683CC59D5E71A94EAC9C4709F5EED3C472879D4418FA67D121580577FFF328 |
| macos-arm64 | `natives/macos-arm64/libysm-core.dylib` | 33C6AA6DD36D925E81E4EF4D83F4E7F40D803D4E511BECE2B8F84E039F5822EA |
| android-arm64 | `natives/android-arm64/libysm-core.so` | 062A3D3C99FBFD15A59486F2684C9CB7BFF20DF199929FEB6C8E00A6DC3BAAD8 |
