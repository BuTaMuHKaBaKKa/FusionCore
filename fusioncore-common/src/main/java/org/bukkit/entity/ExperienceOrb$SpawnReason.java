// Fusion Core: Paper API 1.21.1 (org.bukkit.entity.ExperienceOrb.SpawnReason from paper-api sources), compiled as a top-level class under its binary name.
package org.bukkit.entity;

/**
 * Reasons for why this Experience Orb was spawned
 */
public enum ExperienceOrb$SpawnReason {
    /**
     * Spawned by a player dying
     */
    PLAYER_DEATH,
    /**
     * Spawned by an entity dying after being damaged by a player
     */
    ENTITY_DEATH,
    /**
     * Spawned by player using a furnace
     */
    FURNACE,
    /**
     * Spawned by player breeding animals
     */
    BREED,
    /**
     * Spawned by player trading with a villager
     */
    VILLAGER_TRADE,
    /**
     * Spawned by player fishing
     */
    FISHING,
    /**
     * Spawned by player breaking a block that gives experience points such as Diamond Ore
     */
    BLOCK_BREAK,
    /**
     * Spawned by Bukkit API
     */
    CUSTOM,
    /**
     * Spawned by a player throwing an experience points bottle
     */
    EXP_BOTTLE,
    /**
     * Spawned by a player using a grindstone
     */
    GRINDSTONE,
    /**
     * We do not know why it was spawned
     */
    UNKNOWN
}
