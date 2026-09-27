package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's fishing state API (patch 0674).
 */
@Mixin(value = org.bukkit.entity.FishHook.class, remap = false)
public interface FishHookMixin {

    default int getWaitTime() {
        net.minecraft.world.entity.projectile.FishingHook h = FusionCoreEntities.nms(this);
        return h.timeUntilLured;
    }

    default void setWaitTime(int ticks) {
        net.minecraft.world.entity.projectile.FishingHook h = FusionCoreEntities.nms(this);
        h.timeUntilLured = ticks;
    }

    default int getTimeUntilBite() {
        net.minecraft.world.entity.projectile.FishingHook h = FusionCoreEntities.nms(this);
        return h.timeUntilHooked;
    }

    default void setTimeUntilBite(int ticks) {
        com.google.common.base.Preconditions.checkArgument(ticks >= 1, "Cannot set time until bite to less than 1 (%s<1)", ticks);
        net.minecraft.world.entity.projectile.FishingHook h = FusionCoreEntities.nms(this);
        // the fish angle is reset only when this call enters the hook into the lure stage
        boolean alreadyInLuringPhase = h.timeUntilHooked > 0 && h.timeUntilLured <= 0;
        if (!alreadyInLuringPhase) {
            ((io.izzel.arclight.common.bridge.core.world.entity.projectile.FishingHookBridge) h).fusioncore$randomFishAngle();
            h.timeUntilLured = 0;
        }
        h.timeUntilHooked = ticks;
    }

    default void resetFishingState() {
        net.minecraft.world.entity.projectile.FishingHook h = FusionCoreEntities.nms(this);
        ((io.izzel.arclight.common.bridge.core.world.entity.projectile.FishingHookBridge) h).fusioncore$resetTimeUntilLured();
        h.timeUntilHooked = 0;
    }
}
