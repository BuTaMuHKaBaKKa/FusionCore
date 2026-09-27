package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public Turtle methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.animal.Turtle.class)
public interface TurtleApiInvoker {

    @Invoker("getHomePos")
    net.minecraft.core.BlockPos fusioncore$getHomePos();

    @Invoker("setHasEgg")
    void fusioncore$setHasEgg(boolean hasEgg);

    @Invoker("isGoingHome")
    boolean fusioncore$isGoingHome();
}
