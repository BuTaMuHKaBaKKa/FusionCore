// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package com.destroystokyo.paper.loottable;

import org.bukkit.block.Block;

/**
 * Represents an Inventory that can generate loot, such as Chests inside of Fortresses and Mineshafts
 */
public interface LootableBlockInventory extends LootableInventory {

    /**
     * Gets the block that is lootable
     * @return The Block
     */
    Block getBlock();
}
