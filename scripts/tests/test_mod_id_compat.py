"""Regression checks for the SPM -> Fox rename. Uses real ModVersion Java code.

Loader doubles let the model-script query run without booting Minecraft.
Optional maid/renderer hooks have explicit external-ID source contracts.
Run with Python 3.11+ and JDK 21+: python scripts/tests/test_mod_id_compat.py
"""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


def java_tool(name):
    home = os.environ.get('FOX_JAVA21_HOME') or os.environ.get('JAVA_HOME')
    if not home and os.name == 'nt':
        home = 'C:/Program Files/Java/jdk-21.0.12'
    if home:
        candidate = Path(home) / 'bin' / (name + ('.exe' if os.name == 'nt' else ''))
        if candidate.is_file():
            return str(candidate)
    found = shutil.which(name)
    if not found:
        raise RuntimeError('JDK 21+ required; set JAVA_HOME or FOX_JAVA21_HOME')
    return found

def method_source(source, signature):
    start = source.index(signature)
    opening = source.index('{', start)
    depth = 1
    end = opening + 1
    while depth:
        if source[end] == '{':
            depth += 1
        elif source[end] == '}':
            depth -= 1
        end += 1
    return source[start:end]


def run_fixture(sources):
    with tempfile.TemporaryDirectory(prefix='fox-compat-') as directory:
        base = Path(directory)
        files = []
        for name, source in sources.items():
            path = base / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(source, encoding='utf-8')
            files.append(str(path))
        compiled = subprocess.run([java_tool('javac'), '-encoding', 'UTF-8', '-d', str(base), *files], capture_output=True, text=True, encoding='utf-8')
        if compiled.returncode:
            raise AssertionError(compiled.stderr)
        result = subprocess.run([java_tool('java'), '-cp', str(base), 'Fixture'], capture_output=True, text=True, encoding='utf-8')
        if result.returncode:
            raise AssertionError(result.stderr)


