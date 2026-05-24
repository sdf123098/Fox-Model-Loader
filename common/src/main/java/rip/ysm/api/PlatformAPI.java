package rip.ysm.api;

public final class PlatformAPI {
    private PlatformAPI() {
    }

    public static boolean isServer() {
        return rip.ysm.api.fabric.PlatformAPIImpl.isServer();
    }

    public static String getPlatformName() {
        return rip.ysm.api.fabric.PlatformAPIImpl.getPlatformName();
    }
}
