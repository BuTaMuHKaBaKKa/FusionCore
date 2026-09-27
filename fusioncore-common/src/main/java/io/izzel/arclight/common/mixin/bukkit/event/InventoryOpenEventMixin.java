package io.izzel.arclight.common.mixin.bukkit.event;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's {@code InventoryOpenEvent#titleOverride} (API patch 0417, server 0874): the title the client is shown. */
@Mixin(value = InventoryOpenEvent.class, remap = false)
public abstract class InventoryOpenEventMixin implements io.izzel.arclight.common.bridge.bukkit.InventoryEventExtrasBridge.Open {

    @Unique private Component fusioncore$titleOverride;

    public Component titleOverride() {
        return this.fusioncore$titleOverride;
    }

    public void titleOverride(Component titleOverride) {
        this.fusioncore$titleOverride = titleOverride;
    }

    @Override
    public Component bridge$titleOverride() {
        return this.fusioncore$titleOverride;
    }
}
