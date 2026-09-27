package io.izzel.arclight.neoforge.mixin.neoforge;

import io.izzel.arclight.common.mod.server.event.EntityEventHandler;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CommonHooks.class)
public abstract class CommonHooksMixin {

    @Inject(method = "onPlaceItemIntoWorld", remap = false, at = @At("HEAD"))
    private static void arclight$captureHand(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ArclightCaptures.capturePlaceEventHand(context.getHand());
    }

    @Inject(method = "onPlaceItemIntoWorld", remap = false, at = @At("RETURN"))
    private static void arclight$removeHand(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ArclightCaptures.getPlaceEventHand(InteractionHand.MAIN_HAND);
    }

    @Decorate(method = "onLivingDrops", remap = false, at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/living/LivingDropsEvent;isCanceled()Z"))
    private static boolean arclight$monitorLivingDrops(LivingDropsEvent instance) throws Throwable {
        instance.setCanceled(EntityEventHandler.monitorLivingDrops(instance.getEntity(), instance.getSource(), (List<ItemEntity>) instance.getDrops(), instance.isCanceled()));
        return (boolean) DecorationOps.callsite().invoke(instance);
    }

    // Fusion Core: Paper's orb reason for blocks broken with drops (patch 0125): BLOCK_BREAK, triggered by the breaker
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "handleBlockDrops", remap = false,
        at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/world/level/block/Block;popExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;I)V"))
    private static void fusioncore$blockBreakOrbs(net.minecraft.world.level.block.Block block, net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos pos, int amount,
                                                com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                                @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.world.entity.Entity breaker) {
        io.izzel.arclight.common.mod.util.FusionCoreExperienceOrbs.with(org.bukkit.entity.ExperienceOrb$SpawnReason.BLOCK_BREAK, breaker, null,
            () -> original.call(block, level, pos, amount));
    }
}
