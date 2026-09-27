package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.craftbukkit.v.inventory.CraftMetaShield;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code BlockStateMeta#clearBlockState} on shields (patch 0377): drops the banner. */
@Mixin(value = CraftMetaShield.class, remap = false)
public abstract class CraftMetaShieldMixin {

    @Shadow private org.bukkit.block.Banner banner;

    public void clearBlockState() {
        this.banner = null;
    }
}
