package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.bridge.core.world.entity.monster.SlimeBridge;
import io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents;
import net.minecraft.world.entity.monster.Slime;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's SlimeChangeDirectionEvent and {@code Slime#canWander} (Paper patch 0232). */
@Mixin(targets = "net.minecraft.world.entity.monster.Slime$SlimeRandomDirectionGoal")
public abstract class Slime_SlimeRandomDirectionGoalMixin {

    @Shadow @Final private Slime slime;
    @Shadow private float chosenDegrees;

    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean fusioncore$canWander(boolean canUse) {
        return canUse && ((SlimeBridge) this.slime).bridge$canWander();
    }

    @Inject(method = "tick", cancellable = true, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER,
        target = "Lnet/minecraft/world/entity/monster/Slime$SlimeRandomDirectionGoal;chosenDegrees:F"))
    private void fusioncore$directionEvent(CallbackInfo ci) {
        Float yaw = FusionCorePaperEntityEvents.slimeDirection(this.slime, this.chosenDegrees);
        if (yaw == null) {
            ci.cancel();
        } else {
            this.chosenDegrees = yaw;
        }
    }
}
