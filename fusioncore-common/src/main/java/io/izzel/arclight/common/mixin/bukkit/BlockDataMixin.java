package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code BlockData} additions: destroy speed (Paper patch 0432), random ticking (patch 0730), collision
 * shape at a location (patch 0897).
 */
@Mixin(value = org.bukkit.block.data.BlockData.class, remap = false)
public interface BlockDataMixin {

    default float getDestroySpeed(org.bukkit.inventory.ItemStack itemStack) {
        return this.getDestroySpeed(itemStack, false);
    }

    default float getDestroySpeed(org.bukkit.inventory.ItemStack itemStack, boolean considerEnchants) {
        return io.izzel.arclight.common.mod.util.FusionCoreBlocks.destroySpeed(((org.bukkit.craftbukkit.v.block.data.CraftBlockData) (Object) this).getState(), itemStack, considerEnchants);
    }

    default boolean isRandomlyTicked() {
        return ((org.bukkit.craftbukkit.v.block.data.CraftBlockData) (Object) this).getState().isRandomlyTicking();
    }

    default org.bukkit.util.VoxelShape getCollisionShape(org.bukkit.Location location) {
        com.google.common.base.Preconditions.checkArgument(location != null, "location must not be null");
        org.bukkit.craftbukkit.v.CraftWorld world = (org.bukkit.craftbukkit.v.CraftWorld) location.getWorld();
        com.google.common.base.Preconditions.checkArgument(world != null, "location must not have a null world");
        net.minecraft.core.BlockPos position = new net.minecraft.core.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return new org.bukkit.craftbukkit.v.util.CraftVoxelShape(((org.bukkit.craftbukkit.v.block.data.CraftBlockData) (Object) this).getState().getCollisionShape(world.getHandle(), position));
    }
}
