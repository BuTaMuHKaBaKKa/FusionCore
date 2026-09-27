package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@link EquipmentSlot} convenience methods (API patch 0365). */
@Mixin(value = EquipmentSlot.class, remap = false)
public abstract class EquipmentSlotMixin {

    public boolean isHand() {
        Object self = this;
        return self == EquipmentSlot.HAND || self == EquipmentSlot.OFF_HAND;
    }

    public EquipmentSlot getOppositeHand() {
        Object self = this;
        if (self == EquipmentSlot.HAND) {
            return EquipmentSlot.OFF_HAND;
        } else if (self == EquipmentSlot.OFF_HAND) {
            return EquipmentSlot.HAND;
        }
        throw new IllegalArgumentException("Unable to determine an opposite hand for equipment slot: " + ((EquipmentSlot) self).name());
    }

    public boolean isArmor() {
        Object self = this;
        return self == EquipmentSlot.HEAD || self == EquipmentSlot.CHEST || self == EquipmentSlot.LEGS || self == EquipmentSlot.FEET
            || self == EquipmentSlot.BODY;
    }
}
