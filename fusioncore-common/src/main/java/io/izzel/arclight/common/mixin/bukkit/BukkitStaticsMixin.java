package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: hands {@link Bukkit} to {@code StaticHolderProcessor}, which adds Paper's statics from {@code FusionCoreBukkit}. */
@Mixin(value = Bukkit.class, remap = false)
public abstract class BukkitStaticsMixin {
}
