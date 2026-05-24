package rip.ysm.api.config;

import net.neoforged.fml.config.ModConfig;

public final class ConfigRegistration {

    private ConfigRegistration() {
    }

    public static void register(String modId, ModConfig.Type type, Object spec) {
        rip.ysm.api.config.fabric.ConfigRegistrationImpl.register(modId, type, spec);
    }
}
