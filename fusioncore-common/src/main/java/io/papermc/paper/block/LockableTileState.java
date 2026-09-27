// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.block;

import org.bukkit.Nameable;
import org.bukkit.block.Lockable;
import org.bukkit.block.TileState;

/**
 * Interface for tile entities that are lockable.
 */
public interface LockableTileState extends TileState, Lockable, Nameable {
}
