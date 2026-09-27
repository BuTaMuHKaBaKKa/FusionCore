package io.izzel.arclight.common.bridge.core.world.entity.api;

import java.util.UUID;

/** Fusion Core: Paper's firework spawning entity (patch 0121), saved as {@code SpawningEntity}. */
public interface FireworkApiBridge {

    UUID fusioncore$spawningEntity();

    void fusioncore$setSpawningEntity(UUID spawningEntity);
}
