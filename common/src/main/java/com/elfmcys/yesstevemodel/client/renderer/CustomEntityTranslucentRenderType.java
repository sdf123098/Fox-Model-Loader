package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.util.data.MemoizationCache;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public class CustomEntityTranslucentRenderType {

    private static final Function<Identifier, CustomEntityTranslucentRenderType> CACHE = MemoizationCache.memoize(CustomEntityTranslucentRenderType::new);

    private CustomEntityTranslucentRenderType(Identifier identifier) {
    }

    public static CustomEntityTranslucentRenderType get(Identifier identifier) {
        return CACHE.apply(identifier);
    }
}
