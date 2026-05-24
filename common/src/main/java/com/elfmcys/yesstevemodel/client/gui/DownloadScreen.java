package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


public class DownloadScreen extends Screen {

    private final PlayerModelScreen parentScreen;

    private int guiLeft;

    private int guiTop;

    public DownloadScreen(PlayerModelScreen modelScreen) {
        super(Component.literal("YSM Config GUI"));
        this.parentScreen = modelScreen;
    }

    public void init() {
        this.guiLeft = (this.width - 420) / 2;
        this.guiTop = (this.height - 235) / 2;
        addRenderableWidget(new FlatColorButton(this.guiLeft + 5, this.guiTop, 80, 18, Component.translatable("gui.yes_steve_model.model.return"), button -> {
            Minecraft.getInstance().setScreen(this.parentScreen);
        }));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        GuiGraphicsExtractor guiGraphics = extractor;
        extractTransparentBackground(extractor);
        guiGraphics.centeredText(this.font, Component.literal("Coming Soooooooooooooooooooooooooon™"), this.width / 2, (this.height / 2) - 5, ChatFormatting.DARK_RED.getColor().intValue());
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }
}
