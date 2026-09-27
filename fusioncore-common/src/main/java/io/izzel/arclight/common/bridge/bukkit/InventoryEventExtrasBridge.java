package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: Paper's additions to inventory events that the core reads back. */
public interface InventoryEventExtrasBridge {

    interface FurnaceBurn {
        boolean bridge$willConsumeFuel();
    }

    interface Open {
        net.kyori.adventure.text.Component bridge$titleOverride();
    }
}
