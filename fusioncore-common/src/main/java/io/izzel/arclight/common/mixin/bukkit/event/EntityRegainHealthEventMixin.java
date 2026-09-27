package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's {@code EntityRegainHealthEvent#isFastRegen} (API patch 0038): healing from saturation at a full food bar. */
@Mixin(value = EntityRegainHealthEvent.class, remap = false)
public abstract class EntityRegainHealthEventMixin implements EntityEventExtrasBridge.RegainHealth {

    @Unique private boolean fusioncore$fastRegen;

    public boolean isFastRegen() {
        return this.fusioncore$fastRegen;
    }

    @Override
    public void bridge$setFastRegen(boolean fastRegen) {
        this.fusioncore$fastRegen = fastRegen;
    }
}
