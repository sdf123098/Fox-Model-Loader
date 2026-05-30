package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.client.upload.ModelImportFilePicker;
import com.elfmcys.yesstevemodel.client.upload.ModelUploadSession;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.PlatformUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.List;
import java.util.Locale;

public class ModelUploadScreen extends Screen implements ModelUploadSession.Listener {
    private final Screen parentScreen;
    private final Queue<ModelImportFilePicker.PickedFile> pendingImports = new ArrayDeque<>();
    private long lastFlashTime = 0L;
    private Component error = Component.empty();
    private float displayedProgress = 0f;
    private float prevProgressTarget = -1f;

    public ModelUploadScreen(Screen parent) {
        super(Component.translatable("gui.yes_steve_model.import.title"));
        this.parentScreen = parent;
    }

    private static void drawBorder(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int w, int color) {
        g.fill(x1, y1, x2, y1 + w, color);
        g.fill(x1, y2 - w, x2, y2, color);
        g.fill(x1, y1, x1 + w, y2, color);
        g.fill(x2 - w, y1, x2, y2, color);
    }

    @Override
    public void init() {
        clearWidgets();
        ModelUploadSession.addListener(this);
        int buttonY = 10;
        addRenderableWidget(new FlatColorButton(this.width - 350, buttonY, 130, 18, Component.translatable("gui.yes_steve_model.import.choose_file"), button -> openFilePicker()));
        addRenderableWidget(new FlatColorButton(this.width - 210, buttonY, 130, 18, Component.translatable("gui.yes_steve_model.open_model_folder.open"), button -> openModelFolder()));
        addRenderableWidget(new FlatColorButton(this.width - 70, buttonY, 60, 18, Component.translatable("gui.yes_steve_model.model.return"), button -> Minecraft.getInstance().setScreen(this.parentScreen)));
    }

    @Override
    public void removed() {
        ModelUploadSession.removeListener(this);
        ModelUploadSession.clearIfTerminal();
        ModelImportFilePicker.cancelPicking();
    }

    @Override
    public void onSessionUpdate(ModelUploadSession session) {
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (paths.isEmpty()) {
            return;
        }
        this.error = Component.empty();
        this.lastFlashTime = PlatformUtil.getMillis();
        for (Path path : paths) {
            enqueuePath(path);
        }
        startNextImportIfIdle();
    }

    private void openFilePicker() {
        this.error = Component.empty();
        this.lastFlashTime = PlatformUtil.getMillis();
        Component err = ModelImportFilePicker.pickYsmFile();
        if (err != null) {
            this.error = err;
        }
    }

    private void enqueuePath(Path path) {
        String fileName = path.getFileName().toString();
        try {
            if (Files.isDirectory(path)) {
                this.pendingImports.add(ModelImportFilePicker.packDirectory(path));
                return;
            }
            if (!ModelImportFilePicker.isImportFileName(fileName)) {
                this.error = Component.translatable("gui.yes_steve_model.import.error.invalid_extension");
                return;
            }
            this.pendingImports.add(new ModelImportFilePicker.PickedFile(fileName, Files.readAllBytes(path)));
        } catch (IOException e) {
            this.error = Component.translatable("gui.yes_steve_model.import.error.read_file", e.getMessage());
        }
    }

    private boolean importPickedFile(ModelImportFilePicker.PickedFile file) {
        this.error = Component.empty();
        this.lastFlashTime = PlatformUtil.getMillis();
        String fileName = file.fileName() == null ? "imported.ysm" : file.fileName();
        if (!ModelImportFilePicker.isImportFileName(fileName)) {
            this.error = Component.translatable("gui.yes_steve_model.import.error.invalid_extension");
            return false;
        }
        ModelUploadSession existing = ModelUploadSession.getInstance();
        if (existing != null && !existing.isTerminal()) {
            this.error = Component.translatable("gui.yes_steve_model.import.error.in_progress");
            return false;
        }
        String stem = stripImportExtension(fileName);
        String modelId = stem.toLowerCase(Locale.ROOT);
        if (modelId.isEmpty()) {
            this.error = Component.translatable("gui.yes_steve_model.import.error.model_id_from_filename", stem);
            return false;
        }
        Component err = ModelUploadSession.start(modelId, fileName, file.data());
        if (err != null) {
            this.error = err;
            return false;
        }
        return true;
    }

    private void openModelFolder() {
        try {
            Files.createDirectories(ServerModelManager.CUSTOM);
            PlatformUtil.openFile(ServerModelManager.CUSTOM.toFile());
        } catch (IOException e) {
            this.error = Component.translatable("gui.yes_steve_model.import.error.open_folder", e.getMessage());
        }
    }

    private static String stripImportExtension(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        for (String extension : new String[]{".ysm", ".zip", ".7z"}) {
            if (lower.endsWith(extension)) {
                return fileName.substring(0, fileName.length() - extension.length());
            }
        }
        return fileName;
    }

    private void startNextImportIfIdle() {
        ModelUploadSession existing = ModelUploadSession.getInstance();
        if (existing != null && !existing.isTerminal()) {
            return;
        }
        ModelImportFilePicker.PickedFile next = this.pendingImports.poll();
        if (next != null && !importPickedFile(next)) {
            this.pendingImports.clear();
        }
    }

