package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.generator.CraftLimitedRegion;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@link LimitedRegion} additions (patches 0286, 0555). */
@Mixin(value = LimitedRegion.class, remap = false)
public interface LimitedRegionMixin {

    default void setBlockData(Vector vector, BlockData data) {
        ((LimitedRegion) this).setBlockData(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ(), data);
    }

    default void setBlockState(int x, int y, int z, BlockState state) {
        WorldGenLevel handle = ((CraftLimitedRegion) (Object) this).getHandle();
        BlockPos pos = new BlockPos(x, y, z);
        BlockData current = CraftBlockData.fromData(handle.getBlockState(pos));
        if (!state.getBlockData().matches(current)) {
            throw new IllegalArgumentException("BlockData does not match! Expected " + state.getBlockData().getAsString(false) + ", got " + current.getAsString(false));
        }
        handle.getBlockEntity(pos).loadWithComponents(((CraftBlockEntityState<?>) state).getSnapshotNBT(), handle.registryAccess());
    }

    default void setBlockState(Vector location, BlockState state) {
        this.setBlockState(location.getBlockX(), location.getBlockY(), location.getBlockZ(), state);
    }

    default BlockState getBlockState(Vector location) {
        return ((LimitedRegion) this).getBlockState(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    default void scheduleBlockUpdate(int x, int y, int z) {
        WorldGenLevel handle = ((CraftLimitedRegion) (Object) this).getHandle();
        BlockPos pos = new BlockPos(x, y, z);
        handle.scheduleTick(pos, handle.getBlockState(pos).getBlock(), 0);
    }

    default void scheduleBlockUpdate(Vector location) {
        this.scheduleBlockUpdate(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    default void scheduleFluidUpdate(int x, int y, int z) {
        WorldGenLevel handle = ((CraftLimitedRegion) (Object) this).getHandle();
        BlockPos pos = new BlockPos(x, y, z);
        handle.scheduleTick(pos, handle.getFluidState(pos).getType(), 0);
    }

    default void scheduleFluidUpdate(Vector location) {
        this.scheduleFluidUpdate(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    default World getWorld() {
        return ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) ((CraftLimitedRegion) (Object) this).getHandle().getLevel()).bridge$getWorld();
    }

    default BlockData getBlockData(Vector vector) {
        return ((LimitedRegion) this).getBlockData(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    default int getCenterChunkX() {
        return ((CraftLimitedRegionAccessor) this).fusioncore$centerChunkX();
    }

    default int getCenterBlockX() {
        return this.getCenterChunkX() << 4;
    }

    default int getCenterChunkZ() {
        return ((CraftLimitedRegionAccessor) this).fusioncore$centerChunkZ();
    }

    default int getCenterBlockZ() {
        return this.getCenterChunkZ() << 4;
    }
}
