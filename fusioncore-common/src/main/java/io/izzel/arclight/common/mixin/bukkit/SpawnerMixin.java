package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.inventory.ItemStack;
import org.bukkit.spawner.Spawner;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Spawner#setSpawnedItem} (patch 0286, {@code PaperSharedSpawnerLogic}): the spawner spawns an item entity. */
@Mixin(value = Spawner.class, remap = false)
public interface SpawnerMixin {

    default void setSpawnedItem(ItemStack itemStack) {
        FusionCoreBlockStates.setSpawnedItem(this, itemStack);
    }

    default boolean isActivated() {
        return FusionCoreBlockStates.spawnerActivated(this);
    }

    default void resetTimer() {
        FusionCoreBlockStates.resetSpawnerTimer(this);
    }
}
