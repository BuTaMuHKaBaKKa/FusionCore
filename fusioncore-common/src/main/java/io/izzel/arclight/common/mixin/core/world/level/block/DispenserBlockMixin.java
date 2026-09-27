package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public class DispenserBlockMixin {

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static boolean eventFired = false;

    @Inject(method = "dispenseFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/DispenserBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void arclight$reset(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
        eventFired = false;
    }

    // Fusion Core: Paper's BlockPreDispenseEvent (Paper patch 0477): cancelled, the item stays in its slot
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "dispenseFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private net.minecraft.world.item.ItemStack fusioncore$preDispense(net.minecraft.core.dispenser.DispenseItemBehavior behavior, net.minecraft.core.dispenser.BlockSource source,
                                                                    net.minecraft.world.item.ItemStack stack, com.llamalad7.mixinextras.injector.wrapoperation.Operation<net.minecraft.world.item.ItemStack> original,
                                                                    @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) int slot) {
        if (!io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.preDispense(source.level(), source.pos(), stack, slot)) {
            return stack;
        }
        return original.call(behavior, source, stack);
    }

    // Fusion Core: Paper's BlockFailedDispenseEvent (patch 0463): no click sound and no BLOCK_ACTIVATE game event when a listener turns the effect off
    @org.spongepowered.asm.mixin.injection.Inject(method = "dispenseFrom", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private void fusioncore$failedDispense(net.minecraft.server.level.ServerLevel level, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos,
                                         org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (!io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.failedDispense(level, pos)) {
            ci.cancel();
        }
    }
}
