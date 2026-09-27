package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/** Fusion Core: Paper's gateway and crystal API on {@link DragonBattle} (patch 0867). */
@Mixin(value = DragonBattle.class, remap = false)
public interface DragonBattleMixin {

    default int getGatewayCount() {
        return FusionCoreBlockStates.gatewayCount(((CraftDragonBattleAccessor) this).fusioncore$getHandle());
    }

    default boolean spawnNewGateway() {
        return FusionCoreBlockStates.spawnNewGateway(((CraftDragonBattleAccessor) this).fusioncore$getHandle());
    }

    default void spawnNewGateway(io.papermc.paper.math.Position position) {
        FusionCoreBlockStates.spawnNewGateway(((CraftDragonBattleAccessor) this).fusioncore$getHandle(), position);
    }

    default List<EnderCrystal> getRespawnCrystals() {
        return FusionCoreBlockStates.respawnCrystals(((CraftDragonBattleAccessor) this).fusioncore$getHandle());
    }

    default List<EnderCrystal> getHealingCrystals() {
        return FusionCoreBlockStates.healingCrystals(((CraftDragonBattleAccessor) this).fusioncore$getHandle());
    }
}
