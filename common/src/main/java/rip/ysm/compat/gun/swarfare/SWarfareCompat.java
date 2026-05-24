package rip.ysm.compat.gun.swarfare;

import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SWarfareCompat {

    private SWarfareCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl.isLoaded();
    }

    public static boolean isGunItem(ItemStack itemStack) {
        return rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl.isGunItem(itemStack);
    }

    public static boolean isPlayerAiming(Player player) {
        return rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl.isPlayerAiming(player);
    }

    public static void applyGunTransform(ItemStack stack, AnimatedGeoModel model, LivingEntity entity, PoseStack poseStack, int packedLightIn, float partialTicks) {
        rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl.applyGunTransform(stack, model, entity, poseStack, packedLightIn, partialTicks);
    }

    public static PlayState handleTaczAnim(LivingEntity entity, AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event, String str, ILoopType loopType) {
        return rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl.handleTaczAnim(entity, event, str, loopType);
    }

    public static PlayState handleGunHoldAnim(ItemStack stack, AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event) {
        return rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl.handleGunHoldAnim(stack, event);
    }

    public static PlayState handleGunActionAnim(ItemStack stack, AnimationEvent<? extends LivingAnimatable<? extends LivingEntity>> event) {
        return rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl.handleGunActionAnim(stack, event);
    }

    public static Identifier getGunTexture(ItemStack stack) {
        return rip.ysm.compat.gun.swarfare.fabric.SWarfareCompatImpl.getGunTexture(stack);
    }
}
