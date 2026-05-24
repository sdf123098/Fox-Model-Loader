package rip.ysm.compat.touhoulittlemaid;

import net.minecraft.world.entity.Entity;

import java.util.Optional;

public final class MaidCapabilityBridge {

    private MaidCapabilityBridge() {
    }

    public static Optional<Object> get(Entity entity) {
        return rip.ysm.compat.touhoulittlemaid.fabric.MaidCapabilityBridgeImpl.get(entity);
    }
}
