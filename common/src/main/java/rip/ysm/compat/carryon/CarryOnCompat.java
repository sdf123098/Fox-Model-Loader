package rip.ysm.compat.carryon;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.BiFunction;

public final class CarryOnCompat {

    private CarryOnCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.carryon.fabric.CarryOnCompatImpl.isLoaded();
    }

    public static Optional<BiFunction<String, CustomPlayerEntity, IAnimationController<CustomPlayerEntity>>> getControllerFactory() {
        return rip.ysm.compat.carryon.fabric.CarryOnCompatImpl.getControllerFactory();
    }

    public static boolean isPlayerCarrying(Player player) {
        return rip.ysm.compat.carryon.fabric.CarryOnCompatImpl.isPlayerCarrying(player);
    }

    public static void registerBindings(CtrlBinding binding) {
        rip.ysm.compat.carryon.fabric.CarryOnCompatImpl.registerBindings(binding);
    }
}
