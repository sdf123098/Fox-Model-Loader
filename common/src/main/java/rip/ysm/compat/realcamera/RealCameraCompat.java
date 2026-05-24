package rip.ysm.compat.realcamera;


public final class RealCameraCompat {

    private RealCameraCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.realcamera.fabric.RealCameraCompatImpl.isLoaded();
    }

    public static boolean isActive() {
        return rip.ysm.compat.realcamera.fabric.RealCameraCompatImpl.isActive();
    }
}
