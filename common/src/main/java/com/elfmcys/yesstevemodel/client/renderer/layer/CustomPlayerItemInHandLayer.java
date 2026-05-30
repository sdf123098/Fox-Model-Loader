package com.elfmcys.yesstevemodel.client.renderer.layer;

import rip.ysm.compat.slashblade.SlashBladeRenderer;
import rip.ysm.compat.slashblade.SlashBladeCompat;
import rip.ysm.compat.gun.swarfare.SWarfareCompat;
import com.elfmcys.yesstevemodel.client.animation.condition.InnerClassify;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import rip.ysm.compat.gun.tacz.TacCompat;
import com.elfmcys.yesstevemodel.client.renderer.SubmitRenderContext;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.elfmcys.yesstevemodel.util.accessors.BufferSourceAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.effects.SpearAnimations;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.KineticWeapon;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import rip.ysm.api.item.WeaponKind;

public class CustomPlayerItemInHandLayer extends GeoLayerRenderer<CustomPlayerEntity> {

    private final ItemInHandRenderer itemRenderer;

    public CustomPlayerItemInHandLayer(ItemInHandRenderer itemInHandRenderer) {
        this.itemRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn, CustomPlayerEntity entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        LivingEntity entity = entityLivingBaseIn.getEntity();
        AnimatedGeoModel animatedGeoModel = entityLivingBaseIn.getCurrentModel();
        if (animatedGeoModel == null) {
            return;
        }
        ItemStack offhandItem = entity.getOffhandItem();
        ItemStack mainHandItem = entity.getMainHandItem();
        if (!offhandItem.isEmpty() || !mainHandItem.isEmpty()) {
            poseStack.pushPose();
            boolean useExtraPlayer = entityLivingBaseIn.isRenderLayersFirst();
            HumanoidArm mainArm = entity.getMainArm();
            HumanoidArm offArm = mainArm.getOpposite();
            if (hasItemBoneTransform(animatedGeoModel, mainArm)) {
                if (SlashBladeCompat.isSlashBladeItem(mainHandItem)) {
                    SlashBladeRenderer.renderOnEntity(entity, animatedGeoModel, poseStack, bufferSource, packedLightIn, mainHandItem, partialTick);
                } else {
                    TacCompat.handleGunSound(entity, mainHandItem);
                    renderItem(animatedGeoModel, entity, mainHandItem, getDisplayContext(mainArm), mainArm, poseStack, bufferSource, packedLightIn, partialTick);
                    if (useExtraPlayer && !mainHandItem.isEmpty() && (bufferSource instanceof BufferSourceAccessor)) {
                        ((BufferSourceAccessor) bufferSource).initialize();
                    }
                    TacCompat.handleItemSound(mainHandItem);
                }
            }
            if (hasItemBoneTransform(animatedGeoModel, offArm)) {
                if (SlashBladeCompat.isSlashBladeItem(offhandItem)) {
                    SlashBladeRenderer.renderRightWaist(animatedGeoModel, poseStack, bufferSource, packedLightIn, offhandItem);
                } else {
                    if (!SWarfareCompat.isGunItem(offhandItem)) {
                        renderItem(animatedGeoModel, entity, offhandItem, getDisplayContext(offArm), offArm, poseStack, bufferSource, packedLightIn, partialTick);
                    }
                    if (useExtraPlayer && !offhandItem.isEmpty() && (bufferSource instanceof BufferSourceAccessor)) {
                        ((BufferSourceAccessor) bufferSource).initialize();
                    }
                }
            }
            poseStack.popPose();
            TacCompat.applyItemTransform(offhandItem, animatedGeoModel, entity, poseStack, packedLightIn, partialTick);
            SWarfareCompat.applyGunTransform(offhandItem, animatedGeoModel, entity, poseStack, packedLightIn, partialTick);
        }
    }

    public void renderItem(AnimatedGeoModel model, LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float partialTick) {
        if (!itemStack.isEmpty()) {
            boolean isLeftHand = humanoidArm == HumanoidArm.LEFT;
            poseStack.pushPose();
            if (!applyItemBoneTransform(humanoidArm, poseStack, model)) {
                applyFallbackHandTransform(itemStack, poseStack, true);
                if (SWarfareCompat.isGunItem(itemStack)) {
                    poseStack.translate(0.1d, 0.0d, 0.0d);
                    poseStack.scale(1.25f, 1.25f, 1.25f);
                }
                renderVanillaItemWithUseOrientation(livingEntity, itemStack, itemDisplayContext, humanoidArm, poseStack, i, partialTick);
            }
            poseStack.popPose();
            (isLeftHand ? model.rightHandChain() : model.leftHandChains()).forEach(list -> {
                poseStack.pushPose();
                if (!RenderUtils.prepMatrixForLocator(poseStack, list)) {
                    applyFallbackHandTransform(itemStack, poseStack, false);
                    if (SWarfareCompat.isGunItem(itemStack)) {
                        poseStack.scale(1.25f, 1.25f, 1.25f);
                    }
                    renderVanillaItemWithUseOrientation(livingEntity, itemStack, itemDisplayContext, humanoidArm, poseStack, i, partialTick);
                }
                poseStack.popPose();
            });
        }
    }

    private void applyFallbackHandTransform(ItemStack itemStack, PoseStack poseStack, boolean directHandBone) {
        switch (InnerClassify.getWeaponKind(itemStack)) {
            case TRIDENT -> applyTridentHandTransform(poseStack, directHandBone);
            case LANCE -> applyLanceHandTransform(poseStack, directHandBone);
            case MACE -> applyMaceHandTransform(poseStack, directHandBone);
            case NONE -> applyDefaultHandTransform(poseStack);
        }
    }

