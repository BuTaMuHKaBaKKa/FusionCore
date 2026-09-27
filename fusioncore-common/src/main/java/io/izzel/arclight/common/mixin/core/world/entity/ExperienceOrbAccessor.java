package io.izzel.arclight.common.mixin.core.world.entity;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: how many orbs one orb entity stands for, for Paper's merge rule (only orbs of the same count merge). */
@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {

    @Accessor("count")
    int fusioncore$getCount();
}
