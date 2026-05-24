package rip.ysm.compat.playeranimator;

import net.minecraft.client.player.AbstractClientPlayer;

public final class PlayerAnimatorCompat {

    private PlayerAnimatorCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.playeranimator.fabric.PlayerAnimatorCompatImpl.isLoaded();
    }

    public static boolean isPlayerAnimated(AbstractClientPlayer abstractClientPlayer) {
        return rip.ysm.compat.playeranimator.fabric.PlayerAnimatorCompatImpl.isPlayerAnimated(abstractClientPlayer);
    }
}
