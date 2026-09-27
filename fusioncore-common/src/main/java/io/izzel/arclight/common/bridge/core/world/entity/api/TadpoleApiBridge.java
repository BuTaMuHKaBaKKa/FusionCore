package io.izzel.arclight.common.bridge.core.world.entity.api;

/** Fusion Core: Paper's tadpole age lock (patch 0557), saved as {@code AgeLocked}, also in buckets. */
public interface TadpoleApiBridge {

    boolean fusioncore$ageLocked();

    void fusioncore$setAgeLocked(boolean ageLocked);
}
