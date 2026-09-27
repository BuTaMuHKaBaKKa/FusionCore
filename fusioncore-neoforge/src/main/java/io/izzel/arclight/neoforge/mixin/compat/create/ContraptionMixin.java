package io.izzel.arclight.neoforge.mixin.compat.create;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import io.izzel.arclight.neoforge.mod.machines.MachineOwners;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 * Fusion Core: every block a Create contraption takes is checked as its owner; a denied block fails the assembly.
 * On disassembly a block that may not be placed drops as an item.
 */
@Pseudo
@LoadIfMod(modid = "create", condition = LoadIfMod.ModCondition.PRESENT)
@Mixin(targets = "com.simibubi.create.content.contraptions.Contraption", remap = false)
public class ContraptionMixin {

    @Inject(method = "addBlock", at = @At("HEAD"))
    private void fusioncore$machineTake(Level level, BlockPos pos, org.apache.commons.lang3.tuple.Pair<?, ?> capture, CallbackInfo ci) {
        if (MachineOwners.hasContext() && !MachineOwners.mayRemove(level, pos)) {
            fusioncore$sneakyThrow(fusioncore$unmovable(pos, level.getBlockState(pos)));
        }
    }

    @WrapMethod(method = "removeBlocksFromWorld")
    private void fusioncore$checkedAsWhole(Level level, BlockPos offset, Operation<Void> original) {
        MachineOwners.suspend();
        try {
            original.call(level, offset);
        } finally {
            MachineOwners.resume();
        }
    }

    @WrapOperation(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean fusioncore$machinePut(Level level, BlockPos pos, BlockState state, int flags, Operation<Boolean> original) {
        if (!MachineOwners.checkSetBlockOnce(level, pos, state)) {
            Block.dropResources(state, level, pos, null);
            return false;
        }
        return original.call(level, pos, state, flags);
    }

    private static Throwable fusioncore$unmovable(BlockPos pos, BlockState state) {
        try {
            Class<?> type = Class.forName("com.simibubi.create.content.contraptions.AssemblyException");
            return (Throwable) type.getMethod("unmovableBlock", BlockPos.class, BlockState.class).invoke(null, pos, state);
        } catch (ReflectiveOperationException e) {
            return new IllegalStateException("Fusion Core: block at " + pos + " is protected", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void fusioncore$sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
