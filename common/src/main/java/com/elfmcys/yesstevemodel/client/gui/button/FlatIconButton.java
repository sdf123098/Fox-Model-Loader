package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.client.gui.ISpecialWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;


@Environment(EnvType.CLIENT)
public class FlatIconButton extends AbstractWidget implements ISpecialWidget {

    private final int iconIndex;

    public FlatIconButton(int x, int y, int iconIndex, Component component) {
        super(x, y, 115, 15, component);
        this.iconIndex = iconIndex;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        GuiGraphicsExtractor guiGraphics = extractor;
        guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + this.iconIndex, -280804798);
/*         GuiGraphicsExtractor.renderScrollingString(Minecraft.getInstance().font, getMessage(), getX() + 2, getY(), getX() + getWidth() - 2, getY() + getHeight(), 16777215); */
    }

    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}