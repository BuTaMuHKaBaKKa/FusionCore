package io.izzel.arclight.common.mixin.core.world.entity;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: mob internals Paper's API reaches: {@code Mob#isInDaylight} (patch 0253). */
@Mixin(Mob.class)
public interface MobPaperAccessor {

    @Invoker("isSunBurnTick")
    boolean fusioncore$isSunBurnTick();
}
