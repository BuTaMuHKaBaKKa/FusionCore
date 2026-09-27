package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.block.EntityBlockStorage;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code EntityBlockStorage#clearEntities} (patch 0524), on the snapshot. */
@Mixin(value = EntityBlockStorage.class, remap = false)
public interface EntityBlockStorageMixin {

    default void clearEntities() {
        FusionCoreBlockStates.clearEntities(this);
    }
}
