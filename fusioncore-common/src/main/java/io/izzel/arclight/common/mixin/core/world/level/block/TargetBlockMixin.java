package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's TargetHitEvent (patch 0448): a listener sets the signal strength; cancelled, the target neither
 * powers nor awards the hit statistic/advancement.
 */
@Mixin(TargetBlock.class)
public abstract class TargetBlockMixin {

    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "updateRedstoneOutput", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/TargetBlock;getRedstoneStrength(Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/phys/Vec3;)I"))
    private static int fusioncore$targetHit(int strength, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) LevelAccessor level,
                                          @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) BlockHitResult hit,
                                          @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) Entity entity) {
        return FusionCorePaperBlockEvents.targetHit(strength, level, hit, entity);
    }

    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "updateRedstoneOutput", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/TargetBlock;setOutputPower(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/block/state/BlockState;ILnet/minecraft/core/BlockPos;I)V"))
    private static boolean fusioncore$targetHitPower(LevelAccessor level, BlockState state, int power, BlockPos pos, int ticks) {
        return !FusionCorePaperBlockEvents.targetHitCancelled();
    }

    @Inject(method = "onProjectileHit", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/world/level/block/TargetBlock;updateRedstoneOutput(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/entity/Entity;)I"))
    private void fusioncore$targetHitAward(CallbackInfo ci) {
        if (FusionCorePaperBlockEvents.takeTargetHitCancelled()) {
            ci.cancel();
        }
    }
}
