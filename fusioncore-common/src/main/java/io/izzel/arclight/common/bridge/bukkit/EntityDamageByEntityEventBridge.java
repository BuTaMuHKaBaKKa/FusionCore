package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: Paper's critical flag of {@code EntityDamageByEntityEvent} (patch 0588). */
public interface EntityDamageByEntityEventBridge {

    void bridge$setCritical(boolean critical);
}
