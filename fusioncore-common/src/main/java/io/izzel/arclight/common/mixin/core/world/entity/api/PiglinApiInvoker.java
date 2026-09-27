package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public Piglin methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.monster.piglin.Piglin.class)
public interface PiglinApiInvoker {

    @Invoker("isChargingCrossbow")
    boolean fusioncore$isChargingCrossbow();
}
