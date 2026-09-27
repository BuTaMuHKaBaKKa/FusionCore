package io.izzel.arclight.common.mixin.core.world.entity.ai.behavior.warden;

import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's EntityPushedByEntityAttackEvent (Paper patch 0202) for the warden's sonic boom (a lambda in tick). */
@Mixin(SonicBoom.class)
public abstract class SonicBoomMixin {

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "*(Lnet/minecraft/world/entity/monster/warden/Warden;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;push(DDD)V"))
    private static void fusioncore$pushedBy(net.minecraft.world.entity.LivingEntity pushed, double x, double y, double z, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                          @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.world.entity.monster.warden.Warden warden) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.pushedBy(pushed, warden, x, y, z, original);
    }
}
