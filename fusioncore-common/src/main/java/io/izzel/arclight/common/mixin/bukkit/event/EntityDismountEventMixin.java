package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.EntityEventExtrasBridge;
import org.bukkit.event.entity.EntityDismountEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's {@code EntityDismountEvent#isCancellable} (API patch 0155): a dismount that cannot be cancelled ignores
 * {@code setCancelled(true)}. The server fires it cancellable, as Paper 1.21.1 does; only Paper's constructor makes it otherwise.
 */
@Mixin(value = EntityDismountEvent.class, remap = false)
public abstract class EntityDismountEventMixin implements EntityEventExtrasBridge.Cancellability {

    @Unique private boolean fusioncore$cancellable = true;

    public boolean isCancellable() {
        return this.fusioncore$cancellable;
    }

    @Inject(method = "setCancelled", cancellable = true, at = @At("HEAD"))
    private void fusioncore$notCancellable(boolean cancel, CallbackInfo ci) {
        if (cancel && !this.fusioncore$cancellable) {
            ci.cancel();
        }
    }

    @Override
    public void bridge$setCancellable(boolean cancellable) {
        this.fusioncore$cancellable = cancellable;
    }
}
