package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.level.block.entity.BeaconBlockEntityBridge;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's custom beacon effect range (patch 0429), on the state's snapshot like Paper's CraftBeacon; {@link io.papermc.paper.block.LockableTileState} as in Paper (patch 0380). */
@Mixin(value = org.bukkit.block.Beacon.class, remap = false)
public interface BeaconMixin extends io.papermc.paper.block.LockableTileState {

    default double getEffectRange() {
        return ((BeaconBlockEntityBridge) ((CraftBlockEntityStateAccessor) this).fusioncore$getSnapshot()).fusioncore$effectRange();
    }

    default void setEffectRange(double range) {
        ((BeaconBlockEntityBridge) ((CraftBlockEntityStateAccessor) this).fusioncore$getSnapshot()).fusioncore$setEffectRange(range);
    }

    default void resetEffectRange() {
        ((BeaconBlockEntityBridge) ((CraftBlockEntityStateAccessor) this).fusioncore$getSnapshot()).fusioncore$setEffectRange(-1);
    }
}
