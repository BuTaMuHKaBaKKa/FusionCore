package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's endermite lifetime (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Endermite.class, remap = false)
public interface EndermiteMixin {

    default void setLifetimeTicks(int ticks) {
        net.minecraft.world.entity.monster.Endermite h = FusionCoreEntities.nms(this);
        h.life = ticks;
    }

    default int getLifetimeTicks() {
        net.minecraft.world.entity.monster.Endermite h = FusionCoreEntities.nms(this);
        return h.life;
    }
}
