package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.LootableDataBridge;
import io.izzel.arclight.common.mod.util.FusionCoreLootableInventory;
import io.izzel.arclight.common.mod.util.FusionCoreLootables;
import org.bukkit.craftbukkit.v.block.CraftLootable;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code LootableBlockInventory} on loot container states (patch 0095): the replenish data of the state's
 * snapshot, written back to the block by {@code update()}, as in Paper.
 */
@Mixin(value = CraftLootable.class, remap = false)
public abstract class CraftLootableMixin implements FusionCoreLootableInventory {

    @Override
    public FusionCoreLootables.Data fusioncore$lootData() {
        return ((LootableDataBridge) ((CraftBlockEntityStateAccessor) this).fusioncore$getSnapshot()).fusioncore$lootableData();
    }
}
