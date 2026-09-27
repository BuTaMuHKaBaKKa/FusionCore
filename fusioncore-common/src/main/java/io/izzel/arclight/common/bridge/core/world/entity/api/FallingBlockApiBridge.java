package io.izzel.arclight.common.bridge.core.world.entity.api;

/** Fusion Core: Paper's falling block auto expire (patch 0700), saved as {@code Paper.AutoExpire} when off. */
public interface FallingBlockApiBridge {

    boolean fusioncore$autoExpire();

    void fusioncore$setAutoExpire(boolean autoExpire);
}
