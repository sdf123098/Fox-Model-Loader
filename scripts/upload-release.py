"""Validate and publish the eight standard Release/ JARs using authenticated gh.

Python 3.11+ and GitHub CLI are required. Run --dry-run for offline validation.
New releases remain drafts until every uploaded file passes SHA-256 verification.
Reruns resume drafts or skip identical published assets; conflicts are never deleted.
"""
from __future__ import annotations

import argparse
from datetime import datetime, timedelta, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile
import tomllib
from urllib.parse import quote
import zipfile

REPO_ROOT = Path(__file__).resolve().parent.parent
VARIANTS = ("Fa1.21.1", "Fa26.1.2", "Fa26.2", "Fa26.3", "Neo1.21.1", "Neo26.1.2", "Neo26.2", "Neo26.3")
DISPLAY_NAME = "Fox Model Loader: Revival"


def sha256(path: Path) -> str:
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def collect(workspace: Path) -> tuple[str, list[dict]]:
    release = (workspace / "Release").resolve()
    index = json.loads((workspace / "artifacts.json").read_text(encoding="utf-8"))
    records = [entry for entry in index if entry["distribution"] == "native"]
    if len(records) != len(VARIANTS) or {r["variant"] for r in records} != set(VARIANTS):
        raise ValueError("artifacts.json must contain exactly eight distinct native variants")
    versions = {r.get("version") for r in records}
    if len(versions) != 1 or not all(versions):
        raise ValueError("All eight native variants must have the same nonempty version")
    version = versions.pop()
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._+-]*", version):
        raise ValueError("Invalid artifact version")
    icon = (workspace / "icon.png").read_bytes()
    artifacts = []
    for variant in VARIANTS:
        record = next(r for r in records if r["variant"] == variant)
        key = variant.lower().replace("26.1.2", "26.1.x")
        name = f"fox-model-loader-revival-{version}-{key}.jar"
        path = (release / name).resolve()
        if path.parent != release or Path(record["file"]).name != name:
            raise ValueError(f"Unexpected artifact path for {variant}")
        digest, size = sha256(path), path.stat().st_size
        if digest != record["sha256"].lower() or size != record["bytes"]:
            raise ValueError(f"Build index SHA-256/size mismatch: {name}; rebuild first")
        with zipfile.ZipFile(path) as jar:
            if jar.testzip():
                raise ValueError(f"Corrupt JAR: {name}")
            if variant.startswith("Fa"):
                metadata = json.loads(jar.read("fabric.mod.json"))
                identity = (metadata["id"], metadata["version"], metadata["name"])
                icon_path = metadata["icon"]
            else:
                metadata = tomllib.loads(jar.read("META-INF/neoforge.mods.toml").decode("utf-8"))["mods"][0]
                identity = (metadata["modId"], metadata["version"], metadata["displayName"])
                icon_path = metadata["logoFile"]
            if identity != ("foxmodelloader", version, DISPLAY_NAME) or jar.read(icon_path) != icon:
                raise ValueError(f"Wrong mod identity or icon: {name}")
            natives = json.loads(jar.read("native-manifest.json"))["artifacts"]
            native_paths = {f"natives/{n['platform']}/{n['filename']}" for n in natives}
            packaged = {n for n in jar.namelist() if n.startswith("natives/") and n.endswith((".dll", ".so", ".dylib"))}
            if len(natives) != 6 or len(native_paths) != 6 or native_paths != packaged:
                raise ValueError(f"Expected six native binaries: {name}")
            for native in natives:
                data = jar.read(f"natives/{native['platform']}/{native['filename']}")
                if hashlib.sha256(data).hexdigest() != native["sha256"].lower():
                    raise ValueError(f"Native digest mismatch: {name}: {native['platform']}")
        artifacts.append(dict(variant=variant, path=path, name=name, sha256=digest, bytes=size))
    if {p.name for p in release.glob("*.jar")} != {a["name"] for a in artifacts}:
        raise ValueError("Release/ contains extra or missing JARs; keep only the eight indexed standard JARs")
    return version, artifacts


def gh(*arguments: str) -> str:
    env = os.environ.copy()
    env.update(GH_PROMPT_DISABLED="1", GH_PAGER="cat", GIT_TERMINAL_PROMPT="0")
    result = subprocess.run(["gh", *arguments], capture_output=True, text=True,
                            encoding="utf-8", errors="replace", env=env, timeout=600)
    if result.returncode:
        raise RuntimeError(f"gh {arguments[0]} failed ({result.returncode}): {result.stderr.strip()}")
    return result.stdout


