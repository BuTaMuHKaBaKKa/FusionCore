package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.EntityDamageByEntityEventBridge;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's {@code EntityDamageByEntityEvent#isCritical}. */
@Mixin(value = EntityDamageByEntityEvent.class, remap = false)
public abstract class EntityDamageByEntityEventMixin implements EntityDamageByEntityEventBridge {

    @Unique private boolean fusioncore$critical;

    public boolean isCritical() {
        return this.fusioncore$critical;
    }

    @Override
    public void bridge$setCritical(boolean critical) {
        this.fusioncore$critical = critical;
    }
}
