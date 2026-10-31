package io.izzel.arclight.common.mod.util;

/**
 * Fusion Core: per-level state of the core's features, kept on the level itself (a mixin on Level) so that it lives and dies
 * with the level; a static map keyed by level keeps unloaded levels alive whenever its values reach back to the level.
 */
public interface FusionCoreLevelData {

    /** The Anti-Xray controller of the level, or null before its first use */
    Object fusioncore$antiXray();

    void fusioncore$setAntiXray(Object controller);

    /** The nearest-player index of optimize-mob-despawn, or null */
    Object fusioncore$despawnIndex();

    void fusioncore$setDespawnIndex(Object index);
}
