package com.micaftic.morpher.core.gui.components.groups;

import net.minecraft.network.chat.Component;
import com.micaftic.morpher.core.gui.OptionGroup;

public class CategoryGroup extends OptionGroup {
    private final String catKey;

    public CategoryGroup(String catKey) {
        super("animation_category." + catKey);
        this.catKey = catKey;
    }

    @Override
    public Component getTitle() {
        String key = "gui.foxmodelloader.animation.category." + catKey;
        return Component.translatable(key);
    }
}
