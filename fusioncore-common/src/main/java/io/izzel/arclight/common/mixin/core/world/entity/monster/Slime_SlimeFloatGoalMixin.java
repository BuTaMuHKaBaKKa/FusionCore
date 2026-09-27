package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's SlimeSwimEvent and {@code Slime#canWander} (Paper patch 0232). */
@Mixin(targets = "net.minecraft.world.entity.monster.Slime$SlimeFloatGoal")
public abstract class Slime_SlimeFloatGoalMixin {

    @Shadow @Final private Slime slime;

    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean fusioncore$swimEvent(boolean canUse) {
        return canUse && FusionCorePaperEntityEvents.slimeSwim(this.slime);
    }
}
