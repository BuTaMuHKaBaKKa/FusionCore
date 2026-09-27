package io.izzel.arclight.common.mixin.core.world.entity.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: private BaseSpawner methods Paper's Spawner API calls. */
@Mixin(BaseSpawner.class)
public interface BaseSpawnerApiInvoker {

    @Invoker("isNearPlayer")
    boolean fusioncore$isNearPlayer(Level level, BlockPos pos);

    @Invoker("delay")
    void fusioncore$delay(Level level, BlockPos pos);
}
