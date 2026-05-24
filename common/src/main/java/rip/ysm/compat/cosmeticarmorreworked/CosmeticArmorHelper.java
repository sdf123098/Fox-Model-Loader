package rip.ysm.compat.cosmeticarmorreworked;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class CosmeticArmorHelper {

    private CosmeticArmorHelper() {
    }

    public static ItemStack getArmorItem(LivingEntity entity, EquipmentSlot slot) {
        return rip.ysm.compat.cosmeticarmorreworked.fabric.CosmeticArmorHelperImpl.getArmorItem(entity, slot);
    }

    public static ItemStack getElytraItem(LivingEntity livingEntity) {
        return rip.ysm.compat.cosmeticarmorreworked.fabric.CosmeticArmorHelperImpl.getElytraItem(livingEntity);
    }
}
