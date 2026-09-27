package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.entity.Phantom;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

/**
 * Fusion Core: Paper's {@code Phantom#getSpawningEntity()}, so plugins compiled against Paper
 * resolve it. {@code CraftPhantomMixin} overrides it on the real entity.
 */
@Mixin(value = Phantom.class, remap = false)
public interface PhantomMixin {

    /** Paper's {@code shouldBurnInDay} for phantoms (patch 0528). */
    default boolean shouldBurnInDay() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.api.SunBurnBridge) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this)).fusioncore$shouldBurnInDay();
    }

    default void setShouldBurnInDay(boolean shouldBurnInDay) {
        ((io.izzel.arclight.common.bridge.core.world.entity.api.SunBurnBridge) io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this)).fusioncore$setShouldBurnInDay(shouldBurnInDay);
    }

    default UUID getSpawningEntity() {
        return null;
    }

    /** Paper's phantom anchor (patch 0557). */
    default org.bukkit.Location getAnchorLocation() {
        net.minecraft.world.entity.monster.Phantom h = io.izzel.arclight.common.mod.util.FusionCoreEntities.nms(this);
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.location(this, h.anchorPoint);
    }

    default void setAnchorLocation(org.bukkit.Location location) {
        com.google.common.base.Preconditions.checkArgument(location != null, "location cannot be null");
        net.minecraft.world.entity.monster.Phantom h = io.izzel.arclight.common.mod.util.FusionCoreEntities.nms(this);
        h.anchorPoint = io.izzel.arclight.common.mod.util.FusionCoreEntities.blockPos(location);
    }
}
