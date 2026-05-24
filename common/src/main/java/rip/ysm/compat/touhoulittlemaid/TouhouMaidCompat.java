package rip.ysm.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.network.message.FeedbackData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

public final class TouhouMaidCompat {

    private TouhouMaidCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidCompatImpl.isLoaded();
    }

    public static void init() {
        rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidCompatImpl.init();
    }

    public static boolean isMaidEntity(Entity entity) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidCompatImpl.isMaidEntity(entity);
    }

    public static void handleProjectileOwner(Projectile projectile, Entity entity) {
        rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidCompatImpl.handleProjectileOwner(projectile, entity);
    }

    public static void registerAnimationRoulette(Entity entity, String str, int i) {
        rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidCompatImpl.registerAnimationRoulette(entity, str, i);
    }

    public static void applyFeedback(Entity entity, FeedbackData message) {
        rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidCompatImpl.applyFeedback(entity, message);
    }

    @Environment(EnvType.CLIENT)
    public static void playMaidAnimation(Entity entity, String str) {
        rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidCompatImpl.playMaidAnimation(entity, str);
    }
}
