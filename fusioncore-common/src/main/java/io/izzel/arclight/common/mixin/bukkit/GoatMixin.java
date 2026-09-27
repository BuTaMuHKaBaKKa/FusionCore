package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code ram} (patch 0591): the brain memories Paper's {@code Goat#ram} sets.
 */
@Mixin(value = org.bukkit.entity.Goat.class, remap = false)
public interface GoatMixin {

    default void ram(org.bukkit.entity.LivingEntity entity) {
        net.minecraft.world.entity.animal.goat.Goat h = FusionCoreEntities.nms(this);
        net.minecraft.world.entity.ai.Brain<net.minecraft.world.entity.animal.goat.Goat> brain = h.getBrain();
        brain.setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.RAM_TARGET, FusionCoreEntities.handle(entity).position());
        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.RAM_COOLDOWN_TICKS);
        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.BREED_TARGET);
        brain.eraseMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.TEMPTING_PLAYER);
        brain.setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.RAM);
    }
}
