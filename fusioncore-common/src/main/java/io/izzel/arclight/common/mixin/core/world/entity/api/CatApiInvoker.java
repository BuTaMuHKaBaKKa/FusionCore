package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public Cat methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.animal.Cat.class)
public interface CatApiInvoker {

    @Invoker("setRelaxStateOne")
    void fusioncore$setRelaxStateOne(boolean relax);

    @Invoker("isRelaxStateOne")
    boolean fusioncore$isRelaxStateOne();
}
