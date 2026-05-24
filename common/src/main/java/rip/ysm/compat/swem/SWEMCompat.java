package rip.ysm.compat.swem;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;

public final class SWEMCompat {

    private SWEMCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.swem.fabric.SWEMCompatImpl.isLoaded();
    }

    public static String getHorseGaitName(LivingEntity livingEntity) {
        return rip.ysm.compat.swem.fabric.SWEMCompatImpl.getHorseGaitName(livingEntity);
    }

    public static void registerControllerFunctions(CtrlBinding ctrlBinding) {
        rip.ysm.compat.swem.fabric.SWEMCompatImpl.registerControllerFunctions(ctrlBinding);
    }
}
