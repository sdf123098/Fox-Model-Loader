package com.micaftic.morpher.client.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/** Client-only compatibility for Tweakeroo's detached camera, without a hard dependency. */
public final class TweakerooCompat {
    private static final ClassValue<Boolean> CAMERA_TYPES = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                if ("fi.dy.masa.tweakeroo.util.CameraEntity".equals(current.getName())) {
                    return true;
                }
            }
            return false;
        }
    };

    private TweakerooCompat() {
    }

    public static boolean isCameraProxy(Entity entity) {
        return entity != null && CAMERA_TYPES.get(entity.getClass());
    }

    public static boolean isSpectatorBody(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (player == minecraft.player
                && isCameraProxy(minecraft.getCameraEntity())
                && minecraft.gameMode != null) {
            // Tweakeroo may spoof Player.isSpectator() while updating terrain.
            // Model visibility must follow the body's real server game mode.
            return minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR;
        }
        return player.isSpectator();
    }
}
