package io.izzel.arclight.common.bridge.bukkit;

import org.bukkit.event.inventory.InventoryCloseEvent$Reason;

/** Fusion Core: sets Paper's close reason for {@code FusionCoreEvents.newInventoryCloseEvent}. */
public interface InventoryCloseEventBridge {

    void fusioncore$setReason(InventoryCloseEvent$Reason reason);
}
