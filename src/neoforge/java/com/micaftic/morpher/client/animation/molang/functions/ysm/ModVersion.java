package com.micaftic.morpher.client.animation.molang.functions.ysm;

import com.micaftic.morpher.YesSteveModel;
import com.micaftic.morpher.molang.runtime.ExecutionContext;
import com.micaftic.morpher.molang.runtime.Function;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModVersion implements Function {
    @Override
    @Nullable
    public Object evaluate(@NotNull ExecutionContext<?> context, @NotNull Function.ArgumentCollection arguments) {
        String modid = arguments.getAsString(context, 0);
        if (modid == null) {
            return null;
        }
        String version = installedVersion(modid);
        if (version == null && "sparkle_morpher".equals(modid)) {
            return installedVersion(YesSteveModel.MOD_ID);
        }
        return version;
    }

    private static String installedVersion(String modid) {
        return ModList.get().getModContainerById(modid)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse(null);
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}