package rip.ysm.api.capability;

import net.minecraft.world.entity.Entity;

public final class CapabilityLifecycle {

    private CapabilityLifecycle() {
    }

    public static void revive(Entity entity) {
        rip.ysm.api.capability.fabric.CapabilityLifecycleImpl.revive(entity);
    }

    public static void invalidate(Entity entity) {
        rip.ysm.api.capability.fabric.CapabilityLifecycleImpl.invalidate(entity);
    }
}
