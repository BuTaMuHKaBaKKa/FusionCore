package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's deprecated {@code PlayerResourcePackStatusEvent#getHash} (API patch 0026), always null as in Paper. */
@Mixin(value = PlayerResourcePackStatusEvent.class, remap = false)
public abstract class PlayerResourcePackStatusEventMixin {

    public String getHash() {
        return null;
    }
}
