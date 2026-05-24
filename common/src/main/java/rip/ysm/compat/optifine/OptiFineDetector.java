package rip.ysm.compat.optifine;


public final class OptiFineDetector {

    private OptiFineDetector() {
    }

    public static boolean isOptifinePresent() {
        return rip.ysm.compat.optifine.fabric.OptiFineDetectorImpl.isOptifinePresent();
    }
}
