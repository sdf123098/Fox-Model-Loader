package com.elfmcys.yesstevemodel.client.input;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.gui.ExtraPlayerConfigScreen;
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen;
import com.elfmcys.yesstevemodel.config.ServerConfig;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.util.InputUtil;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import rip.ysm.api.PlatformAPI;
import rip.ysm.api.client.KeyMappingFactory;

public final class PlayerModelToggleKey {

    public static final KeyMapping KEY_MAPPING = KeyMappingFactory.createInGameAlt("key.yes_steve_model.player_model.desc", InputConstants.Type.KEYSYM, 89, "key.category.yes_steve_model");

    private PlayerModelToggleKey() {
    }

    public static void register() {
        if (PlatformAPI.isServer()) {
            return;
        }
        ClientRawInputEvent.KEY_PRESSED.register((client, keyCode, scanCode, action, modifiers) -> {
            return onKeyInput(action, keyCode, scanCode) ? EventResult.interruptFalse() : EventResult.pass();
        });
    }

    private static boolean onKeyInput(int action, int keyCode, int scanCode) {
        if (action != 1 || !InputUtil.isKeyPressed(keyCode, scanCode, KEY_MAPPING)) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof PlayerModelScreen screen) {
            if (screen.shouldCloseWithToggleKey()) {
                screen.onClose();
                return true;
            }
            return false;
        }
        if (!InputUtil.isPlayerReady()) {
            return false;
        }
        if (!YesSteveModel.isAvailable()) {
            YesSteveModel.sendUnavailableMessage();
            return true;
        }
        if (NetworkHandler.isClientConnected() && !ServerConfig.CAN_SWITCH_MODEL.get()) {
            minecraft.setScreen(new ExtraPlayerConfigScreen(null));
        } else {
            minecraft.setScreen(new PlayerModelScreen());
        }
        return true;
    }
}
