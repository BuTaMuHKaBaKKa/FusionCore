package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.bridge.core.world.entity.MobBridge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.BeehiveBlock;
import org.bukkit.event.entity.EntityTargetEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeehiveBlock.class)
public class BeehiveBlockMixin {

    @Redirect(method = "angerNearbyBees", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Bee;setTarget(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void arclight$targetReason(Bee beeEntity, LivingEntity livingEntity) {
        ((MobBridge) beeEntity).bridge$pushGoalTargetReason(EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
        beeEntity.setTarget(livingEntity);
    }

    // Fusion Core: Paper's PlayerShearBlockEvent (patch 0440): cancelled, nothing happens; otherwise the drops listeners left fall
    @org.spongepowered.asm.mixin.Unique
    private static java.util.List<net.minecraft.world.item.ItemStack> fusioncore$shearDrops;

    @org.spongepowered.asm.mixin.injection.Inject(method = "useItemOn", cancellable = true, at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void fusioncore$shearBlock(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level,
                                     net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
                                     net.minecraft.world.phys.BlockHitResult hit, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.world.ItemInteractionResult> cir) {
        fusioncore$shearDrops = null;
        if (level.isClientSide) {
            return;
        }
        java.util.List<net.minecraft.world.item.ItemStack> drops = io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.shearBlock(player, level, pos, stack, hand,
            new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.HONEYCOMB, 3));
        if (drops == null) {
            cir.setReturnValue(net.minecraft.world.ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION);
        } else if (drops != io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.NOT_FIRED) {
            fusioncore$shearDrops = drops;
        }
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "useItemOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/BeehiveBlock;dropHoneycomb(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void fusioncore$shearDrops(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        java.util.List<net.minecraft.world.item.ItemStack> drops = fusioncore$shearDrops;
        fusioncore$shearDrops = null;
        if (drops == null) {
            original.call(level, pos);
            return;
        }
        for (net.minecraft.world.item.ItemStack drop : drops) {
            net.minecraft.world.level.block.Block.popResource(level, pos, drop);
        }
    }
}
