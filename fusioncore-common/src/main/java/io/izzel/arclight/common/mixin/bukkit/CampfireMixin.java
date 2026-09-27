package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreBlockStates;
import org.bukkit.block.Campfire;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's per-slot cooking switch on {@link Campfire} (patch 0634), on the state's snapshot as in Paper's CraftCampfire. */
@Mixin(value = Campfire.class, remap = false)
public interface CampfireMixin {

    default void stopCooking() {
        for (int i = 0; i < 4; i++) {
            this.stopCooking(i);
        }
    }

    default void startCooking() {
        for (int i = 0; i < 4; i++) {
            this.startCooking(i);
        }
    }

    default boolean stopCooking(int index) {
        return FusionCoreBlockStates.setCookingDisabled(this, index, true);
    }

    default boolean startCooking(int index) {
        return FusionCoreBlockStates.setCookingDisabled(this, index, false);
    }

    default boolean isCookingDisabled(int index) {
        return FusionCoreBlockStates.isCookingDisabled(this, index);
    }
}
