package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's lightning API (patch 0437).
 */
@Mixin(value = org.bukkit.entity.LightningStrike.class, remap = false)
public interface LightningStrikeMixin {

    default int getFlashCount() {
        net.minecraft.world.entity.LightningBolt h = FusionCoreEntities.nms(this);
        return h.flashes;
    }

    default void setFlashCount(int flashes) {
        com.google.common.base.Preconditions.checkArgument(flashes >= 0, "Flashes has to be a positive number!");
        net.minecraft.world.entity.LightningBolt h = FusionCoreEntities.nms(this);
        h.flashes = flashes;
    }

    default org.bukkit.entity.Entity getCausingEntity() {
        net.minecraft.world.entity.LightningBolt h = FusionCoreEntities.nms(this);
        net.minecraft.server.level.ServerPlayer cause = h.getCause();
        return cause == null ? null : ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) cause).bridge$getBukkitEntity();
    }
}
