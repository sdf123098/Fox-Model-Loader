package rip.ysm.compat.create;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.player.Player;

public final class CreateCompat {

    private CreateCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.create.fabric.CreateCompatImpl.isLoaded();
    }

    public static boolean isPlayerOnCreateContraption(Player player) {
        return rip.ysm.compat.create.fabric.CreateCompatImpl.isPlayerOnCreateContraption(player);
    }

    public static void registerCreateFunctions(CtrlBinding binding) {
        rip.ysm.compat.create.fabric.CreateCompatImpl.registerCreateFunctions(binding);
    }
}
