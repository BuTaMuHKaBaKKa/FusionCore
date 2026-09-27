package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's vex API (patches 0209, 0557).
 */
@Mixin(value = org.bukkit.entity.Vex.class, remap = false)
public interface VexMixin {

    default org.bukkit.entity.Mob getSummoner() {
        net.minecraft.world.entity.monster.Vex h = FusionCoreEntities.nms(this);
        net.minecraft.world.entity.Mob owner = h.getOwner();
        return owner == null ? null : (org.bukkit.entity.Mob) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) owner).bridge$getBukkitEntity();
    }

    default void setSummoner(org.bukkit.entity.Mob summoner) {
        net.minecraft.world.entity.monster.Vex h = FusionCoreEntities.nms(this);
        h.setOwner(summoner == null ? null : FusionCoreEntities.nms(summoner));
    }

    default boolean hasLimitedLifetime() {
        net.minecraft.world.entity.monster.Vex h = FusionCoreEntities.nms(this);
        return h.hasLimitedLife;
    }

    default void setLimitedLifetime(boolean hasLimitedLifetime) {
        net.minecraft.world.entity.monster.Vex h = FusionCoreEntities.nms(this);
        h.hasLimitedLife = hasLimitedLifetime;
    }

    default int getLimitedLifetimeTicks() {
        net.minecraft.world.entity.monster.Vex h = FusionCoreEntities.nms(this);
        return h.limitedLifeTicks;
    }

    default void setLimitedLifetimeTicks(int ticks) {
        net.minecraft.world.entity.monster.Vex h = FusionCoreEntities.nms(this);
        h.limitedLifeTicks = ticks;
    }
}
