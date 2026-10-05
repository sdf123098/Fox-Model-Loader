# Fox Model Loader 2.0 native renderer

This directory is the native source used by this branch. It is independent of the SPM workspace.

Build all six platforms using Zig and Android NDK:

```powershell
zig build -Dplatform=all -Drelease -Dandroid-ndk="<NDK root>" -Dandroid-api=21
```

JNI ABI: 3; variant family: `vulkan`. Keep this family matched to the Java bridge.
Do not replace classic binaries with Vulkan binaries: JNI registration requires exact methods.

To rebuild and install this branch's resources, run `python scripts/rebuild-natives.py --zig <zig.exe> --ndk <NDK root>`.
