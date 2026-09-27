package io.izzel.arclight.common.mixin.core.world.entity.ai.goal;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EatBlockGoal.class)
public class EatBlockGoalMixin {

    @Shadow @Final private Mob mob;

    private transient boolean fusioncore$grassCancelled;

    // Fusion Core: EntityChangeBlockEvent with Paper's new blocks (patch 0683): eating tall grass fired no event at all, and the grass
    // block turning into dirt was reported as air. A cancelled event keeps the block but the mob still "ate", as in Paper.
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean fusioncore$eatGrass(Level level, BlockPos pos, boolean drop, Operation<Boolean> original) {
        if (!CraftEventFactory.callEntityChangeBlockEvent(this.mob, pos, level.getFluidState(pos).createLegacyBlock())) {
            return false;
        }
        return original.call(level, pos, drop);
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private boolean fusioncore$eatGrassBlock(Level level, int type, BlockPos pos, int data) {
        this.fusioncore$grassCancelled = !CraftEventFactory.callEntityChangeBlockEvent(this.mob, pos, Blocks.DIRT.defaultBlockState());
        return !this.fusioncore$grassCancelled;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean fusioncore$grassToDirt(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        if (this.fusioncore$grassCancelled) {
            this.fusioncore$grassCancelled = false;
            return false;
        }
        return original.call(level, pos, state, flags);
    }
}
