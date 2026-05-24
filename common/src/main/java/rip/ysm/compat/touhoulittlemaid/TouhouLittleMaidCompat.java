package rip.ysm.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.client.animation.molang.TLMBinding;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public final class TouhouLittleMaidCompat {

    private TouhouLittleMaidCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isLoaded();
    }

    public static boolean isMaidEntity(Entity entity) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidEntity(entity);
    }

    public static boolean isMaidRideable(Entity entity) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidRideable(entity);
    }

    public static boolean isSimplePlanesEntity(Entity entity) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isSimplePlanesEntity(entity);
    }

    public static boolean isImmersiveAircraftEntity(Entity entity) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isImmersiveAircraftEntity(entity);
    }

    public static boolean isMaidItem(Item item) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidItem(item);
    }

    public static String getMaidEntityId(Entity entity) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.getMaidEntityId(entity);
    }

    public static boolean isMaidSitting(LivingEntity livingEntity) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidSitting(livingEntity);
    }

    public static void registerMaidAnimStates(TLMBinding tlmBinding) {
        rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.registerMaidAnimStates(tlmBinding);
    }

    public static PlayState handleMaidInteraction(AnimationEvent<LivingAnimatable<?>> event, LivingEntity livingEntity, Entity entity) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.handleMaidInteraction(event, livingEntity, entity);
    }

    public static boolean isMaidChatAvailable() {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.isMaidChatAvailable();
    }

    public static void openMaidChat() {
        rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.openMaidChat();
    }

    public static Object buildControllers(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl.buildControllers(modelBundle, resourceBundle);
    }
}
