package io.izzel.arclight.common.mixin.bukkit.event;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's {@code PlayerMoveEvent} helpers (API patch 0263). */
@Mixin(value = PlayerMoveEvent.class, remap = false)
public abstract class PlayerMoveEventMixin {

    @Shadow public abstract Location getFrom();
    @Shadow public abstract Location getTo();

    public boolean hasChangedPosition() {
        return this.hasExplicitlyChangedPosition() || !this.getFrom().getWorld().equals(this.getTo().getWorld());
    }

    public boolean hasExplicitlyChangedPosition() {
        Location from = this.getFrom();
        Location to = this.getTo();
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
    }

    public boolean hasChangedBlock() {
        return this.hasExplicitlyChangedBlock() || !this.getFrom().getWorld().equals(this.getTo().getWorld());
    }

    public boolean hasExplicitlyChangedBlock() {
        Location from = this.getFrom();
        Location to = this.getTo();
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }

    public boolean hasChangedOrientation() {
        Location from = this.getFrom();
        Location to = this.getTo();
        return from.getPitch() != to.getPitch() || from.getYaw() != to.getYaw();
    }
}
