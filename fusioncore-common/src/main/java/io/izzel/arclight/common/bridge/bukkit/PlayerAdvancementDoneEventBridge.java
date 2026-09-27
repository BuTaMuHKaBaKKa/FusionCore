package io.izzel.arclight.common.bridge.bukkit;

import net.kyori.adventure.text.Component;

/** Fusion Core: access to Paper's announcement message on {@code PlayerAdvancementDoneEvent}. */
public interface PlayerAdvancementDoneEventBridge {

    Component fusioncore$message();

    void fusioncore$setMessage(Component message);
}