class ModIdCompatibilityTest(unittest.TestCase):
    def test_model_scripts_can_query_legacy_id_without_faking_other_mods(self):
        production = ROOT / 'common/src/main/java/com/micaftic/morpher/client/animation/molang/functions/ysm/ModVersion.java'
        if not production.is_file():
            production = ROOT / 'src/neoforge/java/com/micaftic/morpher/client/animation/molang/functions/ysm/ModVersion.java'
        with tempfile.TemporaryDirectory(prefix='fox-mod-id-') as directory:
            base = Path(directory)
            sources = {
                'com/micaftic/morpher/molang/runtime/ExecutionContext.java':
                    'package com.micaftic.morpher.molang.runtime; public interface ExecutionContext<T> {}',
                'com/micaftic/morpher/molang/runtime/Function.java': '''
package com.micaftic.morpher.molang.runtime;
public interface Function {
    Object evaluate(ExecutionContext<?> context, ArgumentCollection args);
    boolean validateArgumentSize(int size);
    record ArgumentCollection(String value) {
        public String getAsString(ExecutionContext<?> ctx, int index) { return value; }
    }
}''',
                'com/micaftic/morpher/YesSteveModel.java':
                    'package com.micaftic.morpher; public class YesSteveModel { public static final String MOD_ID="foxmodelloader"; }',
                'org/jetbrains/annotations/NotNull.java':
                    'package org.jetbrains.annotations; public @interface NotNull {}',
                'org/jetbrains/annotations/Nullable.java':
                    'package org.jetbrains.annotations; public @interface Nullable {}',
                'Fixture.java': '''
import com.micaftic.morpher.client.animation.molang.functions.ysm.ModVersion;
import com.micaftic.morpher.molang.runtime.Function.ArgumentCollection;
public class Fixture {
    static void check(ModVersion fn, String id, String expected) {
        Object actual = fn.evaluate(null, new ArgumentCollection(id));
        if (!java.util.Objects.equals(expected, actual))
            throw new AssertionError(id + ": expected " + expected + ", got " + actual);
    }
    public static void main(String[] args) {
        ModVersion fn = new ModVersion();
        check(fn, "foxmodelloader", "2.0");
        check(fn, "sparkle_morpher", "2.0");
        check(fn, "other_mod", "3.1");
        check(fn, "missing_mod", null);
        check(fn, "yes_steve_model", null);
        check(fn, null, null);
        System.setProperty("fixture.spm", "1.2.9");
        check(fn, "sparkle_morpher", "1.2.9");
    }
}''',
            }
            for package in ['dev.architectury.platform', 'com.micaftic.morpher.core.architectury.platform']:
                sources[package.replace('.', '/') + '/Platform.java'] = '''
package %s;
public class Platform {
    public static boolean isModLoaded(String id) { return version(id) != null; }
    public static Mod getMod(String id) { return new Mod(version(id)); }
    public record Mod(String version) { public String getVersion() { return version; } }
    static String version(String id) {
        return switch(id) {
            case "foxmodelloader" -> "2.0";
            case "other_mod" -> "3.1";
            case "sparkle_morpher" -> System.getProperty("fixture.spm");
            default -> null;
        };
    }
}''' % package
            sources['net/neoforged/fml/ModList.java'] = '''
package net.neoforged.fml;
public class ModList {
    public static ModList get() { return new ModList(); }
    public java.util.Optional<Container> getModContainerById(String id) {
        var mod = dev.architectury.platform.Platform.getMod(id);
        return mod.getVersion() == null ? java.util.Optional.empty() : java.util.Optional.of(new Container(mod.getVersion()));
    }
    public record Container(String version) { public Container getModInfo() { return this; } public String getVersion() { return version; } }
}'''
            java_files = []
            for name, source in sources.items():
                path = base / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(source, encoding='utf-8')
                java_files.append(str(path))
            result = subprocess.run([java_tool('javac'), '-encoding', 'UTF-8', '-d', str(base), *java_files, str(production)], capture_output=True, text=True, encoding='utf-8')
            self.assertEqual(0, result.returncode, result.stderr)
            result = subprocess.run([java_tool('java'), '-cp', str(base), 'Fixture'], capture_output=True, text=True, encoding='utf-8')
            self.assertEqual(0, result.returncode, result.stderr)

    @unittest.skipUnless((ROOT / 'fabric').is_dir(), 'Fabric extension contract')
    def test_fabric_reads_both_external_entrypoint_keys(self):
        path = ROOT / 'fabric/src/main/java/com/micaftic/morpher/fabric/client/YesSteveModelFabricClient.java'
        source = path.read_text(encoding='utf-8')
        self.assertIn('"foxmodelloader_render_compat"', source)
        self.assertIn('"sparkle_morpher_render_compat"', source)

    @unittest.skipUnless((ROOT / 'fabric').is_dir(), 'Fabric extension discovery')
    def test_fabric_legacy_extension_initializes_once(self):
        source = (ROOT / 'fabric/src/main/java/com/micaftic/morpher/fabric/client/YesSteveModelFabricClient.java').read_text(encoding='utf-8')
        body = method_source(source, 'public void onInitializeClient()').split('{', 1)[1]
        discovery = body[:body.index('        OrihimeDirectModelCompat.init();')]
        run_fixture({
            'net/fabricmc/loader/api/FabricLoader.java': '''
package net.fabricmc.loader.api;
public class FabricLoader {
    public static java.util.Map<String, java.util.List<?>> entries = new java.util.HashMap<>();
    public static FabricLoader getInstance() { return new FabricLoader(); }
    @SuppressWarnings("unchecked")
    public <T> java.util.List<T> getEntrypoints(String key, Class<T> type) {
        return (java.util.List<T>) entries.getOrDefault(key, java.util.List.of());
    }
}''',
            'Fixture.java': '''
import net.fabricmc.loader.api.FabricLoader;
public class Fixture {
    interface ClientRenderCompatibility {}
    static class Old implements ClientRenderCompatibility {}
    static class Current implements ClientRenderCompatibility {}
    static class ClientRenderCompatibilityRegistry {
        static java.util.List<ClientRenderCompatibility> registered = new java.util.ArrayList<>();
        static void register(ClientRenderCompatibility module) { registered.add(module); }
    }
    static void discover() { ''' + discovery + ''' }
    public static void main(String[] args) {
        Old old = new Old();
        FabricLoader.entries.put("sparkle_morpher_render_compat", java.util.List.of(old));
        discover();
        if (!ClientRenderCompatibilityRegistry.registered.equals(java.util.List.of(old)))
            throw new AssertionError("Legacy extension was not initialized");
        ClientRenderCompatibilityRegistry.registered.clear();
        Current current = new Current();
        FabricLoader.entries.put("foxmodelloader_render_compat", java.util.List.of(current));
        FabricLoader.entries.put("sparkle_morpher_render_compat", java.util.List.of(new Current(), old));
        discover();
        if (!ClientRenderCompatibilityRegistry.registered.equals(java.util.List.of(current, old)))
            throw new AssertionError("Dual-key module initialized twice, or Revival entry did not take precedence");
    }
}'''
        })

    @unittest.skipUnless((ROOT / 'fabric').is_dir(), 'Fabric maid contract')
    def test_orihime_guard_excludes_original_ysm_instead_of_itself(self):
        source = (ROOT / 'fabric/src/main/java/com/micaftic/morpher/fabric/client/OrihimeDirectModelCompat.java').read_text(encoding='utf-8')
        self.assertIn('FabricLoader.getInstance().isModLoaded("yes_steve_model")', source)
        self.assertNotIn('FabricLoader.getInstance().isModLoaded("foxmodelloader")', source)

    @unittest.skipUnless((ROOT / 'common/src/main/java/com/micaftic/morpher/client/renderer/preview/PreviewRenderBridge.java').is_file(), '26.x preview overrides')
    def test_legacy_mixin_override_still_disables_gui_preview(self):
        source = (ROOT / 'common/src/main/java/com/micaftic/morpher/client/renderer/preview/PreviewRenderBridge.java').read_text(encoding='utf-8')
        method = method_source(source, 'private static boolean isGuiPreviewBridgeMixinEnabledByConfig()')
        run_fixture({'Fixture.java': 'public class Fixture { ' + method + '''
    static void check(boolean expected) {
        if (isGuiPreviewBridgeMixinEnabledByConfig() != expected) throw new AssertionError("Legacy mixin override ignored");
    }
    public static void main(String[] args) {
        System.setProperty("sparkle_morpher.mixin.GuiEntityRendererMixin", "false");
        check(false);
        System.setProperty("foxmodelloader.mixin.GuiEntityRendererMixin", "true");
        check(true);
        System.clearProperty("foxmodelloader.mixin.GuiEntityRendererMixin");
        System.clearProperty("sparkle_morpher.mixin.GuiEntityRendererMixin");
        System.setProperty("sparkle_morpher.disableMixins", "GuiEntityRendererMixin");
        check(false);
        System.setProperty("foxmodelloader.disableMixins", "UnrelatedMixin");
        check(true);
    }
}'''})

    def test_legacy_graphics_backend_override(self):
        path = ROOT / 'common/src/main/java/com/micaftic/morpher/core/render/SmGraphicsBackendDetector.java'
        if not path.is_file():
            self.skipTest('No backend override detector in this variant')
        source = path.read_text(encoding='utf-8')
        if 'String override = firstNonBlank(' not in source:
            self.skipTest('This variant uses different backend detection')
        start = source.index('String override = firstNonBlank(')
        override = source[start:source.index(');', start) + 2]
        helper = method_source(source, 'private static String firstNonBlank(')
        run_fixture({'Fixture.java': 'public class Fixture { ' + helper + '\nstatic String override() { ' + override + ''' return override; }
    public static void main(String[] args) {
        System.setProperty("sparkle_morpher.graphicsBackend", "vulkan");
        if (!"vulkan".equals(override())) throw new AssertionError("Legacy backend override ignored");
        System.setProperty("foxmodelloader.graphicsBackend", "opengl");
        if (!"opengl".equals(override())) throw new AssertionError("Revival override must take precedence");
    }
}'''})

    def test_legacy_mixin_plugin_override(self):
        path = ROOT / 'common/src/main/java/com/micaftic/morpher/mixin/plugin/MixinTweaker.java'
        if not path.is_file() or 'disabledMixins()' not in path.read_text(encoding='utf-8'):
            self.skipTest('No configurable mixin plugin in this variant')
        run_fixture({
            'com/micaftic/morpher/mixin/plugin/MixinTweaker.java': path.read_text(encoding='utf-8'),
            'com/micaftic/morpher/util/obfuscate/Keep.java': 'package com.micaftic.morpher.util.obfuscate; public @interface Keep {}',
            'org/objectweb/asm/tree/ClassNode.java': 'package org.objectweb.asm.tree; public class ClassNode {}',
            'org/spongepowered/asm/mixin/extensibility/IMixinConfigPlugin.java': 'package org.spongepowered.asm.mixin.extensibility; public interface IMixinConfigPlugin {}',
            'org/spongepowered/asm/mixin/extensibility/IMixinInfo.java': 'package org.spongepowered.asm.mixin.extensibility; public interface IMixinInfo {}',
            'Fixture.java': '''
import com.micaftic.morpher.mixin.plugin.MixinTweaker;
public class Fixture {
    static void check(boolean expected) {
        if (new MixinTweaker().shouldApplyMixin("unused", "com.micaftic.morpher.mixin.client.GuiEntityRendererMixin") != expected)
            throw new AssertionError("Legacy mixin plugin override ignored");
    }
    public static void main(String[] args) {
        System.setProperty("sparkle_morpher.mixin.GuiEntityRendererMixin", "false");
        check(false);
        System.setProperty("foxmodelloader.mixin.GuiEntityRendererMixin", "true");
        check(true);
        System.clearProperty("sparkle_morpher.mixin.GuiEntityRendererMixin");
        System.clearProperty("foxmodelloader.mixin.GuiEntityRendererMixin");
        System.setProperty("sparkle_morpher.disableMixins", "GuiEntityRendererMixin");
        check(false);
        System.setProperty("foxmodelloader.disableMixins", "UnrelatedMixin");
        check(true);
    }
}'''
        })

    @unittest.skipUnless((ROOT / 'src/neoforge').is_dir(), 'NeoForge maid contract')
    def test_maid_guard_excludes_original_ysm_instead_of_itself(self):
        path = ROOT / 'src/neoforge/java/com/micaftic/morpher/client/compat/touhoulittlemaid/OfficialTouhouLittleMaidCompat.java'
        source = path.read_text(encoding='utf-8')
        self.assertIn('ModList.get().isLoaded("yes_steve_model")', source)
        self.assertNotIn('ModList.get().isLoaded("foxmodelloader")', source)

    def test_neoforge_new_maid_repository_has_gui_event_fallback(self):
        if not ROOT.name.startswith('Fox-Model-Loader-Neo26.'):
            self.skipTest('New NeoForge maid repository targets 26.x')
        path = ROOT / 'src/neoforge/java/com/micaftic/morpher/client/compat/touhoulittlemaid/OfficialTouhouLittleMaidCompat.java'
        source = path.read_text(encoding='utf-8')
        self.assertIn('com.github.tartaricacid.touhoulittlemaid.api.event.client.MaidContainerGuiEvent$Init', source)
        self.assertIn('getMethod("addButton", String.class, AbstractWidget.class)', source)
        self.assertIn('ClassNotFoundException', source)

    def test_neoforge_new_maid_gui_selects_model_through_fox_network(self):
        if not ROOT.name.startswith('Fox-Model-Loader-Neo26.'):
            self.skipTest('New NeoForge maid repository targets 26.x')
        from maid_gui_fixture import sources
        path = ROOT / 'src/neoforge/java/com/micaftic/morpher/client/compat/touhoulittlemaid/OfficialTouhouLittleMaidCompat.java'
        run_fixture(sources(path.read_text(encoding='utf-8')))


if __name__ == '__main__':
    unittest.main(verbosity=2)
