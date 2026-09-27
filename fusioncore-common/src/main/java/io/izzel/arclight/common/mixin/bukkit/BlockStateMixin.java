package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code BlockState#getDrops} (Paper patch 0744) and {@code isCollidable} (patch 0590). */
@Mixin(value = org.bukkit.block.BlockState.class, remap = false)
public interface BlockStateMixin {

    default java.util.Collection<org.bukkit.inventory.ItemStack> getDrops() {
        return this.getDrops(null);
    }

    default java.util.Collection<org.bukkit.inventory.ItemStack> getDrops(org.bukkit.inventory.ItemStack tool) {
        return this.getDrops(tool, null);
    }

    /** Drops of the state at its position with this tool and breaker, without the tool check when the tool is null. */
    default java.util.Collection<org.bukkit.inventory.ItemStack> getDrops(org.bukkit.inventory.ItemStack tool, org.bukkit.entity.Entity entity) {
        return io.izzel.arclight.common.mod.util.FusionCoreBlocks.stateDrops((org.bukkit.craftbukkit.v.block.CraftBlockState) (Object) this, tool, entity);
    }

    default boolean isCollidable() {
        return ((io.izzel.arclight.common.mixin.core.world.level.block.state.BlockBehaviourPaperAccessor)
            ((org.bukkit.craftbukkit.v.block.CraftBlockState) (Object) this).getHandle().getBlock()).fusioncore$hasCollision();
    }
}
