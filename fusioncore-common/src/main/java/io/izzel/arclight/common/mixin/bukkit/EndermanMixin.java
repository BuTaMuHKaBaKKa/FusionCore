package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's enderman API (patches 0185, 0557).
 */
@Mixin(value = org.bukkit.entity.Enderman.class, remap = false)
public interface EndermanMixin {

    default boolean teleportRandomly() {
        net.minecraft.world.entity.monster.EnderMan h = FusionCoreEntities.nms(this);
        return h.teleport();
    }

    default boolean isScreaming() {
        net.minecraft.world.entity.monster.EnderMan h = FusionCoreEntities.nms(this);
        return h.isCreepy();
    }

    default void setScreaming(boolean screaming) {
        net.minecraft.world.entity.monster.EnderMan h = FusionCoreEntities.nms(this);
        h.getEntityData().set(net.minecraft.world.entity.monster.EnderMan.DATA_CREEPY, screaming);
    }

    default boolean hasBeenStaredAt() {
        net.minecraft.world.entity.monster.EnderMan h = FusionCoreEntities.nms(this);
        return h.hasBeenStaredAt();
    }

    default void setHasBeenStaredAt(boolean hasBeenStaredAt) {
        net.minecraft.world.entity.monster.EnderMan h = FusionCoreEntities.nms(this);
        h.getEntityData().set(net.minecraft.world.entity.monster.EnderMan.DATA_STARED_AT, hasBeenStaredAt);
    }
}
