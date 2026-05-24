package rip.ysm.compat.slashblade;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class SlashBladeCompat {

    private SlashBladeCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.slashblade.fabric.SlashBladeCompatImpl.isLoaded();
    }

    public static boolean isSlashBladeItem(ItemStack itemStack) {
        return rip.ysm.compat.slashblade.fabric.SlashBladeCompatImpl.isSlashBladeItem(itemStack);
    }

    public static String getComboAnimName(AnimationEvent<? extends LivingAnimatable<?>> event) {
        return rip.ysm.compat.slashblade.fabric.SlashBladeCompatImpl.getComboAnimName(event);
    }

    public static PlayState handleSlashBladeAnim(LivingEntity livingEntity, AnimationEvent<? extends LivingAnimatable<?>> event, String str, ILoopType loopType) {
        return rip.ysm.compat.slashblade.fabric.SlashBladeCompatImpl.handleSlashBladeAnim(livingEntity, event, str, loopType);
    }

    public static void registerControllerFunctions(CtrlBinding ctrlBinding) {
        rip.ysm.compat.slashblade.fabric.SlashBladeCompatImpl.registerControllerFunctions(ctrlBinding);
    }

    public static boolean hasNewApi() {
        return rip.ysm.compat.slashblade.fabric.SlashBladeCompatImpl.hasNewApi();
    }
}
