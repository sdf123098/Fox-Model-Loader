package com.micaftic.morpher.core.compat.oculus.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

/** Optional Iris bridge. Iris owns its API classes; Fox must never package stubs. */
public final class OculusCompatImpl {
    private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

    private OculusCompatImpl() {
    }

    public static boolean isLoaded() {
        return IRIS_LOADED;
    }

    public static boolean isPBRActive() {
        return isRenderingShadowPass();
    }

    public static void updatePBRState() {
    }

    public static boolean isShaderPackInUse() {
        return IRIS_LOADED && IrisHolder.API.invokeBoolean(IrisHolder.API.shaderPackMethod);
    }

    public static boolean isRenderingShadowPass() {
        return IRIS_LOADED && IrisHolder.API.invokeBoolean(IrisHolder.API.shadowPassMethod);
    }

    private static final class IrisHolder {
        private static final IrisApiBridge API = new IrisApiBridge();
    }

    private static final class IrisApiBridge {
        private final Object instance;
        private final Method shaderPackMethod;
        private final Method shadowPassMethod;

        private IrisApiBridge() {
            Object api = null;
            Method shaderPack = null;
            Method shadowPass = null;
            try {
                Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                api = apiClass.getMethod("getInstance").invoke(null);
                shaderPack = apiClass.getMethod("isShaderPackInUse");
                shadowPass = apiClass.getMethod("isRenderingShadowPass");
            } catch (Throwable ignored) {
                // Iris is optional; an absent or incompatible API disables the bridge.
            }
            instance = api;
            shaderPackMethod = shaderPack;
            shadowPassMethod = shadowPass;
        }

        private boolean invokeBoolean(Method method) {
            if (instance == null || method == null) {
                return false;
            }
            try {
                return Boolean.TRUE.equals(method.invoke(instance));
            } catch (Throwable ignored) {
                return false;
            }
        }
    }
}
