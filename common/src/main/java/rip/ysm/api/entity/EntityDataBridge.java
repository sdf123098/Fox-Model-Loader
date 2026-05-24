package rip.ysm.api.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public final class EntityDataBridge {

    private EntityDataBridge() {
    }

    public static CompoundTag getPersistentData(Entity entity) {
        return rip.ysm.api.entity.fabric.EntityDataBridgeImpl.getPersistentData(entity);
    }

    public static boolean shouldRiderSit(Entity vehicle) {
        return rip.ysm.api.entity.fabric.EntityDataBridgeImpl.shouldRiderSit(vehicle);
    }
}
