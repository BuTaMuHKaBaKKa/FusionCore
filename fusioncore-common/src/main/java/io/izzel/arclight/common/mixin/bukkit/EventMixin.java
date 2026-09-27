package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code Event#callEvent()}. */
@Mixin(value = org.bukkit.event.Event.class, remap = false)
public abstract class EventMixin {

    /** Paper: calls the event and tells whether it went through (not cancelled). */
    public boolean callEvent() {
        org.bukkit.Bukkit.getPluginManager().callEvent((org.bukkit.event.Event) (Object) this);
        if (this instanceof org.bukkit.event.Cancellable cancellable) {
            return !cancellable.isCancelled();
        }
        return true;
    }
}
