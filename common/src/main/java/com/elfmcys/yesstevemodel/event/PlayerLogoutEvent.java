package com.elfmcys.yesstevemodel.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import dev.architectury.event.events.common.PlayerEvent;

public final class PlayerLogoutEvent {

    private PlayerLogoutEvent() {
    }

    public static void register() {
        PlayerEvent.PLAYER_QUIT.register(player -> {
            if (!YesSteveModel.isAvailable()) {
                return;
            }
            if (NetworkHandler.isPlayerConnected(player)) {
                ServerModelManager.syncModelToPlayer(player.getUUID());
            }
        });
    }
}