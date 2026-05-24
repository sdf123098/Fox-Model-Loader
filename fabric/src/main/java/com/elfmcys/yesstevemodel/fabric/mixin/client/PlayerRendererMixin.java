package com.elfmcys.yesstevemodel.fabric.mixin.client;

import com.elfmcys.yesstevemodel.client.event.ReplacePlayerRenderEvent;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.mixin.client.MinecraftAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerRendererMixin {

    // MC 26.x Fabric: submit() is in LivingEntityRenderer, not overridden in AvatarRenderer.
    // Only intercept when the render state is for an avatar/player.
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void ysm$onSubmit(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        if (state instanceof AvatarRenderState avatarState && Minecraft.getInstance().level != null) {
            net.minecraft.world.entity.Entity entity = Minecraft.getInstance().level.getEntity(avatarState.id);
            if (entity instanceof AbstractClientPlayer player) {
                float yaw = ModelPreviewRenderer.isPreview() ? 180.0f : state.yRot;
                if (ReplacePlayerRenderEvent.onRenderPlayerPre(player, yaw, ((MinecraftAccessor) Minecraft.getInstance()).ysm$getDeltaTracker().getGameTimeDeltaTicks(), poseStack, ((MinecraftAccessor) Minecraft.getInstance()).ysm$renderBuffers().bufferSource(), collector, 0xF000F0)) {
                    ci.cancel();
                }
            }
        }
    }
}
