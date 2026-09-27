package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.craftbukkit.v.inventory.CraftMetaBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code BlockStateMeta#clearBlockState} (patch 0377). */
@Mixin(value = CraftMetaBlockState.class, remap = false)
public abstract class CraftMetaBlockStateMixin {

    @Shadow private org.bukkit.craftbukkit.v.block.CraftBlockEntityState<?> blockEntityTag;

    public void clearBlockState() {
        this.blockEntityTag = null;
    }
}
