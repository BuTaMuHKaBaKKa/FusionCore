package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's wither API (patch 0557), portal travel with a core hook.
 */
@Mixin(value = org.bukkit.entity.Wither.class, remap = false)
public interface WitherMixin {

    default boolean canTravelThroughPortals() {
        return FusionCoreEntities.handle(this).canUsePortal(false);
    }

    default void setCanTravelThroughPortals(boolean value) {
        ((io.izzel.arclight.common.bridge.core.world.entity.api.WitherBossApiBridge) FusionCoreEntities.handle(this)).fusioncore$setCanTravelThroughPortals(value);
    }

    default boolean isCharged() {
        net.minecraft.world.entity.boss.wither.WitherBoss h = FusionCoreEntities.nms(this);
        return h.isPowered();
    }

    default int getInvulnerableTicks() {
        net.minecraft.world.entity.boss.wither.WitherBoss h = FusionCoreEntities.nms(this);
        return h.getInvulnerableTicks();
    }

    default void setInvulnerableTicks(int ticks) {
        net.minecraft.world.entity.boss.wither.WitherBoss h = FusionCoreEntities.nms(this);
        h.setInvulnerableTicks(ticks);
    }

    default void enterInvulnerabilityPhase() {
        net.minecraft.world.entity.boss.wither.WitherBoss h = FusionCoreEntities.nms(this);
        h.makeInvulnerable();
    }
}
