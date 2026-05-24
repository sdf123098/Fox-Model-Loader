package rip.ysm.compat.simpleplanes;

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import org.joml.Vector3f;

import java.util.Optional;

public final class SimplePlanesCompat {

    private SimplePlanesCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.simpleplanes.fabric.SimplePlanesCompatImpl.isLoaded();
    }

    public static Optional<Vector3f> getSimplePlanesRotation(AnimationEvent<GeckoVehicleEntity> event) {
        return rip.ysm.compat.simpleplanes.fabric.SimplePlanesCompatImpl.getSimplePlanesRotation(event);
    }
}
