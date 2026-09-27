package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.craftbukkit.v.inventory.CraftMetaColorableArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code LeatherArmorMeta#isDyed} for wolf armour and other dyeable armour (patch 0961 "Expose hasColor to leather armor"). */
@Mixin(value = CraftMetaColorableArmor.class, remap = false)
public abstract class CraftMetaColorableArmorMixin {

    @Shadow abstract boolean hasColor();

    public boolean isDyed() {
        return this.hasColor();
    }
}
