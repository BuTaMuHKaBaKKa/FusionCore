package io.izzel.arclight.common.mod.util;

import org.bukkit.event.HandlerList;

/** Fusion Core: Paper's static {@code VehicleCollisionEvent#getHandlerList}; the list is shared by both collision events. */
public final class FusionCoreVehicleCollisionEventStatics {

    public static final HandlerList HANDLERS = new HandlerList();

    private FusionCoreVehicleCollisionEventStatics() {
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
