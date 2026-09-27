package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: Paper's command flag and location of {@code TabCompleteEvent}. */
public interface TabCompleteEventBridge {

    void bridge$init(boolean isCommand, org.bukkit.Location location);
}
