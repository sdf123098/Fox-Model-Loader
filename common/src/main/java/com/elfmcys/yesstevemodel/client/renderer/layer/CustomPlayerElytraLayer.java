package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.mojang.math.Axis;

public class CustomPlayerElytraLayer extends GeoLayerRenderer<CustomPlayerEntity> {

    private static final Identifier WINGS_LOCATION = Identifier.withDefaultNamespace("textures/entity/equipment/wings/elytra.png");

    private final net.minecraft.client.model.object.equipment.ElytraModel elytraModel;

    public CustomPlayerElytraLayer(EntityRendererProvider.Context context) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        net.minecraft.client.model.object.equipment.ElytraModel rawModel = new net.minecraft.client.model.object.equipment.ElytraModel(context.bakeLayer(ModelLayers.ELYTRA));
        this.elytraModel = rawModel;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn, CustomPlayerEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (shouldSkipCustomElytra(entityLivingBaseIn.getModelId())) {
            return;
        }
        LivingEntity entity = entityLivingBaseIn.getEntity();
        ItemStack stack = CosmeticArmorHelper.getElytraItem(entity);
        AnimatedGeoModel animatedGeoModel = entityLivingBaseIn.getCurrentModel();
        if (!stack.isEmpty() && animatedGeoModel != null && !animatedGeoModel.elytraBones().isEmpty()) {
            Identifier cloakTextureLocation = WINGS_LOCATION;
            if (entity instanceof AbstractClientPlayer abstractClientPlayer) {
                if (abstractClientPlayer.getSkin().elytra() != null) {
                    cloakTextureLocation = abstractClientPlayer.getSkin().elytra().texturePath();
                } else if (abstractClientPlayer.getSkin().cape() != null) {
                    cloakTextureLocation = abstractClientPlayer.getSkin().cape().texturePath();
                }
            }
            poseStack.pushPose();
            renderElytra(poseStack, animatedGeoModel);
            poseStack.translate(0.0d, 1.5d, 0.0d);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            poseStack.scale(2.0f, 2.0f, 2.0f);
            this.elytraModel.setupAnim(createElytraState(entity, partialTick, ageInTicks));
            this.elytraModel.renderToBuffer(poseStack, bufferSource.getBuffer(RenderTypes.armorCutoutNoCull(cloakTextureLocation)), packedLightIn, OverlayTexture.NO_OVERLAY, -1);
            poseStack.popPose();
        }
    }

    private HumanoidRenderState createElytraState(LivingEntity entity, float partialTick, float ageInTicks) {
        HumanoidRenderState state = new HumanoidRenderState();
        state.ageInTicks = ageInTicks;
        state.isBaby = entity.isBaby();
        state.isCrouching = entity.isCrouching();
        state.isFallFlying = entity.isFallFlying();
        state.elytraRotX = entity.elytraAnimationState.getRotX(partialTick);
        state.elytraRotY = entity.elytraAnimationState.getRotY(partialTick);
        state.elytraRotZ = entity.elytraAnimationState.getRotZ(partialTick);
        return state;
    }

    public void renderElytra(PoseStack poseStack, AnimatedGeoModel model) {
        RenderUtils.prepMatrixForLocator(poseStack, model.elytraBones());
    }

    private static boolean shouldSkipCustomElytra(String modelId) {
        return "default".equals(modelId)
                || "misc/1_alex".equals(modelId)
                || "misc/2_steve".equals(modelId)
                || "misc/3_default_boy".equals(modelId)
                || "misc/4_default_controllers".equals(modelId);
    }
}
