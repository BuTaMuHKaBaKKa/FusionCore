package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.craftbukkit.v.inventory.CraftMetaCompass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's lodestone methods (patch 0960) on top of Spigot's target and tracked flag. */
@Mixin(value = CraftMetaCompass.class, remap = false)
public abstract class CraftMetaCompassMixin {

    // @formatter:off
    @Shadow private net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> lodestoneWorld;
    @Shadow private boolean tracked;
    // @formatter:on

    public boolean isLodestoneCompass() {
        return this.lodestoneWorld != null || !this.tracked;
    }

    public void clearLodestone() {
        this.lodestoneWorld = null;
        this.tracked = true;
    }
}
