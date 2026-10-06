"""Exercise the actual optional Iris bridge and reject bundled Iris API classes.

Python 3.11+ and JDK 21+. Loader/API doubles avoid launching Minecraft.
The API is compiled separately to reproduce real mod classpath precedence.
"""
from pathlib import Path
import os
import subprocess
import sys
import tempfile
import unittest
import zipfile

sys.dont_write_bytecode = True
from test_mod_id_compat import java_tool

ROOT = Path(__file__).resolve().parents[2]
FABRIC = (ROOT / 'fabric').is_dir()
BRIDGE = ('com.micaftic.morpher.core.compat.oculus.fabric.OculusCompatImpl'
          if FABRIC else 'com.micaftic.morpher.core.compat.oculus.OculusCompat')
SOURCE_ROOT = ROOT / ('fabric/src/main/java' if FABRIC else 'src/neoforge/java')
API_PATH = 'net/irisshaders/iris/api/v0/IrisApi.java'


def check_jar(path):
    with zipfile.ZipFile(path) as jar:
        collisions = [name for name in jar.namelist()
                      if name.startswith('net/irisshaders/') and name.endswith('.class')]
    if collisions:
        raise AssertionError(f'{path}: bundled Iris classes can shadow the real API: {collisions}')


class OptionalShaderCompatibilityTest(unittest.TestCase):
    def test_iris_owns_its_api_namespace(self):
        self.assertFalse((ROOT / 'common/src/main/java' / API_PATH).exists(),
                         'Do not compile or ship a local IrisApi placeholder')

    def test_actual_bridge_with_optional_api_and_changing_shader_state(self):
        with tempfile.TemporaryDirectory(prefix='fox-iris-') as directory:
            base = Path(directory)
            api = base / 'api'
            mod = base / 'mod'
            api.mkdir()
            mod.mkdir()
            api_source = api / API_PATH
            api_source.parent.mkdir(parents=True)
            api_source.write_text('''package net.irisshaders.iris.api.v0;
public interface IrisApi {
    static IrisApi getInstance() {
        if (Boolean.getBoolean("api.init.fail")) throw new LinkageError("fixture ABI failure");
        return new Implementation();
    }
    boolean isShaderPackInUse();
    boolean isRenderingShadowPass();
}
class Implementation implements IrisApi {
    public boolean isShaderPackInUse() {
        if (Boolean.getBoolean("api.call.fail")) throw new LinkageError("fixture call failure");
        return Boolean.getBoolean("pack");
    }
    public boolean isRenderingShadowPass() {
        if (Boolean.getBoolean("api.call.fail")) throw new LinkageError("fixture call failure");
        return Boolean.getBoolean("shadow");
    }
}
''', encoding='utf-8')
            self.compile(api, [api_source])
            sources = {
                'Fixture.java': f'''import {BRIDGE};
public class Fixture {{
    static void check(boolean value, boolean expected, String label) {{
        if (value != expected) throw new AssertionError(label + ": " + value + " != " + expected);
    }}
    public static void main(String[] args) {{
        boolean loaded = Boolean.getBoolean("loaded");
        boolean usable = loaded && Boolean.getBoolean("usable");
        check({BRIDGE}.isLoaded(), loaded, "mod detection");
        check({BRIDGE}.isShaderPackInUse(), false, "pack disabled");
        check({BRIDGE}.isRenderingShadowPass(), false, "normal pass");
        System.setProperty("pack", "true");
        check({BRIDGE}.isShaderPackInUse(), usable, "pack enabled");
        check({BRIDGE}.isRenderingShadowPass(), false, "pack enabled outside shadow pass");
        System.setProperty("shadow", "true");
        check({BRIDGE}.isRenderingShadowPass(), usable, "shadow pass");
        check({BRIDGE}.isPBRActive(), usable, "legacy shadow predicate");
        System.setProperty("api.call.fail", "true");
        check({BRIDGE}.isShaderPackInUse(), false, "ABI call failure");
        check({BRIDGE}.isRenderingShadowPass(), false, "ABI shadow failure");
        {BRIDGE}.updatePBRState();
    }}
}}''',
                'net/fabricmc/loader/api/FabricLoader.java': '''package net.fabricmc.loader.api;
public class FabricLoader {
    public static FabricLoader getInstance() { return new FabricLoader(); }
    public boolean isModLoaded(String id) {
        return Boolean.getBoolean("loaded") && id.equals(System.getProperty("mod", "iris"));
    }
}''',
                'net/neoforged/fml/ModList.java': '''package net.neoforged.fml;
public class ModList {
    public static ModList get() { return new ModList(); }
    public java.util.Optional<Object> getModContainerById(String id) {
        return Boolean.getBoolean("loaded") && id.equals(System.getProperty("mod", "iris"))
            ? java.util.Optional.of(new Object()) : java.util.Optional.empty();
    }
}''',
            }
            files = [SOURCE_ROOT / (BRIDGE.replace('.', '/') + '.java')]
            # Include the real project placeholder if present: it wins over Iris on
            # the mod-first runtime classpath, reproducing the pre-fix failure.
            placeholder = ROOT / 'common/src/main/java' / API_PATH
            if placeholder.exists():
                files.append(placeholder)
            for name, source in sources.items():
                path = mod / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(source, encoding='utf-8')
                files.append(path)
            self.compile(mod, files, api)
            cases = [(False, False, False, False, 'iris'),
                     (True, False, False, False, 'iris'),
                     (True, True, False, True, 'iris'),
                     (True, True, True, False, 'iris')]
            if not FABRIC:
                cases.append((True, True, False, True, 'oculus'))
            for loaded, include_api, init_fail, usable, mod_id in cases:
                with self.subTest(loaded=loaded, api=include_api, init_fail=init_fail, mod=mod_id):
                    classpath = str(mod) + (os.pathsep + str(api) if include_api else '')
                    result = subprocess.run([
                        java_tool('java'), '-cp', classpath,
                        f'-Dloaded={str(loaded).lower()}', f'-Dusable={str(usable).lower()}',
                        f'-Dapi.init.fail={str(init_fail).lower()}', f'-Dmod={mod_id}', 'Fixture'
                    ], capture_output=True, text=True, encoding='utf-8')
                    self.assertEqual(result.returncode, 0, result.stderr)

    def compile(self, output, files, classpath=None):
        args = [java_tool('javac'), '-encoding', 'UTF-8', '-d', str(output)]
        if classpath is not None:
            args += ['-cp', str(classpath)]
        result = subprocess.run([*args, *map(str, files)], capture_output=True, text=True, encoding='utf-8')
        self.assertEqual(result.returncode, 0, result.stderr)


if __name__ == '__main__':
    if len(sys.argv) == 3 and sys.argv[1] == '--jar':
        check_jar(Path(sys.argv[2]))
        print(f'Optional shader packaging OK: {sys.argv[2]}')
    elif sys.argv[1:] == ['--check-artifacts']:
        libs = ROOT / ('fabric/build/libs' if FABRIC else 'build/libs')
        jars = [p for p in libs.glob('fox-model-loader-revival-*.jar')
                if not any(s in p.stem for s in ('-dev', '-sources', '-javadoc'))]
        if not jars:
            raise SystemExit(f'No release JARs in {libs}')
        for jar in jars:
            check_jar(jar)
            print(f'Optional shader packaging OK: {jar.name}')
    else:
        unittest.main()
