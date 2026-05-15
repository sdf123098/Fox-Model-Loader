package rip.ysm.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import rip.ysm.gui.components.buttons.FooterButton;
import rip.ysm.gui.components.buttons.TabButton;

import java.util.ArrayList;
import java.util.List;

public abstract class OptionScreen extends Screen {
    @Nullable
    protected final Screen parentScreen;
    protected final List<OptionGroup> groups = new ArrayList<>();
    protected final List<TabButton> tabButtons = new ArrayList<>();
    protected final List<OptionRow<?>> activeRows = new ArrayList<>();
    protected OptionGroup activeGroup;
    @Nullable
    protected OptionRow<?> hoveredRow;

    protected int panelLeft;
    protected int panelTop;
    protected int panelRight;
    protected int panelBottom;

    protected FooterButton applyBtn;
    protected FooterButton undoBtn;
    protected FooterButton saveBtn;
    protected FooterButton cancelBtn;

    public OptionScreen(Component title, @Nullable Screen parent) {
        super(title);
        this.parentScreen = parent;
    }

    protected abstract void registerGroups();

    @Override
    protected void init() {
        groups.clear();
        tabButtons.clear();
        registerGroups();

        int totalWidth = Math.min(this.width - 40, 540);
        int totalHeight = Math.min(this.height - 40, 320);
        panelLeft = (this.width - totalWidth) / 2;
        panelTop = (this.height - totalHeight) / 2;
        panelRight = panelLeft + totalWidth;
        panelBottom = panelTop + totalHeight;

        int tabX = panelLeft;
        int tabY = panelTop + 6 + 18;
        for (OptionGroup g : groups) {
            TabButton tb = new TabButton(tabX, tabY, 110, 22, g, this::selectGroup);
            tabButtons.add(tb);
            addRenderableWidget(tb);
            tabY += 22;
        }

        int footerY = panelBottom - 38;
        int btnW = 70;
        int btnH = 20;
        int gap = 4;
        cancelBtn = new FooterButton(panelRight - btnW - 6, footerY, btnW, btnH, Component.translatable("gui.yes_steve_model.config.cancel"), this::onCancel);
        saveBtn = new FooterButton(cancelBtn.getX() - btnW - gap, footerY, btnW, btnH, Component.translatable("gui.yes_steve_model.config.save"), this::onSave);
        applyBtn = new FooterButton(saveBtn.getX() - btnW - gap, footerY, btnW, btnH, Component.translatable("gui.yes_steve_model.config.apply"), this::onApply);
        undoBtn = new FooterButton(panelLeft + 6, footerY, btnW, btnH, Component.translatable("gui.yes_steve_model.config.undo"), this::onUndo);
        addRenderableWidget(undoBtn);
        addRenderableWidget(applyBtn);
        addRenderableWidget(saveBtn);
        addRenderableWidget(cancelBtn);

        if (!groups.isEmpty()) selectGroup(groups.get(0));
    }

    protected void selectGroup(OptionGroup group) {
        if (activeGroup == group && !activeRows.isEmpty()) return;
        for (OptionRow<?> row : activeRows) this.removeWidget(row);
        activeRows.clear();
        activeGroup = group;
        for (TabButton tb : tabButtons) tb.setSelected(tb.getGroup() == group);

        int rowX = panelLeft + 110 + 6;
        int rowY = panelTop + 6 + 18;
        int rowW = panelRight - rowX - 6;
        for (OptionRow<?> template : group.getRows()) {
            template.setX(rowX);
            template.setY(rowY);
            template.setWidth(rowW);
            activeRows.add(template);
            addRenderableWidget(template);
            rowY += 24;
        }
    }

    protected boolean anyDirty() {
        for (OptionGroup g : groups) if (g.isDirty()) return true;
        return false;
    }

    protected void onApply() {
        for (OptionGroup g : groups) g.apply();
    }

    protected void onSave() {
        onApply();
        Minecraft.getInstance().setScreen(parentScreen);
    }

    protected void onCancel() {
        for (OptionGroup g : groups) g.undo();
        Minecraft.getInstance().setScreen(parentScreen);
    }

    protected void onUndo() {
        if (activeGroup != null) activeGroup.undo();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(g);

        g.fill(panelLeft, panelTop, panelRight, panelTop + 18, 0x90000000);
        g.drawString(this.font, this.title, panelLeft + 6, panelTop + 5, 0xFFFFFFFF, false);

        int descY = panelBottom - 32 - 56;

        hoveredRow = null;
        for (OptionRow<?> row : activeRows) {
            if (mouseX >= row.getX() && mouseX < row.getX() + row.getWidth() && mouseY >= row.getY() && mouseY < row.getY() + row.getHeight()) {
                hoveredRow = row;
                break;
            }
        }

        boolean dirty = anyDirty();
        applyBtn.active = dirty;
        undoBtn.active = activeGroup != null && activeGroup.isDirty();

        super.render(g, mouseX, mouseY, partialTick);

        renderDescription(g, descY);
    }

    protected void renderDescription(GuiGraphics g, int descY) {
        if (hoveredRow == null || hoveredRow.getOption() == null) return;
        g.fill(panelLeft, descY, panelRight, descY + 28, 0x80000000);
        Option<?> opt = hoveredRow.getOption();
        Component title = opt.getLabel();
        g.drawString(this.font, title, panelLeft + 6, descY + 4, -1, false);

        Component desc = opt.getDescription();
        int maxWidth = panelRight - panelLeft - 6 * 2;
        List<FormattedCharSequence> lines = this.font.split(desc, maxWidth);
        int lineY = descY + 16;
        int max = Math.min(lines.size(), (28 - 16) / 10);
        for (int i = 0; i < max; i++) {
            g.drawString(this.font, lines.get(i), panelLeft + 6, lineY, 0xFFCCCCCC, false);
            lineY += 10;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        onCancel();
    }
}
