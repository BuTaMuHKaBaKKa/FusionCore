package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.PlayerEventExtrasBridge;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's source of the experience (API patch 0043, server 0112): the orb, null otherwise. */
@Mixin(value = PlayerExpChangeEvent.class, remap = false)
public abstract class PlayerExpChangeEventMixin implements PlayerEventExtrasBridge.ExpChange {

    @Unique private Entity fusioncore$source;

    public Entity getSource() {
        return this.fusioncore$source;
    }

    @Override
    public void bridge$setSource(Entity source) {
        this.fusioncore$source = source;
    }
}
