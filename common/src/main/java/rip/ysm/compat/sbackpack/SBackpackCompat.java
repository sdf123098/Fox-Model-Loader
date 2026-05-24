package rip.ysm.compat.sbackpack;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public final class SBackpackCompat {

    private SBackpackCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.sbackpack.fabric.SBackpackCompatImpl.isLoaded();
    }

    public static void setupRenderLayers() {
        rip.ysm.compat.sbackpack.fabric.SBackpackCompatImpl.setupRenderLayers();
    }

    public static Optional<Pair<String, String>> getInCompatibleInfo() {
        return rip.ysm.compat.sbackpack.fabric.SBackpackCompatImpl.getInCompatibleInfo();
    }

    public static void registerControllerFunctions(CtrlBinding binding) {
        rip.ysm.compat.sbackpack.fabric.SBackpackCompatImpl.registerControllerFunctions(binding);
    }

    public static ItemStack getBackpackItem(LivingEntity livingEntity) {
        return rip.ysm.compat.sbackpack.fabric.SBackpackCompatImpl.getBackpackItem(livingEntity);
    }
}
