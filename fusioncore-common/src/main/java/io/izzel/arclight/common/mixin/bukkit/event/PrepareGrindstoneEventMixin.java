package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's superclass, set by {@code SuperclassProcessor}; the mixin only hands the class to it. */
@Mixin(value = PrepareGrindstoneEvent.class, remap = false)
public abstract class PrepareGrindstoneEventMixin {
}
