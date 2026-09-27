package io.izzel.arclight.common.bridge.core.world.entity.api;

import org.bukkit.entity.ExperienceOrb$SpawnReason;

import java.util.UUID;

/** Fusion Core: Paper's orb spawn reason, source and trigger (patch 0125), saved as {@code Paper.ExpData}. */
public interface ExperienceOrbApiBridge {

    ExperienceOrb$SpawnReason fusioncore$spawnReason();

    UUID fusioncore$triggerEntityId();

    UUID fusioncore$sourceEntityId();

    void fusioncore$setSpawnData(ExperienceOrb$SpawnReason reason, UUID trigger, UUID source);
}
