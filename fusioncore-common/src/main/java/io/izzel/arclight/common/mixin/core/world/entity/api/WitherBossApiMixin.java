package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.izzel.arclight.common.bridge.core.world.entity.api.WitherBossApiBridge;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's {@code Wither#setCanTravelThroughPortals} (patch 0557): vanilla withers never use portals. */
@Mixin(WitherBoss.class)
public abstract class WitherBossApiMixin implements WitherBossApiBridge {

    @Unique private boolean fusioncore$canPortal = false;

    @ModifyReturnValue(method = "canUsePortal", at = @At("RETURN"))
    private boolean fusioncore$canPortal(boolean original) {
        return this.fusioncore$canPortal;
    }

    @Override
    public void fusioncore$setCanTravelThroughPortals(boolean canPortal) {
        this.fusioncore$canPortal = canPortal;
    }
}
