package com.micaftic.morpher;

import com.micaftic.morpher.config.GeneralConfig;
import com.micaftic.morpher.config.ModSoundEvents;
import com.micaftic.morpher.config.ServerConfig;
import com.micaftic.morpher.event.YsmEventBootstrap;
import com.micaftic.morpher.util.obfuscate.Keep;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.micaftic.morpher.core.architectury.platform.Platform;
import com.micaftic.morpher.core.storage.ModelStoragePaths;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.micaftic.morpher.core.api.PlatformAPI;
import com.micaftic.morpher.core.api.config.ConfigRegistration;

import java.io.File;
import java.io.IOException;

/**
 * TODO:
 * 姒涙ǹ顓诲Ο鈥崇?锋惔鏃囶嚉鐏忓崬婀Ο锛勭矋閺嬭泛濮炴潪鐣屾畱閺冭泛鈧瑥姘ㄦ０鍕鏉炴垝绨?
 * 閸忚泛鐣犲Ο鈥崇?风紒鐔虹埠闁姤妲告潻娑樺弳娑撴牜鏅崥搴″鏉? */
public class YesSteveModel {

    public static final String MOD_ID = "foxmodelloader";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private YesSteveModel() {
    }

    public static void init() {
        ModelStoragePaths.init(Platform.getConfigFolder().resolve(MOD_ID));
        LOGGER.info("Initializing Fox Model Loader, platform: " + PlatformAPI.getPlatformName());
        try {
            RuntimeAccelerationLoader.init();
        } catch (IOException e) {
            LOGGER.error("Failed to initialize native lib", e);
        }
        if (!RuntimeAccelerationLoader.isAvailable()) {
            LOGGER.error(getErrorMessage());
        } else {
            initConfig();
        }
        YsmEventBootstrap.register();
    }



    @SuppressWarnings({"deprecation", "removal"})
    private static void initConfig() {
        File oldConfig = Platform.getConfigFolder().resolve("foxmodelloader-common.toml").toFile();
        if (oldConfig.isFile()) {
            File file2 = Platform.getConfigFolder().resolve("foxmodelloader-client.toml").toFile();
            if (!file2.isFile()) {
                oldConfig.renameTo(file2);
            } else {
                oldConfig.delete();
            }
        }
        ConfigRegistration.register(MOD_ID, ModConfig.Type.CLIENT, GeneralConfig.buildSpec());
        ConfigRegistration.register(MOD_ID, ModConfig.Type.SERVER, ServerConfig.buildSpec());
        if (!PlatformAPI.isServer()) {
            // MC 26.x: DeferredRegister.register now requires (String, Supplier) args
            // ModSoundEvents.REGISTER.register("", () -> SoundEvent.createVariableRangeEvent(com.micaftic.morpher.core.api.resource.ResourceApi.nativeId(MOD_ID, "")));
        }
    }

    @Keep
    public static boolean isAvailable() {
        return RuntimeAccelerationLoader.isAvailable();
    }

    public static boolean isOnAndroid() {
        return RuntimeAccelerationLoader.isOnAndroid();
    }
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

    public static Component getUnavailableComponent() {
        return RuntimeAccelerationLoader.getErrorComponent();
    }

    public static String getErrorMessage() {
        return RuntimeAccelerationLoader.getErrorMessage();
    }
}
