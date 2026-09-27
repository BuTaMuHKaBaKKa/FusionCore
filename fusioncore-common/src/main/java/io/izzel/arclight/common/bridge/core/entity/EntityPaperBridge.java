package io.izzel.arclight.common.bridge.core.entity;

import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.UUID;

/**
 * Fusion Core: Paper's per-entity state (origin, spawn reason, spawner flag, freeze lock, fixed pose), saved under Paper's NBT keys.
 */
public interface EntityPaperBridge {

    org.bukkit.util.Vector fusioncore$originVector();

    UUID fusioncore$originWorld();

    void fusioncore$setOrigin(org.bukkit.Location location);

    CreatureSpawnEvent.SpawnReason fusioncore$spawnReason();

    void fusioncore$setSpawnReason(CreatureSpawnEvent.SpawnReason reason);

    boolean fusioncore$fromMobSpawner();

    void fusioncore$setFromMobSpawner(boolean fromMobSpawner);

    boolean fusioncore$freezeLocked();

    void fusioncore$setFreezeLocked(boolean locked);

    boolean fusioncore$fixedPose();

    void fusioncore$setFixedPose(boolean fixed);

    /** {@code Entity#setLevel} (protected), for Paper's {@code Entity#spawnAt}. */
    void fusioncore$setLevel(net.minecraft.world.level.Level level);
}
