package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code getMinecartMaterial} (patch 0452).
 */
@Mixin(value = org.bukkit.entity.Minecart.class, remap = false)
public interface MinecartMixin {

    default org.bukkit.Material getMinecartMaterial() {
        net.minecraft.world.entity.vehicle.AbstractMinecart h = FusionCoreEntities.nms(this);
        net.minecraft.world.item.Item item = switch (h.getMinecartType()) {
            case CHEST -> net.minecraft.world.item.Items.CHEST_MINECART;
            case FURNACE -> net.minecraft.world.item.Items.FURNACE_MINECART;
            case TNT -> net.minecraft.world.item.Items.TNT_MINECART;
            case HOPPER -> net.minecraft.world.item.Items.HOPPER_MINECART;
            case COMMAND_BLOCK -> net.minecraft.world.item.Items.COMMAND_BLOCK_MINECART;
            default -> net.minecraft.world.item.Items.MINECART; // RIDEABLE, SPAWNER, and types added by mods
        };
        return org.bukkit.craftbukkit.v.util.CraftMagicNumbers.getMaterial(item);
    }
}
