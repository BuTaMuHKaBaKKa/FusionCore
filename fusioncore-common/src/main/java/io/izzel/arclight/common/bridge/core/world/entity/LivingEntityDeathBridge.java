package io.izzel.arclight.common.bridge.core.world.entity;

import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: per-entity state of Paper's cancellable death, kept by {@code LivingEntityDeathMixin}. */
public interface LivingEntityDeathBridge {

    /** Paper's {@code silentDeath}: the entity dies from {@code hurt} without the death sound vanilla would play there. */
    boolean fusioncore$isSilentDeath();

    /** The death event fired during the running {@code dropAllDeathLoot}, set by {@code EntityEventHandler}. */
    void fusioncore$setDeathEvent(EntityDeathEvent event);

    /** Takes the death event of the last {@code dropAllDeathLoot} (null if none fired) and forgets it. */
    @Nullable EntityDeathEvent fusioncore$takeDeathEvent();

    /**
     * Paper clears what the death takes from the entity (a mob's dropped equipment, a llama's chest) only when the death event is not
     * cancelled. Returns false outside {@code dropAllDeathLoot}: then the caller clears at once.
     */
    boolean fusioncore$deferDeathClear(Runnable clear);
}
