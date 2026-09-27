package io.izzel.arclight.common.mixin.bukkit.event;

import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code defaultTitle()} on {@link InventoryType} (Paper patch 0006 "Adventure"): the Spigot title as text. */
@Mixin(value = InventoryType.class, remap = false)
public abstract class InventoryTypeMixin {

    @Shadow public abstract String getDefaultTitle();

    public Component defaultTitle() {
        return Component.text(this.getDefaultTitle());
    }
}
