# Native source and build provenance

Fox Model Loader: Revival 2.0 rebuilds the original Sparkle Morpher 1.2.9 native renderer from the source in this repository.
Upstream: [OpenYSMDev/openysm.cpp](https://github.com/OpenYSMDev/openysm.cpp); historical fork: [sdf123098/openysm.cpp](https://github.com/sdf123098/openysm.cpp). The packaged files below are local rebuilds, not the historical fork's binaries.

Source: `common/src/main/native/openysm.cpp`. Family: **classic**; JNI ABI: **3**.
Source bundle SHA-256: `f07cb2737184581349a665b78312baee0b85cdc87c7c86e17f0c2c73c8df62ba` (SHA-256 of sorted JSON filename/digest map in `native-manifest.json`).

Toolchain: Zig `0.17.0-dev.1099+7db2ef610`, Android NDK `25.2.9519653`, Android API `21`, `ReleaseFast`.
Rebuild: `python scripts/rebuild-natives.py --zig <zig.exe> --ndk <NDK root>`.
The script verifies all six outputs before installing them and refreshes the manifest. Gradle checks digests and JNI markers before packaging.
Classic branches (1.21.1/26.1) and Vulkan branches (26.2) must use their corresponding source and Java bridge.
Internal JNI package names and `ysm-core` library filenames remain stable for ABI compatibility; the mod ID/resource namespace is `foxmodelloader`.

| Target | Packaged file | SHA-256 |
| --- | --- | --- |
| windows-x64 | `natives/windows-x64/ysm-core.dll` | BE9220EB779E332C3D3AEC1C1C56A23EBECE9455755B042F2A686FB17930550A |
| windows-x86 | `natives/windows-x86/ysm-core.dll` | 38FB99931E022A29172A9D34930C6D115844CA420BD7DE5A88D2FEC12E606C37 |
| linux-x64 | `natives/linux-x64/libysm-core.so` | 5A70BD270CFFA461973606452EDE720C688E83A48DF8AE2B63003EC3F6788811 |
| macos-x64 | `natives/macos-x64/libysm-core.dylib` | 10A1F4DE36EC3CA69DED52A357F64E50FBCACF7C3CC4DBCDD69224FE07D55954 |
| macos-arm64 | `natives/macos-arm64/libysm-core.dylib` | DB2CAC51E7F875DFC49BBFAD5E52BF7462D73BBAB51B874D3FDE235AD194DCB9 |
| android-arm64 | `natives/android-arm64/libysm-core.so` | 12D55085B2ADB7B524458C6F161C5D4FC0F0DA9CBE2933ED226E73695C3864A4 |
