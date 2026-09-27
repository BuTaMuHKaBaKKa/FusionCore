package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code getHighestAnger} (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Warden.class, remap = false)
public interface WardenMixin {

    default int getHighestAnger() {
        net.minecraft.world.entity.monster.warden.Warden h = FusionCoreEntities.nms(this);
        return h.getAngerManagement().getActiveAnger(null);
    }
}
