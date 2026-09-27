package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's furnace minecart push (patch 0672).
 */
@Mixin(value = org.bukkit.entity.minecart.PoweredMinecart.class, remap = false)
public interface PoweredMinecartMixin {

    default double getPushX() {
        net.minecraft.world.entity.vehicle.MinecartFurnace h = FusionCoreEntities.nms(this);
        return h.xPush;
    }

    default double getPushZ() {
        net.minecraft.world.entity.vehicle.MinecartFurnace h = FusionCoreEntities.nms(this);
        return h.zPush;
    }

    default void setPushX(double xPush) {
        net.minecraft.world.entity.vehicle.MinecartFurnace h = FusionCoreEntities.nms(this);
        h.xPush = xPush;
    }

    default void setPushZ(double zPush) {
        net.minecraft.world.entity.vehicle.MinecartFurnace h = FusionCoreEntities.nms(this);
        h.zPush = zPush;
    }
}
