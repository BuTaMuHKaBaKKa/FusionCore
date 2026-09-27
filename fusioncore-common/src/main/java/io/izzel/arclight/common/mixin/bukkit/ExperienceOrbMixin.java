package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.entity.api.ExperienceOrbApiBridge;
import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.bukkit.entity.ExperienceOrb$SpawnReason;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's orb count (patch 0355), spawn reason, trigger and source (patch 0125).
 */
@Mixin(value = org.bukkit.entity.ExperienceOrb.class, remap = false)
public interface ExperienceOrbMixin {

    default int getCount() {
        net.minecraft.world.entity.ExperienceOrb h = FusionCoreEntities.nms(this);
        return h.count;
    }

    default void setCount(int count) {
        net.minecraft.world.entity.ExperienceOrb h = FusionCoreEntities.nms(this);
        h.count = count;
    }

    default java.util.UUID getTriggerEntityId() {
        return ((ExperienceOrbApiBridge) FusionCoreEntities.nms(this)).fusioncore$triggerEntityId();
    }

    default java.util.UUID getSourceEntityId() {
        return ((ExperienceOrbApiBridge) FusionCoreEntities.nms(this)).fusioncore$sourceEntityId();
    }

    default ExperienceOrb$SpawnReason getSpawnReason() {
        return ((ExperienceOrbApiBridge) FusionCoreEntities.nms(this)).fusioncore$spawnReason();
    }

    @Deprecated
    default boolean isFromBottle() {
        return this.getSpawnReason() == ExperienceOrb$SpawnReason.EXP_BOTTLE;
    }
}
