package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.LootableDataBridge;
import io.izzel.arclight.common.mod.util.FusionCoreLootableInventory;
import io.izzel.arclight.common.mod.util.FusionCoreLootables;
import org.bukkit.craftbukkit.v.entity.CraftChestBoat;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code LootableEntityInventory} (patch 0095): the entity's replenish data. */
@Mixin(value = CraftChestBoat.class, remap = false)
public abstract class CraftChestBoatMixin implements FusionCoreLootableInventory {

    @Override
    public FusionCoreLootables.Data fusioncore$lootData() {
        return ((LootableDataBridge) ((CraftChestBoat) (Object) this).getHandle()).fusioncore$lootableData();
    }
}
