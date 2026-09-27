package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.craftbukkit.v.inventory.CraftMetaLeatherArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code LeatherArmorMeta#isDyed} for leather armour (patch 0961 "Expose hasColor to leather armor"). */
@Mixin(value = CraftMetaLeatherArmor.class, remap = false)
public abstract class CraftMetaLeatherArmorMixin {

    @Shadow abstract boolean hasColor();

    public boolean isDyed() {
        return this.hasColor();
    }
}
