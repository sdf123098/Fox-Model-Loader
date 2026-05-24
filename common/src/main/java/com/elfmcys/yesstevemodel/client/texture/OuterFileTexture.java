package com.elfmcys.yesstevemodel.client.texture;

import com.elfmcys.yesstevemodel.YesSteveModel;
import rip.ysm.compat.oculus.ShadersTextureType;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public class OuterFileTexture extends AbstractTexture implements ITextureMap {
    private final byte[] data;

    private Map<ShadersTextureType, OuterFileTexture> suffixTextures = Reference2ReferenceMaps.emptyMap();

    private boolean uploaded;

    public OuterFileTexture(byte[] data) {
        this.data = data;
    }

    public void load(@NotNull ResourceManager resourceManager) {
        doLoad();
    }

    public void doLoad() {
        RenderSystem.assertOnRenderThread();
        if (this.uploaded && this.textureView != null) {
            return;
        }
        NativeImage image = null;
        try {
            image = NativeImage.read(new ByteArrayInputStream(data));
        } catch (IOException e) {
            YesSteveModel.LOGGER.warn("Failed to decode YSM texture, using fallback texture", e);
            image = createFallbackImage();
        }
        uploadImage(image);
    }

    public boolean isLoaded() {
        return this.uploaded && this.textureView != null;
    }

    private void uploadImage(NativeImage image) {
        try (image) {
            int width = Math.max(1, image.getWidth());
            int height = Math.max(1, image.getHeight());
            if (this.texture != null || this.textureView != null || this.sampler != null) {
                super.close();
            }
            var device = RenderSystem.getDevice();
            this.texture = device.createTexture(
                    () -> "YSM outer texture",
                    GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
                    TextureFormat.RGBA8,
                    width,
                    height,
                    1,
                    1);
            this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
            this.textureView = device.createTextureView(this.texture);
            device.createCommandEncoder().writeToTexture(this.texture, image);
            this.uploaded = true;
        }
    }

    private static NativeImage createFallbackImage() {
        NativeImage image = new NativeImage(1, 1, false);
        image.setPixel(0, 0, 0xFFFF00FF);
        return image;
    }

    public void setSuffixTextures(Map<ShadersTextureType, OuterFileTexture> map) {
        this.suffixTextures = Reference2ReferenceMaps.unmodifiable(new Reference2ReferenceOpenHashMap<>(map));
    }

    public Map<ShadersTextureType, ? extends AbstractTexture> getSuffixTextures() {
        return this.suffixTextures;
    }
}
