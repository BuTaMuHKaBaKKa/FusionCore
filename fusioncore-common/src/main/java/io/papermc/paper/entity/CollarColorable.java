// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.entity;

import org.bukkit.DyeColor;
import org.bukkit.entity.LivingEntity;

/**
 * Entities that can have their collars colored.
 */
public interface CollarColorable extends LivingEntity {

    /**
     * Get the collar color of this entity
     *
     * @return the color of the collar
     */
    DyeColor getCollarColor();

    /**
     * Set the collar color of this entity
     *
     * @param color the color to apply
     */
    void setCollarColor(DyeColor color);
}
