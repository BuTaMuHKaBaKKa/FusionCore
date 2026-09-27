package io.izzel.arclight.common.mixin.core.world.entity.monster.hoglin;

import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's EntityPushedByEntityAttackEvent (Paper patch 0202) for hoglin and zoglin throws. */
@Mixin(HoglinBase.class)
public interface HoglinBaseMixin {

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "throwTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;push(DDD)V"))
    private static void fusioncore$pushedBy(net.minecraft.world.entity.LivingEntity pushed, double x, double y, double z, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                          @com.llamalad7.mixinextras.sugar.Local(argsOnly = true, ordinal = 0) net.minecraft.world.entity.LivingEntity attacker) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.pushedBy(pushed, attacker, x, y, z, original);
    }
}
