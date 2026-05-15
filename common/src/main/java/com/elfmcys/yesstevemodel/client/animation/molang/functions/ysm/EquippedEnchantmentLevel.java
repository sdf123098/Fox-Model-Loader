package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EquippedEnchantmentLevel extends LivingEntityFunction {
    @Override
    public Object eval(ExecutionContext<IContext<LivingEntity>> context, ArgumentCollection arguments) {
        EquipmentSlot slotType = MolangUtils.parseSlotType(context.entity(), arguments.getAsString(context, 0));
        if (slotType == null) {
            return null;
        }
        ItemStack stack = CosmeticArmorHelper.getArmorItem(context.entity().entity(), slotType);
        if (stack.isEmpty()) {
            return 0;
        }
        int enchantmentLevel = 0;
        for (int i = 1; i < arguments.size(); i++) {
            ResourceLocation id = arguments.getResourceLocation(context, 1);
            Enchantment raw = context.entity().entity().level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).get(id);
            if (id != null && raw != null) {
                enchantmentLevel += EnchantmentHelper.getItemEnchantmentLevel(new Holder.Direct<>(raw), stack);
            }
        }
        return enchantmentLevel;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size >= 2;
    }
}
