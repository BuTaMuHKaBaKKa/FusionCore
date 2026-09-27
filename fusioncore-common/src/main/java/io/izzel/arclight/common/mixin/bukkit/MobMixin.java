package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreMobs;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code Mob} API: pathfinder, look control, head rotation, handedness, experience reward,
 * daylight, aggression, seeded loot table and {@link io.papermc.paper.entity.Leashable}.
 */
@Mixin(value = org.bukkit.entity.Mob.class, remap = false)
public interface MobMixin extends io.papermc.paper.entity.Leashable {

    default com.destroystokyo.paper.entity.Pathfinder getPathfinder() {
        return FusionCoreMobs.pathfinder((org.bukkit.entity.Mob) this);
    }

    default boolean isInDaylight() {
        return FusionCoreMobs.isInDaylight((org.bukkit.entity.Mob) this);
    }

    default void lookAt(org.bukkit.Location location) {
        FusionCoreMobs.lookAt((org.bukkit.entity.Mob) this, location);
    }

    default void lookAt(org.bukkit.Location location, float headRotationSpeed, float maxHeadPitch) {
        FusionCoreMobs.lookAt((org.bukkit.entity.Mob) this, location, headRotationSpeed, maxHeadPitch);
    }

    default void lookAt(org.bukkit.entity.Entity entity) {
        FusionCoreMobs.lookAt((org.bukkit.entity.Mob) this, entity);
    }

    default void lookAt(org.bukkit.entity.Entity entity, float headRotationSpeed, float maxHeadPitch) {
        FusionCoreMobs.lookAt((org.bukkit.entity.Mob) this, entity, headRotationSpeed, maxHeadPitch);
    }

    default void lookAt(double x, double y, double z) {
        FusionCoreMobs.mob(this).getLookControl().setLookAt(x, y, z);
    }

    default void lookAt(double x, double y, double z, float headRotationSpeed, float maxHeadPitch) {
        FusionCoreMobs.mob(this).getLookControl().setLookAt(x, y, z, headRotationSpeed, maxHeadPitch);
    }

    default int getHeadRotationSpeed() {
        return FusionCoreMobs.mob(this).getHeadRotSpeed();
    }

    default int getMaxHeadPitch() {
        return FusionCoreMobs.mob(this).getMaxHeadXRot();
    }

    default boolean isLeftHanded() {
        return FusionCoreMobs.mob(this).isLeftHanded();
    }

    default void setLeftHanded(boolean leftHanded) {
        FusionCoreMobs.mob(this).setLeftHanded(leftHanded);
    }

    default int getPossibleExperienceReward() {
        return FusionCoreMobs.possibleExperienceReward((org.bukkit.entity.Mob) this);
    }

    default boolean isAggressive() {
        return FusionCoreMobs.mob(this).isAggressive();
    }

    default void setAggressive(boolean aggressive) {
        FusionCoreMobs.mob(this).setAggressive(aggressive);
    }

    default void setLootTable(org.bukkit.loot.LootTable table, long seed) {
        ((org.bukkit.entity.Mob) this).setLootTable(table);
        ((org.bukkit.entity.Mob) this).setSeed(seed);
    }
}
