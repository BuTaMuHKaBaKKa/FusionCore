// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package com.destroystokyo.paper.event.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a Slime decides to change direction to target a LivingEntity.
 * <p>
 * This event does not fire for the entity's actual movement. Only when it
 * is choosing to start moving.
 */
public class SlimeTargetLivingEntityEvent extends SlimePathfindEvent {

    private final LivingEntity target;

    @ApiStatus.Internal
    public SlimeTargetLivingEntityEvent(final Slime slime, final LivingEntity target) {
        super(slime);
        this.target = target;
    }

    /**
     * Get the targeted entity
     *
     * @return Targeted entity
     */
    public LivingEntity getTarget() {
        return this.target;
    }
}
