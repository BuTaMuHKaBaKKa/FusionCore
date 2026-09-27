package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.EntityUnleashEventBridge;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's cancellable {@code EntityUnleashEvent} with {@code dropLeash} (API patches 0241, 0295). */
@Mixin(value = EntityUnleashEvent.class, remap = false)
public abstract class EntityUnleashEventMixin implements Cancellable, EntityUnleashEventBridge {

    @Unique private boolean fusioncore$cancelled;
    @Unique private boolean fusioncore$dropLeash;

    public boolean isDropLeash() {
        return this.fusioncore$dropLeash;
    }

    public void setDropLeash(boolean dropLeash) {
        this.fusioncore$dropLeash = dropLeash;
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
    public boolean bridge$isDropLeash() {
        return this.isDropLeash();
    }

    @Override
    public void bridge$setDropLeash(boolean dropLeash) {
        this.setDropLeash(dropLeash);
    }

    @Override
    public boolean bridge$isCancelled() {
        return this.isCancelled();
    }
}
