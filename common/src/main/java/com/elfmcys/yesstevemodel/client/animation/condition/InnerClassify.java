package com.elfmcys.yesstevemodel.client.animation.condition;

import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat;
import rip.ysm.compat.slashblade.SlashBladeCompat;
import com.elfmcys.yesstevemodel.util.ItemTagsConstants;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InnerClassify {

    private static final String EMPTY = "";

    public static String doClassifyTest(String str, LivingEntity livingEntity, InteractionHand interactionHand) {
        String itemType = getItemType(livingEntity.getItemInHand(interactionHand));
        if (!itemType.equals("")) {
            return str + itemType;
        }
        return "";
    }

    public static String getItemType(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return EMPTY;
        }
        Item item = itemStack.getItem();
        if (SlashBladeCompat.isSlashBladeItem(itemStack)) {
            return "slashblade";
        }
        if (itemStack.is(ItemTagsConstants.SWORDS)) {
            return "sword";
        }
        if (TouhouLittleMaidCompat.isMaidItem(item)) {
            return "gohei";
        }
        if (itemStack.is(ItemTagsConstants.AXES)) {
            return "axe";
        }
        if (itemStack.is(ItemTagsConstants.PICKAXES)) {
            return "pickaxe";
        }
        if (itemStack.is(ItemTagsConstants.SHOVELS)) {
            return "shovel";
        }
        if (itemStack.is(ItemTagsConstants.HOES)) {
            return "hoe";
        }
        if (item == Items.SHIELD || itemStack.is(ItemTagsConstants.SHIELDS)) {
            return "shield";
        }
        if (item == Items.CROSSBOW || itemStack.is(ItemTagsConstants.CROSSBOWS)) {
            return "crossbow";
        }
        if (item == Items.BOW || itemStack.is(ItemTagsConstants.BOWS)) {
            return "bow";
        }
        if (item == Items.FISHING_ROD || itemStack.is(ItemTagsConstants.FISHING_RODS)) {
            return "fishing_rod";
        }
        if (item == Items.TRIDENT || itemStack.is(ItemTagsConstants.TRIDENTS)) {
            return "spear";
        }
        if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || itemStack.is(ItemTagsConstants.THROWABLE_POTION)) {
            return "throwable_potion";
        }
        return EMPTY;
    }
}
