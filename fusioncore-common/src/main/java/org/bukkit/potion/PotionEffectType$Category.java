// Fusion Core: Paper API 1.21.1 (org.bukkit.potion.PotionEffectType.Category), compiled as a top-level class under its binary name.
package org.bukkit.potion;

import org.jetbrains.annotations.NotNull;

/**
 * Category of {@link PotionEffectType}s
 */
public enum PotionEffectType$Category {

    BENEFICIAL(net.kyori.adventure.text.format.NamedTextColor.BLUE),
    HARMFUL(net.kyori.adventure.text.format.NamedTextColor.RED),
    NEUTRAL(net.kyori.adventure.text.format.NamedTextColor.BLUE);

    private final net.kyori.adventure.text.format.TextColor color;

    PotionEffectType$Category(net.kyori.adventure.text.format.TextColor color) {
        this.color = color;
    }

    /**
     * Returns the text color used for the category's attribute modifiers in the tooltip.
     *
     * @return the text color
     */
    @NotNull
    public net.kyori.adventure.text.format.TextColor getColor() {
        return color;
    }
}
