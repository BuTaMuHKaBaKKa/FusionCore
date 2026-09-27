package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.generator.CraftLimitedRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CraftLimitedRegion.class, remap = false)
public interface CraftLimitedRegionAccessor {

    @Accessor("centerChunkX")
    int fusioncore$centerChunkX();

    @Accessor("centerChunkZ")
    int fusioncore$centerChunkZ();
}