    private void applyTridentHandTransform(PoseStack poseStack, boolean directHandBone) {
        applyDefaultHandTransform(poseStack);
        if (!directHandBone) {
            poseStack.translate(0.0d, 0.0d, -0.0125d);
        }
    }

    private void applyLanceHandTransform(PoseStack poseStack, boolean directHandBone) {
        applyDefaultHandTransform(poseStack);
        poseStack.translate(0.0d, directHandBone ? -0.01875d : -0.0125d, -0.025d);
    }

    private void applyMaceHandTransform(PoseStack poseStack, boolean directHandBone) {
        applyDefaultHandTransform(poseStack);
        poseStack.translate(0.0d, directHandBone ? -0.0125d : 0.0d, 0.01875d);
    }

    private void applyDefaultHandTransform(PoseStack poseStack) {
        poseStack.translate(0.0d, -0.0625d, -0.1d);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
    }

    private void renderVanillaItemWithUseOrientation(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, HumanoidArm humanoidArm, PoseStack poseStack, int packedLight, float partialTick) {
        if (shouldNormalizeBowItemScale(itemStack)) {
            normalizeBowItemScale(poseStack);
        }
        if (shouldApplySpearUseItemTransform(livingEntity, itemStack, humanoidArm)) {
            float ticksUsingItem = clampSpearUseTicksBeforeVanillaSway(itemStack, livingEntity.getTicksUsingItem(partialTick));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            ArmedEntityRenderState renderState = new ArmedEntityRenderState();
            renderState.attackTime = livingEntity.getAttackAnim(partialTick);
            renderState.ticksSinceKineticHitFeedback = livingEntity.getTicksSinceLastKineticHitFeedback(partialTick);
            SpearAnimations.thirdPersonUseItem(renderState, poseStack, ticksUsingItem, humanoidArm, itemStack);
        }
        renderVanillaItem(livingEntity, itemStack, itemDisplayContext, humanoidArm, poseStack, packedLight);
    }

    private boolean shouldNormalizeBowItemScale(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.getUseAnimation() == ItemUseAnimation.BOW;
    }

    private void normalizeBowItemScale(PoseStack poseStack) {
        float scale = getLargestAxisScale(poseStack.last().pose());
        if (scale <= 1.0f || scale < 1.0E-4f) {
            return;
        }
        float inverseScale = 1.0f / scale;
        poseStack.scale(inverseScale, inverseScale, inverseScale);
    }

    private float getLargestAxisScale(Matrix4f matrix) {
        float xScale = length(matrix.m00(), matrix.m01(), matrix.m02());
        float yScale = length(matrix.m10(), matrix.m11(), matrix.m12());
        float zScale = length(matrix.m20(), matrix.m21(), matrix.m22());
        return Math.max(xScale, Math.max(yScale, zScale));
    }

    private float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private float clampSpearUseTicksBeforeVanillaSway(ItemStack itemStack, float ticksUsingItem) {
        KineticWeapon kineticWeapon = itemStack.get(DataComponents.KINETIC_WEAPON);
        if (kineticWeapon == null) {
            return ticksUsingItem;
        }
        return kineticWeapon.dismountConditions().map(condition -> {
            float swayStartTicks = kineticWeapon.delayTicks() + condition.maxDurationTicks() - 20.0f;
            if (swayStartTicks <= 0.0f) {
                return ticksUsingItem;
            }
            float stableUseTicks = Math.max(kineticWeapon.delayTicks(), swayStartTicks - 0.01f);
            return Math.min(ticksUsingItem, stableUseTicks);
        }).orElse(ticksUsingItem);
    }

    private boolean shouldApplySpearUseItemTransform(LivingEntity livingEntity, ItemStack itemStack, HumanoidArm humanoidArm) {
        if (itemStack.isEmpty() || itemStack.getUseAnimation() != ItemUseAnimation.SPEAR) {
            return false;
        }
        if (!livingEntity.isUsingItem() || livingEntity.getUseItemRemainingTicks() <= 0) {
            return false;
        }
        return livingEntity.getUsedItemHand() == getRenderedHand(livingEntity, humanoidArm);
    }

    private InteractionHand getRenderedHand(LivingEntity livingEntity, HumanoidArm humanoidArm) {
        return humanoidArm == livingEntity.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private ItemDisplayContext getDisplayContext(HumanoidArm humanoidArm) {
        return humanoidArm == HumanoidArm.LEFT ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private boolean hasItemBoneTransform(AnimatedGeoModel model, HumanoidArm humanoidArm) {
        return humanoidArm == HumanoidArm.LEFT ? !model.leftHandBones().isEmpty() : !model.rightHandBones().isEmpty();
    }

    private void renderVanillaItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, HumanoidArm humanoidArm, PoseStack poseStack, int packedLight) {
        SubmitNodeCollector collector = SubmitRenderContext.get();
        if (collector != null) {
            this.itemRenderer.renderItem(livingEntity, itemStack, itemDisplayContext, poseStack, collector, packedLight);
        }
    }

    public boolean applyItemBoneTransform(HumanoidArm humanoidArm, PoseStack poseStack, AnimatedGeoModel model) {
        if (humanoidArm == HumanoidArm.LEFT) {
            return RenderUtils.prepMatrixForLocator(poseStack, model.leftHandBones());
        }
        return RenderUtils.prepMatrixForLocator(poseStack, model.rightHandBones());
    }
}
