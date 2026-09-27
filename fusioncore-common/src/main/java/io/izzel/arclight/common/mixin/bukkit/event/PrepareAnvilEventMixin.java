package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's superclass, set by {@code SuperclassProcessor}; the mixin only hands the class to it. */
@Mixin(value = PrepareAnvilEvent.class, remap = false)
public abstract class PrepareAnvilEventMixin {
}
