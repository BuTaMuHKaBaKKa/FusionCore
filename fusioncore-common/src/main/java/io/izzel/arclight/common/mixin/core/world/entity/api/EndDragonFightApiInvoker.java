package io.izzel.arclight.common.mixin.core.world.entity.api;

import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: the end gateway spawners behind Paper's {@code DragonBattle#spawnNewGateway} (patch 0867). */
@Mixin(EndDragonFight.class)
public interface EndDragonFightApiInvoker {

    @Invoker("spawnNewGateway")
    void fusioncore$spawnNewGateway();
}
