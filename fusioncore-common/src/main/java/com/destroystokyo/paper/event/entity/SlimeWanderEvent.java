// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package com.destroystokyo.paper.event.entity;

import org.bukkit.entity.Slime;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a Slime decides to start wandering.
 * <p>
 * This event does not fire for the entity's actual movement. Only when it
 * is choosing to start moving.
 */
public class SlimeWanderEvent extends SlimePathfindEvent {

    @ApiStatus.Internal
    public SlimeWanderEvent(final Slime slime) {
        super(slime);
    }
}
