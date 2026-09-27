package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.block.Container;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: {@link Container} as a lockable tile state and inventory holder (API patches 0380, 0488). */
@Mixin(value = Container.class, remap = false)
public interface ContainerMixin extends io.papermc.paper.block.LockableTileState, io.papermc.paper.block.TileStateInventoryHolder {

    default boolean isSnapshot() {
        return true;
    }
}
