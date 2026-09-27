package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge;
import org.bukkit.PortalType;
import org.bukkit.event.entity.EntityPortalEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's {@code EntityPortalEvent#getPortalType} (API patch 0382); Spigot's constructors give {@code CUSTOM}, as Paper's. */
@Mixin(value = EntityPortalEvent.class, remap = false)
public abstract class EntityPortalEventMixin implements EntityEventExtrasBridge.Portal {

    @Unique private PortalType fusioncore$portalType = PortalType.CUSTOM;

    public PortalType getPortalType() {
        return this.fusioncore$portalType;
    }

    @Override
    public void bridge$setPortalType(PortalType type) {
        this.fusioncore$portalType = type;
    }
}
