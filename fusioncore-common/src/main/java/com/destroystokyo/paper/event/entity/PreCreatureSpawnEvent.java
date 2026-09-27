package com.destroystokyo.paper.event.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fusion Core: only {@link PhantomPreSpawnEvent} is fired; natural and spawner spawns go through {@link CreatureSpawnEvent}.
 */
public class PreCreatureSpawnEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Location location;
    private final EntityType type;
    private final CreatureSpawnEvent.SpawnReason reason;
    private boolean shouldAbortSpawn;

    private boolean cancelled;

    public PreCreatureSpawnEvent(final @NotNull Location location, final @NotNull EntityType type, final @NotNull CreatureSpawnEvent.SpawnReason reason) {
        this.location = location;
        this.type = type;
        this.reason = reason;
    }

    /**
     * @return The location this creature is being spawned at
     */
    public @NotNull Location getSpawnLocation() {
        return this.location.clone();
    }

    /**
     * @return The type of creature being spawned
     */
    public @NotNull EntityType getType() {
        return this.type;
    }

    /**
     * @return Reason this creature is spawning (ie, NATURAL vs SPAWNER)
     */
    public @NotNull CreatureSpawnEvent.SpawnReason getReason() {
        return this.reason;
    }

    /**
     * @return If the spawn process should be aborted vs trying more attempts
     */
    public boolean shouldAbortSpawn() {
        return this.shouldAbortSpawn;
    }

    /**
     * Set this if you are more blanket blocking all types of these spawns, and wish to abort the spawn process from
     * trying more attempts after this cancellation.
     *
     * @param shouldAbortSpawn Set if the spawn process should be aborted vs trying more attempts
     */
    public void setShouldAbortSpawn(final boolean shouldAbortSpawn) {
        this.shouldAbortSpawn = shouldAbortSpawn;
    }

    /**
     * @return If the spawn of this creature is cancelled or not
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Cancelling this event is more efficient than cancelling {@link CreatureSpawnEvent}
     *
     * @param cancel {@code true} if you wish to cancel this event, and abort the spawn of this creature
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
