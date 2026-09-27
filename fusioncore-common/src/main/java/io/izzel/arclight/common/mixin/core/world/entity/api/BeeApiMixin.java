package io.izzel.arclight.common.mixin.core.world.entity.api;

import io.izzel.arclight.common.bridge.core.world.entity.api.BeeApiBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Fusion Core: Paper's bee rolling override (patch 0557): a set override wins over what the AI of the bee asks for. */
@Mixin(Bee.class)
public abstract class BeeApiMixin implements BeeApiBridge {

    @Shadow
    private boolean isRolling() {
        throw new AssertionError();
    }

    @Shadow
    private void setRolling(boolean isRolling) {
        throw new AssertionError();
    }

    @Unique private TriState fusioncore$rollingOverride = TriState.NOT_SET;

    @ModifyVariable(method = "setRolling", at = @At("HEAD"), argsOnly = true)
    private boolean fusioncore$rollingOverride(boolean rolling) {
        return this.fusioncore$rollingOverride.toBooleanOrElse(rolling);
    }

    @Override
    public TriState fusioncore$rollingOverride() {
        return this.fusioncore$rollingOverride;
    }

    @Override
    public void fusioncore$setRollingOverride(TriState rolling) {
        this.fusioncore$rollingOverride = rolling;
        this.setRolling(this.isRolling());
    }

    @Override
    public boolean fusioncore$isRolling() {
        return this.isRolling();
    }
}
