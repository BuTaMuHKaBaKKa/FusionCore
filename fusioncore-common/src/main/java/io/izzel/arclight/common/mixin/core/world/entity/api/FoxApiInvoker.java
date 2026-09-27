package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public Fox methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.animal.Fox.class)
public interface FoxApiInvoker {

    @Invoker("setFaceplanted")
    void fusioncore$setFaceplanted(boolean faceplanted);

    @Invoker("isDefending")
    boolean fusioncore$isDefending();

    @Invoker("setDefending")
    void fusioncore$setDefending(boolean defending);
}
