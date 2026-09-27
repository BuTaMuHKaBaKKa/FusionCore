package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.effect.MobEffectInstance;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Fusion Core: potion effects keep their icon flag and hidden effect (Paper patches 0356, 0526). */
@Mixin(value = CraftLivingEntity.class, remap = false)
public abstract class CraftLivingEntityMixin {

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "addPotionEffect(Lorg/bukkit/potion/PotionEffect;Z)Z",
        at = @At(value = "NEW", target = "(Lnet/minecraft/core/Holder;IIZZ)Lnet/minecraft/world/effect/MobEffectInstance;"))
    private MobEffectInstance fusioncore$addWithIcon(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> type, int duration, int amplifier,
                                                   boolean ambient, boolean particles,
                                                   com.llamalad7.mixinextras.injector.wrapoperation.Operation<MobEffectInstance> original,
                                                   PotionEffect effect, boolean force) {
        return new MobEffectInstance(type, duration, amplifier, ambient, particles, effect.hasIcon());
    }

    @Inject(method = "getPotionEffect", cancellable = true, at = @At("HEAD"))
    private void fusioncore$getPotionEffect(PotionEffectType type, CallbackInfoReturnable<PotionEffect> cir) {
        MobEffectInstance handle = ((CraftLivingEntity) (Object) this).getHandle().getEffect(CraftPotionEffectType.bukkitToMinecraftHolder(type));
        cir.setReturnValue(handle == null ? null : CraftPotionUtil.toBukkit(handle));
    }

    @Inject(method = "getActivePotionEffects", cancellable = true, at = @At("HEAD"))
    private void fusioncore$getActivePotionEffects(CallbackInfoReturnable<Collection<PotionEffect>> cir) {
        List<PotionEffect> effects = new ArrayList<>();
        for (MobEffectInstance handle : ((CraftLivingEntity) (Object) this).getHandle().getActiveEffects()) {
            effects.add(CraftPotionUtil.toBukkit(handle));
        }
        cir.setReturnValue(effects);
    }
}
