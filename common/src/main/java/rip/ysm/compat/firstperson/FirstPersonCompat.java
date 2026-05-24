package rip.ysm.compat.firstperson;


public final class FirstPersonCompat {

    private FirstPersonCompat() {
    }

    public static boolean isLoaded() {
        return rip.ysm.compat.firstperson.fabric.FirstPersonCompatImpl.isLoaded();
    }

    public static boolean isFirstPersonActive() {
        return rip.ysm.compat.firstperson.fabric.FirstPersonCompatImpl.isFirstPersonActive();
    }

    public static boolean shouldHideHead() {
        return rip.ysm.compat.firstperson.fabric.FirstPersonCompatImpl.shouldHideHead();
    }

    public static void setCameraDistance(float distance) {
        rip.ysm.compat.firstperson.fabric.FirstPersonCompatImpl.setCameraDistance(distance);
    }
}
