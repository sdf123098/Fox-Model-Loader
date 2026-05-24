package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction;
import net.minecraft.world.entity.projectile.arrow.Arrow;

public abstract class ArrowEntityFunction extends ContextFunction<net.minecraft.world.entity.projectile.arrow.Arrow> {
    @Override
    public boolean validateContext(IContext<?> context) {
        return context.entity() instanceof net.minecraft.world.entity.projectile.arrow.Arrow;
    }
}