package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public Zombie methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.monster.Zombie.class)
public interface ZombieApiInvoker {

    @Invoker("supportsBreakDoorGoal")
    boolean fusioncore$supportsBreakDoorGoal();

    @Invoker("isSunSensitive")
    boolean fusioncore$isSunSensitive();
}
