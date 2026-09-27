package io.izzel.arclight.common.mixin.bukkit;

import com.destroystokyo.paper.loottable.LootableEntityInventory;
import org.bukkit.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@link LootableEntityInventory} on {@link org.bukkit.entity.ChestBoat} (patch 0039); the bodies are on the Craft class. */
@Mixin(value = org.bukkit.entity.ChestBoat.class, remap = false)
public interface ChestBoatMixin extends LootableEntityInventory {

    @Override
    default Entity getEntity() {
        return (Entity) (Object) this;
    }
}