def validate_notes(notes: str, artifacts: list[dict]) -> None:
    if not notes.strip():
        raise ValueError("Release notes must not be empty")
    digests = {a["name"]: a["sha256"] for a in artifacts}
    rows = re.findall(r"^\|\s*`([^`]+\.jar)`\s*\|\s*`([0-9a-fA-F]{64})`\s*\|", notes, re.MULTILINE)
    for name, digest in rows:
        if name not in digests or digest.lower() != digests[name]:
            raise ValueError(f"Release notes contain a stale SHA-256 or filename: {name}; update the notes")


def get_release(repo: str, tag: str) -> dict | None:
    # Exact GraphQL lookup finds drafts immediately, before the REST list catches up.
    # API/network/auth errors must never be interpreted as a missing release.
    owner, name = repo.split("/", 1)
    query = ("query($owner:String!,$repo:String!,$tag:String!){"
             "repository(owner:$owner,name:$repo){release(tagName:$tag){"
             "databaseId tagName isDraft name description url}}}")
    response = json.loads(gh("api", "graphql", "-f", f"query={query}", "-f", f"owner={owner}",
                             "-f", f"repo={name}", "-f", f"tag={tag}"))
    repository = response["data"]["repository"]
    if repository is None:
        raise RuntimeError(f"Repository is unavailable: {repo}")
    release = repository["release"]
    if release is None:
        return None
    return dict(id=release["databaseId"], tag_name=release["tagName"], draft=release["isDraft"],
                name=release["name"], body=release["description"] or "", html_url=release["url"])


def get_assets(repo: str, release: dict) -> dict[str, dict]:
    pages = json.loads(gh("api", f"repos/{repo}/releases/{release['id']}/assets?per_page=100",
                         "--paginate", "--slurp"))
    assets = [asset for page in pages for asset in page]
    names = {asset["name"] for asset in assets}
    if len(names) != len(assets):
        raise RuntimeError("Duplicate remote asset names")
    return {asset["name"]: asset for asset in assets}


def verify_asset(repo: str, tag: str, remote: dict, local: dict) -> None:
    if remote.get("state") != "uploaded" or remote["size"] != local["bytes"]:
        raise RuntimeError(f"Remote asset incomplete or size mismatch: {local['name']}")
    expected = "sha256:" + local["sha256"]
    if remote.get("digest"):
        if remote["digest"].lower() != expected:
            raise RuntimeError(f"Remote SHA-256 conflict: {local['name']}; no assets were deleted")
    else:
        # Older uploads may lack the API digest; verify their actual bytes instead.
        with tempfile.TemporaryDirectory(prefix="fox-release-verify-") as temporary:
            gh("release", "download", tag, "--repo", repo, "--pattern", local["name"], "--dir", temporary)
            if sha256(Path(temporary) / local["name"]) != local["sha256"]:
                raise RuntimeError(f"Downloaded SHA-256 conflict: {local['name']}")


