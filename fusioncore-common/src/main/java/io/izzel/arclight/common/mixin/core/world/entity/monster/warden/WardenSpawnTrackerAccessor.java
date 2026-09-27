package io.izzel.arclight.common.mixin.core.world.entity.monster.warden;

import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: Paper's Player warden warning API (Paper patch 0763), which Paper gets by making these public. */
@Mixin(WardenSpawnTracker.class)
public interface WardenSpawnTrackerAccessor {

    @Accessor("cooldownTicks")
    int fusioncore$getCooldownTicks();

    @Accessor("cooldownTicks")
    void fusioncore$setCooldownTicks(int ticks);

    @Accessor("ticksSinceLastWarning")
    int fusioncore$getTicksSinceLastWarning();

    @Accessor("ticksSinceLastWarning")
    void fusioncore$setTicksSinceLastWarning(int ticks);

    @Invoker("increaseWarningLevel")
    void fusioncore$increaseWarningLevel();
}
