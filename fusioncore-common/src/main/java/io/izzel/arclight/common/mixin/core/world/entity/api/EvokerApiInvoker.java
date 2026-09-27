package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public Evoker methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.monster.Evoker.class)
public interface EvokerApiInvoker {

    @Invoker("getWololoTarget")
    net.minecraft.world.entity.animal.Sheep fusioncore$getWololoTarget();

    @Invoker("setWololoTarget")
    void fusioncore$setWololoTarget(net.minecraft.world.entity.animal.Sheep sheep);
}
