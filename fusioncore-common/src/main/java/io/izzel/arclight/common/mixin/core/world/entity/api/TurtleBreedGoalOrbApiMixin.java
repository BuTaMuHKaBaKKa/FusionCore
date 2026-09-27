package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.mod.util.FusionCoreExperienceOrbs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import org.bukkit.entity.ExperienceOrb$SpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's orb reason for turtle breeding (patch 0125): {@code BREED}, triggered by the breeding player. */
@Mixin(targets = "net.minecraft.world.entity.animal.Turtle$TurtleBreedGoal")
public abstract class TurtleBreedGoalOrbApiMixin {

    @ModifyExpressionValue(method = "breed", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;DDDI)Lnet/minecraft/world/entity/ExperienceOrb;"))
    private ExperienceOrb fusioncore$breedOrb(ExperienceOrb orb, @Local ServerPlayer player) {
        return FusionCoreExperienceOrbs.tag(orb, ExperienceOrb$SpawnReason.BREED, player, null);
    }
}