    @Override
    public void tick() {
        ModelImportFilePicker.PickedFile pickedFile;
        while ((pickedFile = ModelImportFilePicker.pollCompleted()) != null) {
            this.pendingImports.add(pickedFile);
        }
        startNextImportIfIdle();
        Component pickerError = ModelImportFilePicker.consumeLastError();
        if (!pickerError.getString().isEmpty()) {
            this.error = pickerError;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        GuiGraphicsExtractor g = extractor;
        g.fill(0, 0, this.width, this.height, 0xC0000000);

        long sinceFlash = PlatformUtil.getMillis() - this.lastFlashTime;
        int borderColor;
        int borderWidth;
        if (sinceFlash < 900) {
            float t = 1f - (float) sinceFlash / 900;
            int alpha = Math.min(255, Math.max(0, (int) (255 * t)));
            borderColor = (alpha << 24) | 0x00FFC107;
            borderWidth = 4;
        } else {
            borderColor = 0x66808080;
            borderWidth = 2;
        }
        drawBorder(g, 0, 0, this.width, this.height, borderWidth, borderColor);

        ModelUploadSession session = ModelUploadSession.getInstance();
        if (session == null) {
            renderEmptyState(g);
        } else {
            renderSessionState(g, session);
        }

        if (!this.error.getString().isEmpty()) {
            MutableComponent err = this.error.copy().withStyle(ChatFormatting.RED);
            int w = this.font.width(err);
            g.text(this.font, err, (this.width - w) / 2, this.height - 60, 0xFFFFFFFF);
        }

        super.extractRenderState(extractor, mouseX, mouseY, partialTick);
    }

    private void renderEmptyState(GuiGraphicsExtractor guiGraphics) {
        MutableComponent main = Component.translatable(ModelImportFilePicker.isPicking() ? "gui.yes_steve_model.import.select_in_manager" : "gui.yes_steve_model.import.empty").withStyle(ChatFormatting.WHITE);
        MutableComponent sub = Component.translatable("gui.yes_steve_model.import.standalone_only").withStyle(ChatFormatting.GRAY);
        int cx = this.width / 2;
        int cy = this.height / 2;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(cx, cy - 14);
        guiGraphics.pose().scale(2.0f, 2.0f);
        int mw = this.font.width(main);
        guiGraphics.text(this.font, main, -mw / 2, 0, 0xFFFFFFFF);
        guiGraphics.pose().popMatrix();
        int sw = this.font.width(sub);
        guiGraphics.text(this.font, sub, cx - sw / 2, cy + 22, 0xFFAAAAAA);
        if (ModelUploadSession.hasServerLimits()) {
            MutableComponent limit = Component.translatable("gui.yes_steve_model.import.size_limit", ModelUploadSession.formatBytes(ModelUploadSession.getLastMaxTotalBytes())).withStyle(ChatFormatting.DARK_GRAY);
            int lw = this.font.width(limit);
            guiGraphics.text(this.font, limit, cx - lw / 2, cy + 36, 0xFFFFFFFF);
        }
    }

    private void renderSessionState(GuiGraphicsExtractor guiGraphics, ModelUploadSession session) {
        int cx = this.width / 2;
        int cy = this.height / 2;
        ChatFormatting color = switch (session.getState()) {
            case COMPLETED -> ChatFormatting.GREEN;
            case FAILED -> ChatFormatting.RED;
            default -> ChatFormatting.YELLOW;
        };
        Component title = session.getMessage().copy().withStyle(color);
        int tw = this.font.width(title);
        guiGraphics.text(this.font, title, cx - tw / 2, cy - 32, 0xFFFFFFFF);

        Component sub = Component.literal(session.getModelId()).withStyle(ChatFormatting.GRAY);
        int sw = this.font.width(sub);
        guiGraphics.text(this.font, sub, cx - sw / 2, cy - 16, 0xFFFFFFFF);

        int barW = 320;
        int barH = 14;
        int barX = cx - barW / 2;
        int barY = cy + 4;
        float target = session.getProgress();
        if (target < prevProgressTarget) {
            displayedProgress = target;
        }
        prevProgressTarget = target;
        displayedProgress += (target - displayedProgress) * 0.18f;
        if (Math.abs(target - displayedProgress) < 0.001f) {
            displayedProgress = target;
        }
        int fillW = (int) (barW * displayedProgress);
        int fillColor;
        if (session.getState() == ModelUploadSession.State.FAILED) {
            fillColor = 0xFFD23232;
        } else if (session.getState() == ModelUploadSession.State.COMPLETED) {
            fillColor = 0xFF4CAF50;
        } else {
            fillColor = 0xFFFFC107;
        }
        guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF2A2A2A);
        if (fillW > 0) {
            guiGraphics.fill(barX, barY, barX + fillW, barY + barH, fillColor);
        }
        if (session.getState() == ModelUploadSession.State.UPLOADING && fillW > 4) {
            long now = PlatformUtil.getMillis();
            int period = 1400;
            int travel = fillW + 40;
            int shimmerX = (int) (((now % period) / (float) period) * travel) - 20;
            int shimmerW = 24;
            int left = barX + Math.max(0, shimmerX);
            int right = barX + Math.min(fillW, shimmerX + shimmerW);
            if (right > left) {
                guiGraphics.fill(left, barY + 1, right, barY + barH - 1, 0x55FFFFFF);
            }
        }
        guiGraphics.fill(barX, barY, barX + barW, barY + 1, -1);
        guiGraphics.fill(barX, barY + barH - 1, barX + barW, barY + barH, -1);
        guiGraphics.fill(barX, barY, barX + 1, barY + barH, -1);
        guiGraphics.fill(barX + barW - 1, barY, barX + barW, barY + barH, -1);

        String stat = ModelUploadSession.formatBytes(session.getSentBytes()) + " / " + ModelUploadSession.formatBytes(session.getTotalBytes());
        int statW = this.font.width(stat);
        guiGraphics.text(this.font, stat, cx - statW / 2, barY + barH + 6, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parentScreen);
    }
}
