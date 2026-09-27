package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public AbstractHorse methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.animal.horse.AbstractHorse.class)
public interface AbstractHorseApiInvoker {

    @Invoker("getFlag")
    boolean fusioncore$getFlag(int flag);

    @Invoker("setFlag")
    void fusioncore$setFlag(int flag, boolean set);
}
