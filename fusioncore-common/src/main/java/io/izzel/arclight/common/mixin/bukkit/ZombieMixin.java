package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's zombie API (patches 0268, 0457); burning in daylight with a core hook.
 */
@Mixin(value = org.bukkit.entity.Zombie.class, remap = false)
public interface ZombieMixin {

    default boolean shouldBurnInDay() {
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.ZombieApiInvoker) FusionCoreEntities.handle(this)).fusioncore$isSunSensitive();
    }

    default void setShouldBurnInDay(boolean shouldBurnInDay) {
        ((io.izzel.arclight.common.bridge.core.world.entity.api.SunBurnBridge) FusionCoreEntities.handle(this)).fusioncore$setShouldBurnInDay(shouldBurnInDay);
    }

    default boolean isDrowning() {
        net.minecraft.world.entity.monster.Zombie h = FusionCoreEntities.nms(this);
        return h.isUnderWaterConverting();
    }

    default void startDrowning(int drownedConversionTime) {
        net.minecraft.world.entity.monster.Zombie h = FusionCoreEntities.nms(this);
        h.startUnderWaterConversion(drownedConversionTime);
    }

    /** Paper's {@code Zombie#stopDrowning}. */
    default void stopDrowning() {
        net.minecraft.world.entity.monster.Zombie h = FusionCoreEntities.nms(this);
        h.conversionTime = -1;
        h.getEntityData().set(net.minecraft.world.entity.monster.Zombie.DATA_DROWNED_CONVERSION_ID, false);
    }

    default boolean isArmsRaised() {
        net.minecraft.world.entity.monster.Zombie h = FusionCoreEntities.nms(this);
        return h.isAggressive();
    }

    default void setArmsRaised(boolean raised) {
        net.minecraft.world.entity.monster.Zombie h = FusionCoreEntities.nms(this);
        h.setAggressive(raised);
    }

    default boolean supportsBreakingDoors() {
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.ZombieApiInvoker) FusionCoreEntities.handle(this)).fusioncore$supportsBreakDoorGoal();
    }
}
