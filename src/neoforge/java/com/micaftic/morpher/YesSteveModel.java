package com.micaftic.morpher;

import com.micaftic.morpher.config.*;
import com.micaftic.morpher.core.storage.ModelStoragePaths;
import com.micaftic.morpher.event.CommonEvent;
import com.micaftic.morpher.event.YsmEventBootstrap;
import com.micaftic.morpher.util.obfuscate.Keep;
import com.google.gson.*;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.*;
import com.micaftic.morpher.core.api.PlatformAPI;
import com.micaftic.morpher.core.api.config.ConfigRegistration;
import java.io.IOException;

public class YesSteveModel {
    public static final String MOD_ID = "foxmodelloader";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private YesSteveModel() {}
    public static void init() {
        ModelStoragePaths.init(net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve(MOD_ID));
        LOGGER.info("Initializing Fox Model Loader, platform: " + PlatformAPI.getPlatformName());
        try { RuntimeAccelerationLoader.init(); } catch (IOException e) { LOGGER.error("Failed to initialize native lib", e); }
        if (!RuntimeAccelerationLoader.isAvailable()) LOGGER.error(getErrorMessage());
        CommonEvent.init();
        YsmEventBootstrap.register();
    }

    private static void initConfig() {
        java.io.File old = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("foxmodelloader-common.toml").toFile();
        if (old.isFile()) { java.io.File f2 = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get().resolve("foxmodelloader-client.toml").toFile(); if (!f2.isFile()) old.renameTo(f2); else old.delete(); }
        ConfigRegistration.register(MOD_ID, ModConfig.Type.CLIENT, GeneralConfig.buildSpec());
        ConfigRegistration.register(MOD_ID, ModConfig.Type.SERVER, ServerConfig.buildSpec());
    }
    public static void registerModBusEvents(net.neoforged.bus.api.IEventBus bus) {
        ModSoundEvents.REGISTER.register(bus);
        com.micaftic.morpher.neoforge.capability.NeoForgeCapabilities.register(bus);
    }
    @Keep public static boolean isAvailable() { return RuntimeAccelerationLoader.isAvailable(); }
    public static boolean isOnAndroid() { return RuntimeAccelerationLoader.isOnAndroid(); }
    public static void sendUnavailableMessage() {
        if (PlatformAPI.isServer()) {
            return;
        }
        try {
            Class.forName("com.micaftic.morpher.neoforge.YesSteveModelNeoForgeClient")
                    .getMethod("sendUnavailableMessage")
                    .invoke(null);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Failed to send unavailable message on client", e);
        }
    }
    public static Component getUnavailableComponent() { return RuntimeAccelerationLoader.getErrorComponent(); }
    public static String getErrorMessage() { return RuntimeAccelerationLoader.getErrorMessage(); }
}
