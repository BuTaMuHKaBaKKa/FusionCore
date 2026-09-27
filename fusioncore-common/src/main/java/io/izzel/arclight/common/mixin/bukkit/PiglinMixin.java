package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's piglin API (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Piglin.class, remap = false)
public interface PiglinMixin {

    default void setChargingCrossbow(boolean chargingCrossbow) {
        net.minecraft.world.entity.monster.piglin.Piglin h = FusionCoreEntities.nms(this);
        h.setChargingCrossbow(chargingCrossbow);
    }

    default boolean isChargingCrossbow() {
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.PiglinApiInvoker) FusionCoreEntities.handle(this)).fusioncore$isChargingCrossbow();
    }

    default void setDancing(boolean dancing) {
        net.minecraft.world.entity.monster.piglin.Piglin h = FusionCoreEntities.nms(this);
        if (dancing) {
            h.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.DANCING, true);
            h.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.CELEBRATE_LOCATION, h.getOnPos());
        } else {
            h.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.DANCING);
            h.getBrain().eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.CELEBRATE_LOCATION);
        }
    }

    default void setDancing(long duration) {
        net.minecraft.world.entity.monster.piglin.Piglin h = FusionCoreEntities.nms(this);
        h.getBrain().setMemoryWithExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.DANCING, true, duration);
        h.getBrain().setMemoryWithExpiry(net.minecraft.world.entity.ai.memory.MemoryModuleType.CELEBRATE_LOCATION, h.getOnPos(), duration);
    }

    default boolean isDancing() {
        net.minecraft.world.entity.monster.piglin.Piglin h = FusionCoreEntities.nms(this);
        return h.isDancing();
    }
}
