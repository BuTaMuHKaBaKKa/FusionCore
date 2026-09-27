package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: {@link io.papermc.paper.entity.CollarColorable} on wolves (patch 0695). */
@Mixin(value = org.bukkit.entity.Wolf.class, remap = false)
public interface WolfMixin extends io.papermc.paper.entity.CollarColorable {

    @Override
    default org.bukkit.DyeColor getCollarColor() {
        net.minecraft.world.entity.animal.Wolf h = FusionCoreEntities.nms(this);
        return org.bukkit.DyeColor.getByWoolData((byte) h.getCollarColor().getId());
    }
}
