package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's caravan API (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Llama.class, remap = false)
public interface LlamaMixin {

    default boolean inCaravan() {
        net.minecraft.world.entity.animal.horse.Llama h = FusionCoreEntities.nms(this);
        return h.inCaravan();
    }

    default void joinCaravan(org.bukkit.entity.Llama llama) {
        net.minecraft.world.entity.animal.horse.Llama h = FusionCoreEntities.nms(this);
        h.joinCaravan(FusionCoreEntities.nms(llama));
    }

    default void leaveCaravan() {
        net.minecraft.world.entity.animal.horse.Llama h = FusionCoreEntities.nms(this);
        h.leaveCaravan();
    }

    default boolean hasCaravanTail() {
        net.minecraft.world.entity.animal.horse.Llama h = FusionCoreEntities.nms(this);
        return h.hasCaravanTail();
    }

    default org.bukkit.entity.Llama getCaravanHead() {
        net.minecraft.world.entity.animal.horse.Llama h = FusionCoreEntities.nms(this);
        return h.getCaravanHead() == null ? null : (org.bukkit.entity.Llama) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) h.getCaravanHead()).bridge$getBukkitEntity();
    }

    default org.bukkit.entity.Llama getCaravanTail() {
        net.minecraft.world.entity.animal.horse.Llama h = FusionCoreEntities.nms(this);
        return h.caravanTail == null ? null : (org.bukkit.entity.Llama) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) h.caravanTail).bridge$getBukkitEntity();
    }
}
