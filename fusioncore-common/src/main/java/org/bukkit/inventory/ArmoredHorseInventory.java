// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package org.bukkit.inventory;

import org.jetbrains.annotations.Nullable;

public interface ArmoredHorseInventory extends AbstractHorseInventory {

    /**
     * Gets the item in the horse's armor slot.
     *
     * @return the armor item
     */
    @Nullable ItemStack getArmor();

    /**
     * Sets the item in the horse's armor slot.
     *
     * @param stack the new item
     */
    void setArmor(@Nullable ItemStack stack);
}
