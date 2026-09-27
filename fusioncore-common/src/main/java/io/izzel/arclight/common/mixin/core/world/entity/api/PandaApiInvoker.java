package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public Panda methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.animal.Panda.class)
public interface PandaApiInvoker {

    @Invoker("getEatCounter")
    int fusioncore$getEatCounter();

    @Invoker("setEatCounter")
    void fusioncore$setEatCounter(int ticks);
}
