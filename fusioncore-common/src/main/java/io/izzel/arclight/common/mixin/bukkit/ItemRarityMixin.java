package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.inventory.ItemRarity} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.inventory.ItemRarity.class, remap = false)
public abstract class ItemRarityMixin {

    /** Paper: the rarity's name colour, as vanilla's Rarity. */
    public net.kyori.adventure.text.format.TextColor color() {
        return switch (((org.bukkit.inventory.ItemRarity) (Object) this).name()) {
            case "UNCOMMON" -> net.kyori.adventure.text.format.NamedTextColor.YELLOW;
            case "RARE" -> net.kyori.adventure.text.format.NamedTextColor.AQUA;
            case "EPIC" -> net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
            default -> net.kyori.adventure.text.format.NamedTextColor.WHITE;
        };
    }
}
