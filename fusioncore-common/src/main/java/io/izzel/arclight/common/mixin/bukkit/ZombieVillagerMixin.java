package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's conversion without the entity event (patch 0557).
 */
@Mixin(value = org.bukkit.entity.ZombieVillager.class, remap = false)
public interface ZombieVillagerMixin {

    default void setConversionTime(int time, boolean broadcastEntityEvent) {
        if (time < 0 || broadcastEntityEvent) {
            ((org.bukkit.entity.ZombieVillager) this).setConversionTime(time);
        } else {
            ((io.izzel.arclight.common.bridge.core.world.entity.monster.ZombieVillagerPaperBridge) FusionCoreEntities.handle(this)).fusioncore$startConverting(null, time, false);
        }
    }
}
