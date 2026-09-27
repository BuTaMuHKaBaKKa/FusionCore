package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code GameEvent#getRange} and {@code getVibrationLevel} (from Paper's registry entry patch 1015). */
@Mixin(value = org.bukkit.GameEvent.class, remap = false)
public abstract class GameEventMixin {

    public int getRange() {
        return ((org.bukkit.craftbukkit.v.CraftGameEvent) (Object) this).getHandle().notificationRadius();
    }

    public int getVibrationLevel() {
        return net.minecraft.world.level.gameevent.vibrations.VibrationSystem.getGameEventFrequency(
            net.minecraft.core.registries.BuiltInRegistries.GAME_EVENT.wrapAsHolder(((org.bukkit.craftbukkit.v.CraftGameEvent) (Object) this).getHandle()));
    }
}
