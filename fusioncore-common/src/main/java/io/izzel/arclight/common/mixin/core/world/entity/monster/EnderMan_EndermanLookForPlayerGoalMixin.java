package io.izzel.arclight.common.mixin.core.world.entity.monster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's EndermanEscapeEvent with reason STARE (patch 0184): a stared-at enderman teleports away only if allowed. */
@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanLookForPlayerGoal")
public abstract class EnderMan_EndermanLookForPlayerGoalMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/monster/EnderMan;teleport()Z"))
    private boolean fusioncore$stareEscape(EnderMan enderman, Operation<Boolean> original) {
        return FusionCorePaperMobEvents.endermanEscape(enderman, "STARE") && original.call(enderman);
    }
}
