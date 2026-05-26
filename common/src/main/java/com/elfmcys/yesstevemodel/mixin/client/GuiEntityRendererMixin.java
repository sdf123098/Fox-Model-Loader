package com.elfmcys.yesstevemodel.mixin.client;

import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {
    @Inject(method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("HEAD"), cancellable = true)
    private void ysm$renderQueuedPreview(GuiEntityRenderState state, PoseStack poseStack, CallbackInfo ci) {
        ModelPreviewRenderer.setPreviewMode(true);
        poseStack.pushPose();
        Vector3f translation = state.translation();
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(state.rotation());
        FeatureRenderDispatcher featureDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        if (ModelPreviewRenderer.renderQueuedGuiPreview(state.renderState(), poseStack, featureDispatcher.getSubmitNodeStorage())) {
            featureDispatcher.renderAllFeatures();
            poseStack.popPose();
            ModelPreviewRenderer.setPreviewMode(false);
            ci.cancel();
            return;
        }
        poseStack.popPose();
        ModelPreviewRenderer.setPreviewMode(false);
    }

    @Inject(method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("RETURN"))
    private void ysm$clearGuiPreviewMode(GuiEntityRenderState state, PoseStack poseStack, CallbackInfo ci) {
        ModelPreviewRenderer.setPreviewMode(false);
    }
}
