package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code translationKey()} of a creative tab (patch 0404), Paper's keys. */
@Mixin(value = org.bukkit.inventory.CreativeCategory.class, remap = false)
public abstract class CreativeCategoryMixin implements net.kyori.adventure.translation.Translatable {

    public String translationKey() {
        // a switch on the name: an enum switch would need a synthetic inner class, which a mixin cannot carry
        String constant = ((org.bukkit.inventory.CreativeCategory) (Object) this).name();
        return "itemGroup." + switch (constant) {
            case "BUILDING_BLOCKS" -> "buildingBlocks";
            default -> constant.toLowerCase(java.util.Locale.ENGLISH);
        };
    }
}
