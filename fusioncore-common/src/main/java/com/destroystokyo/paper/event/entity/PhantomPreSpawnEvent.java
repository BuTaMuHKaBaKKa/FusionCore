package com.destroystokyo.paper.event.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

/** Called when a phantom is spawned for an exhausted player */
public class PhantomPreSpawnEvent extends PreCreatureSpawnEvent {

    private final Entity entity;

    public PhantomPreSpawnEvent(final @NotNull Location location, final @NotNull Entity entity, final @NotNull CreatureSpawnEvent.SpawnReason reason) {
        super(location, EntityType.PHANTOM, reason);
        this.entity = entity;
    }

    /**
     * Get the entity this phantom is spawning for
     *
     * @return the Entity
     */
    public @NotNull Entity getSpawningEntity() {
        return this.entity;
    }
}
