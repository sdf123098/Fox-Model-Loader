package rip.ysm.compat.oculus;

public final class OculusCompat {

    private OculusCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.oculus.fabric.OculusCompatImpl.isLoaded();
    }

    public static boolean isPBRActive() {
        return rip.ysm.compat.oculus.fabric.OculusCompatImpl.isPBRActive();
    }

    public static void updatePBRState() {
        rip.ysm.compat.oculus.fabric.OculusCompatImpl.updatePBRState();
    }

    public static boolean isShaderPackInUse() {
        return rip.ysm.compat.oculus.fabric.OculusCompatImpl.isShaderPackInUse();
    }

    public static boolean isRenderingShadowPass() {
        return rip.ysm.compat.oculus.fabric.OculusCompatImpl.isRenderingShadowPass();
    }
}
