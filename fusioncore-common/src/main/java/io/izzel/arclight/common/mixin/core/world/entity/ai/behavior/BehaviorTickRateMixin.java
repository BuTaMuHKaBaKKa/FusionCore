package io.izzel.arclight.common.mixin.core.world.entity.ai.behavior;

import io.izzel.arclight.common.mod.util.FusionCoreWorldSettings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fusion Core: Paper's tick-rates.behavior, a behavior of that entity type starts again only that many ticks after it ended.
// Keys are the behavior's class name in lower case, as on Paper.
@Mixin(Behavior.class)
public abstract class BehaviorTickRateMixin {

    @Shadow private long endTimestamp;

    @Inject(method = "tryStart", cancellable = true, at = @At("HEAD"))
    private void fusioncore$tickRate(ServerLevel level, LivingEntity entity, long time, CallbackInfoReturnable<Boolean> cir) {
        int rate = FusionCoreWorldSettings.of(level).behaviorRate(entity.getType(), this);
        if (rate > -1 && time < this.endTimestamp + rate) {
            cir.setReturnValue(false);
        }
    }
}
