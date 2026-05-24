package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.client.renderer.RendererManager;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.util.CameraUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.player.Player;
import rip.ysm.compat.firstperson.FirstPersonCompat;
import rip.ysm.compat.playeranimator.PlayerAnimatorCompat;
import rip.ysm.compat.realcamera.RealCameraCompat;

public class ReplacePlayerRenderEvent {

    private ReplacePlayerRenderEvent() {
    }

    public static boolean onRenderPlayerPre(Player entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        return onRenderPlayerPre(entity, entity.getYRot(), partialTick, poseStack, bufferSource, null, packedLight);
    }

    public static boolean onRenderPlayerPre(Player entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, SubmitNodeCollector collector, int packedLight) {
        if (!YesSteveModel.isAvailable()) {
            return false;
        }
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (entity.equals(localPlayer) && GeneralConfig.safeGet(GeneralConfig.DISABLE_SELF_MODEL)) {
            return false;
        }
        if ((!entity.equals(localPlayer) && GeneralConfig.safeGet(GeneralConfig.DISABLE_OTHER_MODEL)) || entity.isSpectator()) {
            return false;
        }
        boolean[] cancelled = {false};
        try {
            PlayerCapability.get(entity).ifPresent(cap -> {
                if (cap.isModelActive()) {
                    if (!CameraUtil.isFirstPerson(cap)
                            || FirstPersonCompat.isFirstPersonActive()
                            || RealCameraCompat.isActive()
                            || GeneralConfig.safeGet(GeneralConfig.DISABLE_EXTERNAL_FP_ANIM)
                            || !PlayerAnimatorCompat.isPlayerAnimated(localPlayer)) {
                        cancelled[0] = true;
                        RendererManager.getPlayerRenderer().render(entity, entityYaw, partialTick, poseStack, bufferSource, collector, packedLight);
                    }
                }
            });
        } catch (Exception e) {
            YesSteveModel.LOGGER.warn("Failed to render custom player model, falling back to vanilla", e);
            return false;
        }
        return cancelled[0];
    }
}
