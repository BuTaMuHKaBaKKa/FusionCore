package io.izzel.arclight.neoforge.mixin.core.world.level.chunk;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.neoforge.mod.machines.MachineOwners;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Fusion Core: while a mod's block entity ticks, it is the machine whose owner block changes are attributed to
@Mixin(targets = "net/minecraft/world/level/chunk/LevelChunk$BoundTickingBlockEntity")
public class LevelChunk_BoundTickingBlockEntityMixin_Machines {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void fusioncore$machineContext(BlockEntityTicker<BlockEntity> ticker, Level level, BlockPos pos, BlockState state, BlockEntity blockEntity, Operation<Void> original) {
        if (!MachineOwners.enter(blockEntity)) {
            original.call(ticker, level, pos, state, blockEntity);
            return;
        }
        try {
            original.call(ticker, level, pos, state, blockEntity);
        } finally {
            MachineOwners.exit();
        }
    }
}
