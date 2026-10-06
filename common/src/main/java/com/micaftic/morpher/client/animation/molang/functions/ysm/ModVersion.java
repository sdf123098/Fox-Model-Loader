package com.micaftic.morpher.client.animation.molang.functions.ysm;

import com.micaftic.morpher.YesSteveModel;
import com.micaftic.morpher.molang.runtime.ExecutionContext;
import com.micaftic.morpher.molang.runtime.Function;
import com.micaftic.morpher.core.architectury.platform.Platform;
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
        // Preserve the model-script API from the 1.2.9 lineage without claiming to be YSM.
        if (!Platform.isModLoaded(modid) && "sparkle_morpher".equals(modid)) {
            modid = YesSteveModel.MOD_ID;
        }
        if (!Platform.isModLoaded(modid)) {
            return null;
        }
        return Platform.getMod(modid).getVersion();
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}