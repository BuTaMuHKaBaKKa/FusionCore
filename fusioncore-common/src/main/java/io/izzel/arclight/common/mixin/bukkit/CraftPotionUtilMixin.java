package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import net.minecraft.world.effect.MobEffectInstance;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's {@code CraftPotionUtil} conversions keep the icon flag (patches 0356, 0526). */
@Mixin(value = CraftPotionUtil.class, remap = false)
public abstract class CraftPotionUtilMixin {

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "fromBukkit(Lorg/bukkit/potion/PotionEffect;)Lnet/minecraft/world/effect/MobEffectInstance;",
        at = @At(value = "NEW", target = "(Lnet/minecraft/core/Holder;IIZZ)Lnet/minecraft/world/effect/MobEffectInstance;"))
    private static MobEffectInstance fusioncore$fromBukkitIcon(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> type, int duration, int amplifier,
                                                             boolean ambient, boolean particles,
                                                             com.llamalad7.mixinextras.injector.wrapoperation.Operation<MobEffectInstance> original, PotionEffect effect) {
        return new MobEffectInstance(type, duration, amplifier, ambient, particles, effect.hasIcon());
    }

    @Inject(method = "toBukkit(Lnet/minecraft/world/effect/MobEffectInstance;)Lorg/bukkit/potion/PotionEffect;", cancellable = true, at = @At("HEAD"))
    private static void fusioncore$toBukkit(MobEffectInstance effect, CallbackInfoReturnable<PotionEffect> cir) {
        MobEffectInstance hidden = ((io.izzel.arclight.common.mixin.core.world.effect.MobEffectInstanceAccessor) effect).fusioncore$getHiddenEffect();
        cir.setReturnValue(FusionCoreEvents.newPotionEffect(CraftPotionEffectType.minecraftHolderToBukkit(effect.getEffect()), effect.getDuration(),
            effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon(), hidden == null ? null : CraftPotionUtil.toBukkit(hidden)));
    }
}
