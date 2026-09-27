package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreInventories;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's drop chance per slot on {@link EntityEquipment} (patch 0512), through vanilla's per-slot getter/setter (body armor too). */
@Mixin(value = EntityEquipment.class, remap = false)
public interface EntityEquipmentMixin {

    default float getDropChance(EquipmentSlot slot) {
        return FusionCoreInventories.dropChance((EntityEquipment) this, slot);
    }

    default void setDropChance(EquipmentSlot slot, float chance) {
        FusionCoreInventories.setDropChance((EntityEquipment) this, slot, chance);
    }
}
