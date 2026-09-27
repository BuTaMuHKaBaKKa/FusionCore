package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's TNT block data (patch 0557) and the deprecated {@code getSourceLoc}.
 */
@Mixin(value = org.bukkit.entity.TNTPrimed.class, remap = false)
public interface TNTPrimedMixin {

    @Deprecated
    default org.bukkit.Location getSourceLoc() {
        return FusionCoreEntities.origin(this);
    }

    default void setBlockData(org.bukkit.block.data.BlockData data) {
        com.google.common.base.Preconditions.checkArgument(data != null, "The visual block data of this tnt cannot be null. To reset it just set to the TNT default block data");
        net.minecraft.world.entity.item.PrimedTnt h = FusionCoreEntities.nms(this);
        h.setBlockState(((org.bukkit.craftbukkit.v.block.data.CraftBlockData) data).getState());
    }

    default org.bukkit.block.data.BlockData getBlockData() {
        net.minecraft.world.entity.item.PrimedTnt h = FusionCoreEntities.nms(this);
        return org.bukkit.craftbukkit.v.block.data.CraftBlockData.fromData(h.getBlockState());
    }
}
