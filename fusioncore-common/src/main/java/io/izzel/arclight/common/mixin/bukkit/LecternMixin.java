package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.block.Lectern;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: {@link Lectern} as a {@link io.papermc.paper.block.TileStateInventoryHolder} (API patch 0488). */
@Mixin(value = Lectern.class, remap = false)
public interface LecternMixin extends io.papermc.paper.block.TileStateInventoryHolder {

    default boolean isSnapshot() {
        return true;
    }
}
