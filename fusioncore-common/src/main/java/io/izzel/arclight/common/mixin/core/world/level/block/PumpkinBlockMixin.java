package io.izzel.arclight.common.mixin.core.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Fusion Core: Paper's PlayerShearBlockEvent (patch 0440) for carving a pumpkin: cancelled, nothing happens; otherwise each
 * drop listeners left pops where vanilla's seeds would (extra drops share the seeds' motion).
 */
@Mixin(PumpkinBlock.class)
public abstract class PumpkinBlockMixin {

    @Unique
    private static List<ItemStack> fusioncore$shearDrops;

    @Inject(method = "useItemOn", cancellable = true, at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void fusioncore$shearBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit,
                                     CallbackInfoReturnable<ItemInteractionResult> cir) {
        fusioncore$shearDrops = null;
        List<ItemStack> drops = FusionCorePaperBlockEvents.shearBlock(player, level, pos, stack, hand, new ItemStack(Items.PUMPKIN_SEEDS, 4));
        if (drops == null) {
            cir.setReturnValue(ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION);
        } else if (drops != FusionCorePaperBlockEvents.NOT_FIRED) {
            fusioncore$shearDrops = drops;
        }
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean fusioncore$shearDrops(Level level, Entity entity, Operation<Boolean> original) {
        List<ItemStack> drops = fusioncore$shearDrops;
        fusioncore$shearDrops = null;
        if (drops == null || !(entity instanceof ItemEntity seeds)) {
            return original.call(level, entity);
        }
        boolean added = false;
        for (int i = 0; i < drops.size(); i++) {
            ItemEntity item;
            if (i == 0) {
                item = seeds;
                item.setItem(drops.get(0));
            } else {
                Vec3 motion = seeds.getDeltaMovement();
                item = new ItemEntity(level, seeds.getX(), seeds.getY(), seeds.getZ(), drops.get(i), motion.x, motion.y, motion.z);
            }
            added |= original.call(level, item);
        }
        return added;
    }
}
