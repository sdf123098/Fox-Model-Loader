package com.micaftic.morpher.fabric.client;

import com.micaftic.morpher.YesSteveModel;
import com.micaftic.morpher.client.ClientModelManager;
import com.micaftic.morpher.client.compat.ClientRenderCompatibility;
import com.micaftic.morpher.client.compat.ClientRenderCompatibilityRegistry;
import com.micaftic.morpher.client.renderer.AnimationDebugOverlay;
import com.micaftic.morpher.client.renderer.ExtraPlayerOverlay;
import com.micaftic.morpher.client.renderer.ModelSyncStateOverlay;
import com.micaftic.morpher.core.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;
import com.micaftic.morpher.core.api.client.HudOverlay;

public final class YesSteveModelFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // External 1.2.9 integrations may still use the original entrypoint key.
        // A module advertising both keys must initialize only once; prefer the Revival entry.
        java.util.Set<Class<?>> discovered = new java.util.HashSet<>();
        for (String key : new String[]{"foxmodelloader_render_compat", "sparkle_morpher_render_compat"}) {
            for (ClientRenderCompatibility module : FabricLoader.getInstance()
                    .getEntrypoints(key, ClientRenderCompatibility.class)) {
                if (discovered.add(module.getClass())) {
                    ClientRenderCompatibilityRegistry.register(module);
                }
            }
        }
        OrihimeDirectModelCompat.init();
        KeyMappingRegistry.getCustomKeyMappings().forEach(KeyMappingHelper::registerKeyMapping);

        HudOverlay debugOverlay = AnimationDebugOverlay.createOverlay();
        HudOverlay loadingOverlay = new ExtraPlayerOverlay();
        HudOverlay syncOverlay = new ModelSyncStateOverlay();
        HudElementRegistry.attachElementAfter(VanillaHudElements.BOSS_BAR, com.micaftic.morpher.core.api.resource.ResourceApi.nativeId(YesSteveModel.MOD_ID, "hud_overlays"), (guiGraphics, tickDelta) -> {
            Minecraft mc = Minecraft.getInstance();
            float delta = tickDelta.getGameTimeDeltaTicks();
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            Font font = mc.font;
            debugOverlay.render(guiGraphics, font, delta, w, h);
            loadingOverlay.render(guiGraphics, font, delta, w, h);
            syncOverlay.render(guiGraphics, font, delta, w, h);
        });

        ClientModelManager.loadDefaultModel();
        ClientModelManager.reloadLocalModels(null, false);
    }
}
