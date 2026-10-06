"""Exercise release preflight and failure/resume behavior without GitHub writes."""
import argparse
import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import zipfile

spec = importlib.util.spec_from_file_location("upload_release", Path(__file__).parents[1] / "upload-release.py")
upload = importlib.util.module_from_spec(spec)
spec.loader.exec_module(upload)


class UploadTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        (self.root / "Release").mkdir()
        (self.root / "icon.png").write_bytes(b"test-icon")
        self.index = []
        natives = []
        for i in range(6):
            data = f"native-{i}".encode()
            natives.append(dict(platform=f"platform-{i}", filename="library.so", sha256=hashlib.sha256(data).hexdigest()))
        for variant in upload.VARIANTS:
            name = "fox-model-loader-revival-2.1-" + variant.lower().replace("26.1.2", "26.1.x") + ".jar"
            path = self.root / "Release" / name
            with zipfile.ZipFile(path, "w") as jar:
                jar.writestr("icon.png", b"test-icon")
                jar.writestr("native-manifest.json", json.dumps(dict(artifacts=natives)))
                for i, native in enumerate(natives):
                    jar.writestr(f"natives/{native['platform']}/{native['filename']}", f"native-{i}")
                if variant.startswith("Fa"):
                    jar.writestr("fabric.mod.json", json.dumps(dict(id="foxmodelloader", version="2.1", name=upload.DISPLAY_NAME, icon="icon.png")))
                else:
                    jar.writestr("META-INF/neoforge.mods.toml", f'[[mods]]\nmodId="foxmodelloader"\nversion="2.1"\ndisplayName="{upload.DISPLAY_NAME}"\nlogoFile="icon.png"\n')
            self.index.append(dict(variant=variant, distribution="native", version="2.1", file=str(path), sha256=upload.sha256(path), bytes=path.stat().st_size))
        self.write_index()
        notes = self.root / "notes.md"
        notes.write_text("# Release 2.1\n", encoding="utf-8")
        self.args = argparse.Namespace(repo="owner/repo", tag="v2.1", target="main", title="Release 2.1", notes_file=notes, draft=False)
        self.release = dict(id=42, tag_name="v2.1", name=self.args.title, body=notes.read_text(), draft=True, html_url="https://github.com/owner/repo/releases/tag/v2.1")

    def write_index(self):
        (self.root / "artifacts.json").write_text(json.dumps(self.index), encoding="utf-8")

    def remote_assets(self, artifacts):
        return {a["name"]: dict(name=a["name"], state="uploaded", size=a["bytes"], digest="sha256:" + a["sha256"]) for a in artifacts}

    def test_valid_batch(self):
        version, artifacts = upload.collect(self.root)
        self.assertEqual(version, "2.1")
        self.assertEqual(len(artifacts), 8)

    def test_incomplete_batch_rejected(self):
        self.index.pop()
        self.write_index()
        with self.assertRaisesRegex(ValueError, "eight distinct"):
            upload.collect(self.root)

    def test_changed_jar_rejected_before_upload(self):
        with Path(self.index[0]["file"]).open("ab") as stream:
            stream.write(b"changed")
        with self.assertRaisesRegex(ValueError, "SHA-256/size mismatch"):
            upload.collect(self.root)

    def test_stale_icon_rejected(self):
        (self.root / "icon.png").write_bytes(b"new-icon")
        with self.assertRaisesRegex(ValueError, "identity or icon"):
            upload.collect(self.root)

    def test_extra_jar_rejected(self):
        (self.root / "Release/old-version.jar").write_bytes(b"old")
        with self.assertRaisesRegex(ValueError, "extra or missing"):
            upload.collect(self.root)

    def test_stale_note_checksum_rejected(self):
        _, artifacts = upload.collect(self.root)
        body = f"| `{artifacts[0]['name']}` | `{'0' * 64}` |\n"
        with self.assertRaisesRegex(ValueError, "stale SHA-256"):
            upload.validate_notes(body, artifacts)

    def test_draft_lookup_when_release_list_is_stale(self):
        graphql_release = dict(databaseId=42, tagName="v2.1", isDraft=True,
                               name=self.args.title, description=self.release["body"], url=self.release["html_url"])
        def gh(*args):
            if args[:2] == ("api", "graphql"):
                return json.dumps(dict(data=dict(repository=dict(release=graphql_release))))
            return "[[]]"  # The REST collection has not caught up yet.
        with patch.object(upload, "gh", side_effect=gh):
            result = upload.get_release("owner/repo", "v2.1")
            self.assertIsNotNone(result)
            self.assertEqual(result["id"], 42)
            self.assertTrue(result["draft"])

    def test_remote_conflict_causes_no_mutation(self):
        _, artifacts = upload.collect(self.root)
        remote = self.remote_assets(artifacts)
        remote[artifacts[0]["name"]]["digest"] = "sha256:" + "0" * 64
        with patch.object(upload, "get_release", return_value=self.release), patch.object(upload, "get_assets", return_value=remote), patch.object(upload, "gh") as gh:
            with self.assertRaisesRegex(RuntimeError, "conflict"):
                upload.publish(self.args, artifacts)
            self.assertEqual(gh.call_args_list, [unittest.mock.call("auth", "status", "--hostname", "github.com")])

    def test_failed_upload_does_not_publish_draft(self):
        _, artifacts = upload.collect(self.root)
        def gh(*args):
            if args[:2] == ("release", "upload"):
                raise RuntimeError("network failure")
            return ""
        with patch.object(upload, "get_release", return_value=self.release), patch.object(upload, "get_assets", return_value={}), patch.object(upload, "gh", side_effect=gh) as mocked:
            with self.assertRaisesRegex(RuntimeError, "network failure"):
                upload.publish(self.args, artifacts)
            self.assertFalse(any(c.args[:2] == ("release", "edit") for c in mocked.call_args_list))

    def test_published_identical_batch_is_noop(self):
        _, artifacts = upload.collect(self.root)
        self.release["draft"] = False
        with patch.object(upload, "get_release", return_value=self.release), patch.object(upload, "get_assets", return_value=self.remote_assets(artifacts)), patch.object(upload, "gh") as mocked:
            result = upload.publish(self.args, artifacts)
            self.assertFalse(result["draft"])
            self.assertFalse(any(c.args[0] == "release" for c in mocked.call_args_list))

    def test_resume_draft_uploads_only_missing_before_publishing(self):
        _, artifacts = upload.collect(self.root)
        remote = self.remote_assets(artifacts)
        partial = {a["name"]: remote[a["name"]] for a in artifacts[:-1]}
        published = dict(self.release, draft=False)
        with patch.object(upload, "get_release", side_effect=[self.release, published]), patch.object(upload, "get_assets", side_effect=[partial, remote]), patch.object(upload, "gh", return_value="") as mocked:
            upload.publish(self.args, artifacts)
            writes = [c.args[:2] for c in mocked.call_args_list if c.args[0] == "release"]
            self.assertEqual(writes, [("release", "upload"), ("release", "edit")])


if __name__ == "__main__":
    unittest.main()
