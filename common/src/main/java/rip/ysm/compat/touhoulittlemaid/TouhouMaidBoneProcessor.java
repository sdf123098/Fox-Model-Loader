package rip.ysm.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;

public final class TouhouMaidBoneProcessor {

    private TouhouMaidBoneProcessor() {
    }

    public static Object createLocationBone(AnimatedGeoBone bone) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidBoneProcessorImpl.createLocationBone(bone);
    }

    public static Object createLocationModel(AnimatedGeoModel model) {
        return rip.ysm.compat.touhoulittlemaid.fabric.TouhouMaidBoneProcessorImpl.createLocationModel(model);
    }
}
