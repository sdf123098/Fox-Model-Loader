"""Build and install every native target in one standalone Fox repository."""
from pathlib import Path
import argparse
import hashlib
import json
import re
import shutil
import subprocess

FILES = ["dllmain.cpp", "build.zig", "build.zig.zon", "LICENSE",
         "third_party/jni/jni.h", "third_party/jni/jni_md.h",
         "third_party/sse2neon/sse2neon.h"]

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--zig", required=True)
    parser.add_argument("--ndk", required=True, type=Path)
    parser.add_argument("--repo", type=Path, default=Path(__file__).resolve().parent.parent)
    args = parser.parse_args()
    repo = args.repo.resolve()
    source = repo / "common/src/main/native/openysm.cpp"
    resources = repo / ("common/src/main/resources" if (repo / "fabric").is_dir() else "src/main/resources")
    manifest_file = resources / "native-manifest.json"
    manifest = json.loads(manifest_file.read_text(encoding="utf-8"))
    vulkan = "nComputeBoneMatricesVulkan" in (source / "dllmain.cpp").read_text(encoding="utf-8")
    family = "vulkan" if vulkan else "classic"
    ndk_version = next(line.split("=", 1)[1].strip() for line in
                       (args.ndk / "source.properties").read_text().splitlines() if line.startswith("Pkg.Revision"))
    zig_version = subprocess.check_output([args.zig, "version"], text=True).strip()
    subprocess.run([args.zig, "build", "-Dplatform=all", "-Drelease",
                    f"-Dandroid-ndk={args.ndk}", "-Dandroid-api=21", "-j2", "--summary", "all"],
                   cwd=source, check=True)
    source_files = {name: sha(source / name) for name in FILES}
    source_sha = hashlib.sha256(json.dumps(source_files, sort_keys=True).encode()).hexdigest()
    version = f"fox-2.0-{family}-{source_sha[:16]}"
    # Validate all outputs before changing packaged resources.
    for artifact in manifest["artifacts"]:
        binary = source / "zig-out" / artifact["platform"] / artifact["filename"]
        data = binary.read_bytes()
        for marker in [b"nGetAbiVersion", b"nInitModelCache", b"ModelAccelerationBridge", b"ModelRendererBridge"]:
            assert marker in data, (binary, marker)
        assert (b"nComputeBoneMatricesVulkan" in data) == vulkan, binary
        artifact.update(sha256=sha(binary).upper(), abi=3, version=version)
    for artifact in manifest["artifacts"]:
        binary = source / "zig-out" / artifact["platform"] / artifact["filename"]
        target = resources / "natives" / artifact["platform"] / artifact["filename"]
        shutil.copy2(binary, target)
        assert sha(target).upper() == artifact["sha256"]
    manifest.update(version=version, sourceSha256=source_sha, sourceFiles=source_files,
                    toolchain={"zig": zig_version, "androidNdk": ndk_version,
                               "androidApi": 21, "optimize": "ReleaseFast"})
    manifest_file.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    doc_file = repo / "NATIVE_SOURCES.md"
    doc = doc_file.read_text(encoding="utf-8")
    doc = re.sub(r"Source bundle SHA-256: `[0-9a-f]+`", f"Source bundle SHA-256: `{source_sha}`", doc)
    doc = re.sub(r"Toolchain: Zig `[^`]+`, Android NDK `[^`]+`",
                 f"Toolchain: Zig `{zig_version}`, Android NDK `{ndk_version}`", doc)
    for artifact in manifest["artifacts"]:
        path = f"natives/{artifact['platform']}/{artifact['filename']}"
        doc = re.sub(r"(`" + re.escape(path) + r"`\s*\|\s*)[0-9A-Fa-f]+",
                     lambda match: match.group(1) + artifact["sha256"], doc)
    doc_file.write_text(doc, encoding="utf-8")
    print(f"Installed and verified {len(manifest['artifacts'])} {family} binaries. Source SHA-256: {source_sha}")

if __name__ == "__main__":
    main()
