package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's ravager timers (patch 0557).
 */
@Mixin(value = org.bukkit.entity.Ravager.class, remap = false)
public interface RavagerMixin {

    default int getAttackTicks() {
        net.minecraft.world.entity.monster.Ravager h = FusionCoreEntities.nms(this);
        return h.getAttackTick();
    }

    default void setAttackTicks(int ticks) {
        net.minecraft.world.entity.monster.Ravager h = FusionCoreEntities.nms(this);
        h.attackTick = ticks;
    }

    default int getStunnedTicks() {
        net.minecraft.world.entity.monster.Ravager h = FusionCoreEntities.nms(this);
        return h.getStunnedTick();
    }

    default void setStunnedTicks(int ticks) {
        net.minecraft.world.entity.monster.Ravager h = FusionCoreEntities.nms(this);
        h.stunnedTick = ticks;
    }

    default int getRoarTicks() {
        net.minecraft.world.entity.monster.Ravager h = FusionCoreEntities.nms(this);
        return h.getRoarTick();
    }

    default void setRoarTicks(int ticks) {
        net.minecraft.world.entity.monster.Ravager h = FusionCoreEntities.nms(this);
        h.roarTick = ticks;
    }
}
