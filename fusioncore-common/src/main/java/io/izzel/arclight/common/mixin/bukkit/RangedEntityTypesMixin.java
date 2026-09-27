package io.izzel.arclight.common.mixin.bukkit;

import com.destroystokyo.paper.entity.RangedEntity;
import io.izzel.arclight.common.mod.util.FusionCoreMobs;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: the Bukkit types Paper declares as {@link RangedEntity} (patch 0205). */
@Mixin(value = {
    org.bukkit.entity.AbstractSkeleton.class, org.bukkit.entity.Drowned.class, org.bukkit.entity.Illusioner.class,
    org.bukkit.entity.Llama.class, org.bukkit.entity.Piglin.class, org.bukkit.entity.Pillager.class,
    org.bukkit.entity.Snowman.class, org.bukkit.entity.Witch.class, org.bukkit.entity.Wither.class
}, remap = false)
public interface RangedEntityTypesMixin extends RangedEntity {

    @Override
    default void rangedAttack(org.bukkit.entity.LivingEntity target, float charge) {
        FusionCoreMobs.rangedAttack(this, target, charge);
    }

    @Override
    default void setChargingAttack(boolean raiseHands) {
        FusionCoreMobs.mob(this).setAggressive(raiseHands);
    }
}
