package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's falling block state (patch 0700) and the deprecated {@code getSourceLoc}.
 */
@Mixin(value = org.bukkit.entity.FallingBlock.class, remap = false)
public interface FallingBlockMixin {

    default boolean doesAutoExpire() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.api.FallingBlockApiBridge) FusionCoreEntities.handle(this)).fusioncore$autoExpire();
    }

    default void shouldAutoExpire(boolean autoExpires) {
        ((io.izzel.arclight.common.bridge.core.world.entity.api.FallingBlockApiBridge) FusionCoreEntities.handle(this)).fusioncore$setAutoExpire(autoExpires);
    }

    @Deprecated
    default org.bukkit.Location getSourceLoc() {
        return FusionCoreEntities.origin(this);
    }

    default void setBlockData(org.bukkit.block.data.BlockData blockData) {
        com.google.common.base.Preconditions.checkArgument(blockData != null, "blockData");
        net.minecraft.world.entity.item.FallingBlockEntity h = FusionCoreEntities.nms(this);
        net.minecraft.world.level.block.state.BlockState oldState = h.blockState, newState = ((org.bukkit.craftbukkit.v.block.data.CraftBlockData) blockData).getState();
        h.blockState = newState;
        h.blockData = null;
        if (oldState != newState) {
            ((CraftEntityAccessor) (Object) this).fusioncore$update();
        }
    }

    default org.bukkit.block.BlockState getBlockState() {
        net.minecraft.world.entity.item.FallingBlockEntity h = FusionCoreEntities.nms(this);
        return org.bukkit.craftbukkit.v.block.CraftBlockStates.getBlockState(h.blockState, h.blockData);
    }

    default void setBlockState(org.bukkit.block.BlockState blockState) {
        com.google.common.base.Preconditions.checkArgument(blockState != null, "blockState");
        // updates the client if needed and clears the old block entity data; that data is not sent to clients
        this.setBlockData(blockState.getBlockData());
        if (blockState instanceof org.bukkit.craftbukkit.v.block.CraftBlockEntityState<?> tileEntity) {
            net.minecraft.world.entity.item.FallingBlockEntity h = FusionCoreEntities.nms(this);
            h.blockData = tileEntity.getSnapshotNBT();
        }
    }
}
