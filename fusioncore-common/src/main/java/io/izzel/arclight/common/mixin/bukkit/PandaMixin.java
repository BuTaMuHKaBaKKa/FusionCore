package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's panda API (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Panda.class, remap = false)
public interface PandaMixin {

    default void setSneezeTicks(int ticks) {
        net.minecraft.world.entity.animal.Panda h = FusionCoreEntities.nms(this);
        h.setSneezeCounter(ticks);
    }

    default int getSneezeTicks() {
        net.minecraft.world.entity.animal.Panda h = FusionCoreEntities.nms(this);
        return h.getSneezeCounter();
    }

    default void setEatingTicks(int ticks) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.PandaApiInvoker) FusionCoreEntities.handle(this)).fusioncore$setEatCounter(ticks);
    }

    default int getEatingTicks() {
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.PandaApiInvoker) FusionCoreEntities.handle(this)).fusioncore$getEatCounter();
    }

    default void setUnhappyTicks(int ticks) {
        net.minecraft.world.entity.animal.Panda h = FusionCoreEntities.nms(this);
        h.setUnhappyCounter(ticks);
    }

    default org.bukkit.entity.Panda.Gene getCombinedGene() {
        net.minecraft.world.entity.animal.Panda h = FusionCoreEntities.nms(this);
        return org.bukkit.craftbukkit.v.entity.CraftPanda.fromNms(h.getVariant());
    }

    default void setIsOnBack(boolean onBack) {
        ((org.bukkit.entity.Panda) this).setOnBack(onBack);
    }

    default void setIsSitting(boolean sitting) {
        ((org.bukkit.entity.Panda) this).setSitting(sitting);
    }
}
