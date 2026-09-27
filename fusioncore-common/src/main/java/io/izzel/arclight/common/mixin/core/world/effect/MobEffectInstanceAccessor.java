package io.izzel.arclight.common.mixin.core.world.effect;

import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: the hidden effect for Paper's {@code PotionEffect#getHiddenPotionEffect} (Paper patch 0526). */
@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {

    @Accessor("hiddenEffect")
    MobEffectInstance fusioncore$getHiddenEffect();
}
