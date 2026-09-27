package org.bukkit.event.inventory;

/** Fusion Core: Paper's {@code InventoryCloseEvent.Reason}, compiled as a top-level class under its binary name. */
@SuppressWarnings("checkstyle:TypeName")
public enum InventoryCloseEvent$Reason {
    /**
     * Unknown reason
     */
    UNKNOWN,
    /**
     * Player is teleporting
     */
    TELEPORT,
    /**
     * Player is no longer permitted to use this inventory
     */
    CANT_USE,
    /**
     * The chunk the inventory was in was unloaded
     */
    UNLOADED,
    /**
     * Opening new inventory instead
     */
    OPEN_NEW,
    /**
     * Closed
     */
    PLAYER,
    /**
     * Closed due to disconnect
     */
    DISCONNECT,
    /**
     * The player died
     */
    DEATH,
    /**
     * Closed by Bukkit API
     */
    PLUGIN,
}
