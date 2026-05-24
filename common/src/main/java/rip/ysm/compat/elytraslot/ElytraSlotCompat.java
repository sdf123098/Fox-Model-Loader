package rip.ysm.compat.elytraslot;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class ElytraSlotCompat {

    private ElytraSlotCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.elytraslot.fabric.ElytraSlotCompatImpl.isLoaded();
    }

    public static ItemStack getElytraItem(LivingEntity livingEntity) {
        return rip.ysm.compat.elytraslot.fabric.ElytraSlotCompatImpl.getElytraItem(livingEntity);
    }
}
