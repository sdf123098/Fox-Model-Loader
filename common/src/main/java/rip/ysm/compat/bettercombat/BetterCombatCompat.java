package rip.ysm.compat.bettercombat;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;

public final class BetterCombatCompat {

    private BetterCombatCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.bettercombat.fabric.BetterCombatCompatImpl.isLoaded();
    }

    public static void registerBindings(CtrlBinding binding) {
        rip.ysm.compat.bettercombat.fabric.BetterCombatCompatImpl.registerBindings(binding);
    }
}
