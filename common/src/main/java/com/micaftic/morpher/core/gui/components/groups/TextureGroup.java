package com.micaftic.morpher.core.gui.components.groups;

import net.minecraft.network.chat.Component;

public final class TextureGroup extends CategoryGroup {
    public TextureGroup() {
        super("_textures");
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.foxmodelloader.animation.category._textures");
    }
}
