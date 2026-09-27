package io.izzel.arclight.common.mixin.core.world.entity.animal;

import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's null guard for a fish without a leader (patch 0557). */
@Mixin(AbstractSchoolingFish.class)
public abstract class AbstractSchoolingFishMixin {

    @Shadow @Nullable private AbstractSchoolingFish leader;

    @Inject(method = "stopFollowing", cancellable = true, at = @At("HEAD"))
    private void fusioncore$noLeader(CallbackInfo ci) {
        if (this.leader == null) {
            ci.cancel();
        }
    }
}
