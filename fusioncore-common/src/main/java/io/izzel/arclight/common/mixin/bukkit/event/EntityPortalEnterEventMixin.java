package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge;
import org.bukkit.PortalType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fusion Core: Paper's {@code EntityPortalEnterEvent} (API patch 0382): cancellable, with the portal type. Spigot's constructor gives
 * {@code CUSTOM}, as Paper's deprecated one does; the core fires it with the real type.
 */
@Mixin(value = EntityPortalEnterEvent.class, remap = false)
public abstract class EntityPortalEnterEventMixin implements Cancellable, EntityEventExtrasBridge.Portal {

    @Unique private boolean fusioncore$cancelled;
    @Unique private PortalType fusioncore$portalType = PortalType.CUSTOM;

    public PortalType getPortalType() {
        return this.fusioncore$portalType;
    }

    @Override
    public boolean isCancelled() {
        return this.fusioncore$cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.fusioncore$cancelled = cancel;
    }

    @Override
    public void bridge$setPortalType(PortalType type) {
        this.fusioncore$portalType = type;
    }
}
