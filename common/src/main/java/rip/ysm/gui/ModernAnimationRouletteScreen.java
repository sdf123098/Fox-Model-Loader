package rip.ysm.gui;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.client.event.AnimationLockEvent;
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter;
import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons;
import com.elfmcys.yesstevemodel.client.input.AnimationRouletteKey;
import com.elfmcys.yesstevemodel.client.input.ExtraAnimationKey;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.C2SPlayAnimationPacket;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import rip.ysm.api.client.KeyMappingFactory;
import rip.ysm.gpu.BlurStack;
import rip.ysm.gpu.Pie;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ModernAnimationRouletteScreen extends Screen {

    private static final Identifier settingsIcon = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/settings.png");
    private static final Identifier lockIcon = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/lock.png");
    private static final Identifier unlockIcon = Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/unlock.png");

    private static final LinkedList<Pair<String, Integer>> navigationStack = Lists.newLinkedList();
    private static String lastModelId = StringPool.EMPTY;

    private int centerX;
    private int centerY;

    private int hoveredIndex = -1;
    private int hoveredGearIndex = -1;
    private int hoveredPathSegment = -1;
    private boolean hoveredPrev;
    private boolean hoveredNext;

    private Pair<String, Integer> currentNavEntry;

    private final OrderedStringMap<String, String> currentProperties;
    private final Map<String, ExtraAnimationButtons> renderGroups;
    private final Map<String, OrderedStringMap<String, String>> textProperties;
    private final AnimatableEntity<?> animatableModel;
    private final ModelAssembly renderContext;

    public ModernAnimationRouletteScreen(String modelId, ModelAssembly modelAssembly, AnimatableEntity<?> animatable) {
        super(Component.literal("YSM Roulette"));
        this.renderContext = modelAssembly;
        this.animatableModel = animatable;
        this.textProperties = modelAssembly.getModelData().getModelProperties().getExtraAnimationClassify();
        this.renderGroups = modelAssembly.getModelData().getModelProperties().getExtraAnimationButtons();
        if (!lastModelId.equals(modelId)) {
            navigationStack.clear();
            lastModelId = modelId;
        }
        if (navigationStack.isEmpty()) navigationStack.add(MutablePair.of(StringPool.EMPTY, 0));
        this.currentNavEntry = navigationStack.peekLast();
        if (this.textProperties.containsKey(this.currentNavEntry.getLeft())) {
            this.currentProperties = this.textProperties.get(this.currentNavEntry.getLeft());
        } else {
            this.currentProperties = modelAssembly.getModelData().getModelProperties().getExtraAnimation();
            navigationStack.clear();
            navigationStack.add(MutablePair.of(StringPool.EMPTY, this.currentNavEntry.getRight()));
            this.currentNavEntry = navigationStack.peekLast();
        }
    }

    @Override
    protected void init() {
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;
        if (currentNavEntry.getRight() >= pageCount()) currentNavEntry.setValue(0);
    }

    private int pageCount() {
        return Math.max(1, (currentProperties.size() + 7) / 8);
    }

    private int page() {
        return currentNavEntry.getRight();
    }

    private float sliceStartOffset() {
        return -Pie.tau / 16.0f;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        GuiGraphicsExtractor g = extractor;
        if (GeneralConfig.BLUR_GUI != null && GeneralConfig.BLUR_GUI.get()) collectAndFlushBlur(g);

        updateHover(mouseX, mouseY);
        renderSlices(g);
        renderLabels(g);
        renderCenter(g);
        renderPageButtons(g);
        renderPathAndPage(g, mouseX, mouseY);

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }

    private void collectAndFlushBlur(GuiGraphicsExtractor g) {
        float sliceSpan = Pie.tau / 8.0f;
        for (int i = 0; i < 8; i++) {
            int absoluteIdx = i + page() * 8;
            if (absoluteIdx >= currentProperties.size()) continue;
            float start = sliceStartOffset() + i * sliceSpan + 0.02f;
            float end = sliceStartOffset() + (i + 1) * sliceSpan - 0.02f;
            BlurStack.pushBlurPie(centerX, centerY, 22.0f, 100.0f, start, end, 20.0f);
        }
        if (pageCount() > 1) {
            BlurStack.pushBlurPie(centerX - 128.0f, centerY, 0.0f, 16.0f, 0.0f, Pie.tau, 20.0f);
            BlurStack.pushBlurPie(centerX + 128.0f, centerY, 0.0f, 16.0f, 0.0f, Pie.tau, 20.0f);
        }
/*         BlurStack.flush(g); */
    }

    private void updateHover(int mouseX, int mouseY) {
        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float ang = (float) Math.atan2(dy, dx);
        if (ang < 0.0f) ang += Pie.tau;
        ang = (ang - sliceStartOffset() + Pie.tau) % Pie.tau;
        int idx = Mth.clamp((int) (ang / (Pie.tau / 8.0f)), 0, 7);

        hoveredIndex = -1;
        hoveredGearIndex = -1;
        int absoluteIdx = idx + page() * 8;
        if (absoluteIdx < currentProperties.size() && r >= 22.0f && r <= 100.0f) {
            boolean hasGear = currentProperties.getValueAt(absoluteIdx).startsWith("#");
            if (hasGear && r <= 46.0f) hoveredGearIndex = absoluteIdx;
            else hoveredIndex = absoluteIdx;
        }

        float prevDx = mouseX - (centerX - 128.0f);
        float nextDx = mouseX - (centerX + 128.0f);
        float btnDy = mouseY - centerY;
        hoveredPrev = page() > 0 && (prevDx * prevDx + btnDy * btnDy) <= 16.0f * 16.0f;
        hoveredNext = (page() + 1) * 8 < currentProperties.size() && (nextDx * nextDx + btnDy * btnDy) <= 16.0f * 16.0f;
    }

    private void renderSlices(GuiGraphicsExtractor g) {
        float sliceSpan = Pie.tau / 8.0f;
        for (int i = 0; i < 8; i++) {
            int absoluteIdx = i + page() * 8;
            if (absoluteIdx >= currentProperties.size()) {
                drawSlice(g, i, sliceSpan, 22.0f, 100.0f, 0x30000000);
                continue;
            }
            boolean isHover = absoluteIdx == hoveredIndex;
            boolean gearHover = absoluteIdx == hoveredGearIndex;
            boolean isSubmenu = currentProperties.getKeyAt(absoluteIdx).startsWith("#");
            boolean hasGear = currentProperties.getValueAt(absoluteIdx).startsWith("#");
            int mainColor = isHover ? (isSubmenu ? 0xD0FFCC00 : 0xB0FFFFFF) : (isSubmenu ? 0x70552200 : 0x60000000);
            if (hasGear) {
                int gearColor = gearHover ? 0xD0FFCC00 : 0x80333333;
                drawSlice(g, i, sliceSpan, 46.0f, 100.0f, mainColor);
                drawSlice(g, i, sliceSpan, 22.0f, 46.0f, gearColor);
                drawSettingsIcon(g, i, sliceSpan, gearHover);
            } else {
                drawSlice(g, i, sliceSpan, 22.0f, 100.0f, mainColor);
            }
        }
    }

    private void drawSlice(GuiGraphicsExtractor g, int sliceIndex, float sliceSpan, float inner, float outer, int color) {
        float start = sliceStartOffset() + sliceIndex * sliceSpan + 0.02f;
        float end = sliceStartOffset() + (sliceIndex + 1) * sliceSpan - 0.02f;
        Pie.draw(g, centerX, centerY, inner, outer, start, end, color, 1.0f);
    }

    private void drawSettingsIcon(GuiGraphicsExtractor g, int sliceIndex, float sliceSpan, boolean hover) {
        float mid = sliceStartOffset() + (sliceIndex + 0.5f) * sliceSpan;
        float r = 34.0f;
        int ix = centerX + (int) (r * Math.cos(mid)) - 8;
        int iy = centerY + (int) (r * Math.sin(mid)) - 8;
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(770, 771, 1, 0);
/*         if (hover) g.setColor(1.0f, 1.0f, 0.6f, 1.0f); */
        g.blit(settingsIcon, ix, iy, ix + 16, iy + 16, 0.0f, 1.0f, 0.0f, 1.0f);
/*         if (hover) g.setColor(1.0f, 1.0f, 1.0f, 1.0f); */
        GlStateManager._disableBlend();
    }

    private void renderLabels(GuiGraphicsExtractor g) {
        float sliceSpan = Pie.tau / 8.0f;
        for (int i = 0; i < 8; i++) {
            int absoluteIdx = i + page() * 8;
            if (absoluteIdx >= currentProperties.size()) continue;
            float midAngle = sliceStartOffset() + (i + 0.5f) * sliceSpan;
            boolean hasGear = currentProperties.getValueAt(absoluteIdx).startsWith("#");
            boolean isSubmenuLink = currentProperties.getKeyAt(absoluteIdx).startsWith("#");
            float labelR = (hasGear ? 46.0f : 22.0f) * 0.5f + 50.0f;
            int lx = centerX + (int) (labelR * Math.cos(midAngle));
            int ly = centerY + (int) (labelR * Math.sin(midAngle));
            String text = displayLabel(absoluteIdx);
            if (StringUtils.isBlank(text)) continue;
            MutableComponent comp = Component.literal(text);
            if (isSubmenuLink) comp = comp.withStyle(ChatFormatting.GOLD);
            boolean showKey = page() == 0 && navigationStack.size() == 1 && absoluteIdx < ExtraAnimationKey.KEY_MAPPINGS.size();
            int wrapWidth = (int) ((100.0f - (hasGear ? 46.0f : 22.0f)) * 0.9f);
            List<FormattedCharSequence> lines = this.font.split(comp, wrapWidth);
            int totalH = lines.size() * 9 + (showKey ? 10 : 0);
            int lineY = ly - totalH / 2;
            for (FormattedCharSequence line : lines) {
                g.centeredText(this.font, line, lx, lineY, 0xFFFFFFFF);
                lineY += 9;
            }
            if (showKey) renderKeyBinding(g, absoluteIdx, lx, lineY + 1);
        }
    }

    private void renderKeyBinding(GuiGraphicsExtractor g, int slot, int x, int y) {
        if (slot >= ExtraAnimationKey.KEY_MAPPINGS.size()) return;
        KeyMapping km = ExtraAnimationKey.KEY_MAPPINGS.get(slot);
        MutableComponent label = Component.literal("[ ").withStyle(ChatFormatting.YELLOW);
        if (km.isUnbound()) label.append(Component.translatable("key.yes_steve_model.extra_animation.none"));
        else label.append(km.getTranslatedKeyMessage());
        label.append(" ]");
        g.centeredText(this.font, label, x, y, 0xFFCFB058);
    }

    private String displayLabel(int absoluteIdx) {
        String key = currentProperties.getKeyAt(absoluteIdx);
        String value = currentProperties.getValueAt(absoluteIdx);
        String display = value;
        if (value.startsWith("#")) {
            String sub = value.substring(1);
            if (renderGroups.containsKey(sub)) display = renderGroups.get(sub).getName();
        }
        if (StringUtils.isBlank(display)) display = key;
        return ModelMetadataPresenter.getLocalizedModelString(renderContext, "properties.extra_animation.%s".formatted(key), display);
    }

    private void renderCenter(GuiGraphicsExtractor g) {
        if (animatableModel.getEntity() instanceof Player) {
            Identifier tex = AnimationLockEvent.isLocked() ? lockIcon : unlockIcon;
            GlStateManager._enableBlend();
            GlStateManager._blendFuncSeparate(770, 771, 1, 0);
            g.blit(tex, centerX - 16, centerY - 16, centerX + 16, centerY + 16, 0.0f, 1.0f, 0.0f, 1.0f);
            GlStateManager._disableBlend();
        } else {
            g.centeredText(this.font, Component.translatable("gui.yes_steve_model.roulette.stop"), centerX, centerY - 4, 0xFFFFFFFF);
        }
    }

    private void renderPageButtons(GuiGraphicsExtractor g) {
        if (pageCount() <= 1) return;
        drawPageButton(g, centerX - 128.0f, centerY, page() > 0, hoveredPrev, "<");
        drawPageButton(g, centerX + 128.0f, centerY, (page() + 1) * 8 < currentProperties.size(), hoveredNext, ">");
    }

    private void drawPageButton(GuiGraphicsExtractor g, float cx, float cy, boolean enabled, boolean hover, String arrow) {
        int color = !enabled ? 0x40000000 : (hover ? 0xD0FFFFFF : 0x90000000);
        Pie.draw(g, cx, cy, 0.0f, 16.0f, 0.0f, Pie.tau, color, 1.0f);
        int textColor = enabled ? (hover ? 0xFF000000 : 0xFFFFFFFF) : 0x60FFFFFF;
        g.centeredText(this.font, arrow, (int) cx, (int) cy - 4, textColor);
    }

    private void renderPathAndPage(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        layoutAndDrawPath(g, mouseX, mouseY);
        String pageStr = String.format("%d/%d", page() + 1, pageCount());
        g.centeredText(this.font, Component.literal(pageStr).withStyle(ChatFormatting.AQUA), centerX, centerY + 108, 0xFFFFFFFF);
    }

    private void layoutAndDrawPath(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int pathY = centerY - 118;
        String prefix = Component.translatable("gui.yes_steve_model.roulette.path.prefix").getString();
        String rootLabel = Component.translatable("gui.yes_steve_model.roulette.path.root").getString();
        int prefixW = this.font.width(prefix);
        int sep = this.font.width(" > ");
        int total = prefixW;
        for (int i = 0; i < navigationStack.size(); i++) {
            String s = navigationStack.get(i).getLeft();
            total += this.font.width(StringUtils.isBlank(s) ? rootLabel : s);
            if (i < navigationStack.size() - 1) total += sep;
        }
        int x = centerX - total / 2;
        g.text(this.font, prefix, x, pathY, 0xFFFFFFFF, true);
        x += prefixW;

        hoveredPathSegment = -1;
        for (int i = 0; i < navigationStack.size(); i++) {
            String raw = navigationStack.get(i).getLeft();
            String s = StringUtils.isBlank(raw) ? rootLabel : raw;
            int w = this.font.width(s);
            boolean isLast = i == navigationStack.size() - 1;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= pathY - 2 && mouseY < pathY + 10;
            int color = isLast ? 0xFFFFCC00 : (hover ? 0xFFFFFFFF : 0xFFAAAAAA);
            g.text(this.font, s, x, pathY, color, true);
            if (hover && !isLast) {
                g.fill(x, pathY + 9, x + w, pathY + 10, color);
                hoveredPathSegment = i;
            }
            x += w;
            if (i < navigationStack.size() - 1) {
                g.text(this.font, " > ", x, pathY, 0xFF888888, true);
                x += sep;
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean flag) {
        if (hoveredPrev) {
            playClick();
            previousPage();
            return true;
        }
        if (hoveredNext) {
            playClick();
            nextPage();
            return true;
        }
        if (hoveredPathSegment >= 0 && hoveredPathSegment < navigationStack.size() - 1) {
            playClick();
            navigateTo(hoveredPathSegment);
            return true;
        }
        if (hoveredGearIndex >= 0) {
            playClick();
            String value = currentProperties.getValueAt(hoveredGearIndex);
            if (value.startsWith("#")) {
                String sub = value.substring(1);
                if (renderGroups.containsKey(sub)) {
                    Minecraft.getInstance().setScreen(new ModelSettingsScreen(renderContext, animatableModel, this, sub));
                    return true;
                }
            }
        }
        if (hoveredIndex >= 0) {
            playClick();
            String key = currentProperties.getKeyAt(hoveredIndex);
            if ("#return".equals(key)) navigateBack();
            else if (key.startsWith("#")) navigateToSubmenu(key);
            else playAnimation(key);
            return true;
        }
        double cdx = event.x() - centerX;
        double cdy = event.y() - centerY;
        if (cdx * cdx + cdy * cdy <= 22.0 * 22.0) {
            if (animatableModel.getEntity() instanceof Player) {
                AnimationLockEvent.toggleLock();
            } else {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket.createWithIndex(animatableModel.getEntity().getId()));
                onClose();
            }
            return true;
        }
        return super.mouseClicked(event, flag);
    }

    private void navigateTo(int targetIndex) {
        while (navigationStack.size() > targetIndex + 1) navigationStack.removeLast();
        Minecraft.getInstance().setScreen(new ModernAnimationRouletteScreen(lastModelId, renderContext, animatableModel));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY < 0.0) nextPage(); else previousPage();
        return true;
    }

    private void previousPage() {
        currentNavEntry.setValue(Math.max(0, page() - 1));
    }

    private void nextPage() {
        if ((page() + 1) * 8 < currentProperties.size()) currentNavEntry.setValue(page() + 1);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (KeyMappingFactory.isActiveAndMatches(AnimationRouletteKey.KEY_ROULETTE, event.key(), event.scancode())) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private void navigateToSubmenu(String value) {
        if (navigationStack.size() > 5) {
            LocalPlayer p = Minecraft.getInstance().player;
            if (p != null) p.sendSystemMessage(Component.translatable("gui.yes_steve_model.roulette.too_long"));
            return;
        }
        String sub = value.substring(1);
        if (textProperties.get(sub) != null) {
            navigationStack.addLast(MutablePair.of(sub, 0));
            Minecraft.getInstance().setScreen(new ModernAnimationRouletteScreen(lastModelId, renderContext, animatableModel));
        }
    }

    private void navigateBack() {
        if (navigationStack.size() > 1) {
            navigationStack.removeLast();
            Minecraft.getInstance().setScreen(new ModernAnimationRouletteScreen(lastModelId, renderContext, animatableModel));
            return;
        }
        Minecraft.getInstance().setScreen(null);
    }

    private void playAnimation(String key) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (NetworkHandler.isClientConnected()) {
            Pair<String, Integer> last = navigationStack.peekLast();
            String submenu = (last != null && StringUtils.isNotBlank(last.getLeft())) ? last.getLeft() : StringPool.EMPTY;
            Entity entity = animatableModel.getEntity();
            if (entity instanceof Player) NetworkHandler.sendToServer(new C2SPlayAnimationPacket(hoveredIndex, submenu));
            else NetworkHandler.sendToServer(new C2SPlayAnimationPacket(hoveredIndex, submenu, entity.getId()));
        } else if (player != null) {
            PlayerCapability.get(player).ifPresent(cap -> cap.requestModelSwitch(key));
        }
        if (player != null && GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG.get()) {
            player.sendSystemMessage(Component.translatable("message.yes_steve_model.model.animation_roulette.play", key));
        }
        Minecraft.getInstance().setScreen(null);
    }

    private void playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