def publish(args: argparse.Namespace, artifacts: list[dict]) -> dict:
    gh("auth", "status", "--hostname", "github.com")
    release = get_release(args.repo, args.tag)
    remote_assets = get_assets(args.repo, release) if release else {}
    # Verify all existing attachments BEFORE any remote mutation.
    for artifact in artifacts:
        if artifact["name"] in remote_assets:
            verify_asset(args.repo, args.tag, remote_assets[artifact["name"]], artifact)
    if args.draft and release and not release["draft"]:
        raise RuntimeError("--draft cannot turn an existing published release back into a draft")
    if release is None:
        target = args.target
        if not re.fullmatch(r"[0-9a-fA-F]{40}", target):
            ref = json.loads(gh("api", f"repos/{args.repo}/git/ref/heads/{quote(target, safe='/')}"))
            target = ref["object"]["sha"]
        gh("release", "create", args.tag, "--repo", args.repo, "--target", target,
           "--title", args.title, "--notes-file", str(args.notes_file), "--draft")
        release = get_release(args.repo, args.tag)
        if release is None:
            raise RuntimeError("Created release was not returned by GitHub")
    # Freeze local bytes so a concurrent rebuild cannot change an in-flight upload.
    with tempfile.TemporaryDirectory(prefix="fox-release-upload-") as temporary:
        for artifact in artifacts:
            if artifact["name"] in remote_assets:
                print(f"SKIP (same SHA-256): {artifact['name']}", flush=True)
                continue
            snapshot = Path(temporary) / artifact["name"]
            shutil.copy2(artifact["path"], snapshot)
            if sha256(snapshot) != artifact["sha256"]:
                raise RuntimeError(f"Artifact changed during upload: {artifact['name']}")
            print(f"UPLOAD: {artifact['name']}", flush=True)
            gh("release", "upload", args.tag, str(snapshot), "--repo", args.repo)
    remote_assets = get_assets(args.repo, release)
    for artifact in artifacts:
        if artifact["name"] not in remote_assets:
            raise RuntimeError(f"Missing uploaded asset: {artifact['name']}")
        verify_asset(args.repo, args.tag, remote_assets[artifact["name"]], artifact)
    body = args.notes_file.read_text(encoding="utf-8-sig")
    if release["draft"] or release["name"] != args.title or release.get("body", "") != body:
        gh("release", "edit", args.tag, "--repo", args.repo, "--title", args.title,
           "--notes-file", str(args.notes_file), f"--draft={str(args.draft).lower()}")
    result = get_release(args.repo, args.tag)
    if result is None or result["draft"] != args.draft or result["name"] != args.title or result["body"] != body:
        raise RuntimeError("Final release title, notes or publication state did not match")
    return result


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--workspace", type=Path, default=REPO_ROOT.parent,
                        help="Folder containing Release/, artifacts.json and icon.png")
    parser.add_argument("--repo", default="sdf123098/Fox-Model-Loader")
    parser.add_argument("--target", default="main", help="Remote branch or full commit SHA for a new tag")
    parser.add_argument("--tag", help="Default: v<artifact version>")
    parser.add_argument("--title", help="Default: Fox Model Loader: Revival <version>")
    parser.add_argument("--notes-file", type=Path, help="Default: docs/releases/v<version>/RELEASE_NOTES.md in this repo")
    parser.add_argument("--draft", action="store_true", help="Upload and verify, but leave the release unpublished")
    parser.add_argument("--dry-run", action="store_true", help="Validate local files and notes offline; no GitHub changes")
    parser.add_argument("--record-dir", type=Path, default=Path("D:/SparkleMorpher/docs"))
    args = parser.parse_args()
    if not re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", args.repo):
        parser.error("--repo must be OWNER/REPO on github.com")
    version, artifacts = collect(args.workspace.resolve())
    args.tag = args.tag or f"v{version}"
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._+/-]*", args.tag):
        parser.error("Invalid --tag")
    args.title = args.title or f"{DISPLAY_NAME} {version}"
    args.notes_file = (args.notes_file or REPO_ROOT / f"docs/releases/v{version}/RELEASE_NOTES.md").resolve()
    validate_notes(args.notes_file.read_text(encoding="utf-8-sig"), artifacts)
    print(f"Release: {args.repo} / {args.tag}\nNotes: {args.notes_file}", flush=True)
    for artifact in artifacts:
        print(f"VERIFIED: {artifact['name']}  {artifact['sha256']}", flush=True)
    if args.dry_run:
        print("Dry run passed; no GitHub requests or changes.")
        return 0
    if not shutil.which("gh"):
        raise RuntimeError("GitHub CLI is missing; install gh and run gh auth login")
    now = datetime.now(timezone(timedelta(hours=8)))
    record_dir = args.record_dir / now.strftime("%Y-%m-%d") / f"fox-release-upload-{now.strftime('%H%M%S-%f')}"
    record_dir.mkdir(parents=True, exist_ok=False)
    record = dict(created=now.isoformat(), repo=args.repo, tag=args.tag,
                  notes=str(args.notes_file), notesSha256=sha256(args.notes_file),
                  artifacts=[{k: v for k, v in a.items() if k != "path"} for a in artifacts])
    try:
        release = publish(args, artifacts)
        record.update(success=True, url=release["html_url"], draft=release["draft"], releaseId=release["id"])
    except (OSError, RuntimeError, ValueError, KeyError, subprocess.TimeoutExpired) as error:
        record.update(success=False, error=str(error))
        raise
    finally:
        (record_dir / "result.json").write_text(json.dumps(record, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"Upload record: {record_dir / 'result.json'}", flush=True)
    print(f"SUCCESS: {'Draft' if release['draft'] else 'Published'} release with eight verified JARs: {release['html_url']}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, RuntimeError, ValueError, KeyError, zipfile.BadZipFile, subprocess.TimeoutExpired) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        raise SystemExit(1)
