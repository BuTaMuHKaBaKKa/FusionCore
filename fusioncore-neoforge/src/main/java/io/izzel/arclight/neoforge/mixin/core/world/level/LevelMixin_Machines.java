package io.izzel.arclight.neoforge.mixin.core.world.level;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.neoforge.mod.machines.MachineOwners;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

// Fusion Core: block changes of a mod's machine go through Bukkit's block events as its owner
@Mixin(Level.class)
public abstract class LevelMixin_Machines {

    @WrapMethod(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z")
    private boolean fusioncore$machineSetBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, Operation<Boolean> original) {
        Level level = (Level) (Object) this;
        if (!MachineOwners.hasContext()) {
            return original.call(pos, state, flags, recursionLeft);
        }
        if (!MachineOwners.checkSetBlock(level, pos, state)) {
            return false;
        }
        boolean result = original.call(pos, state, flags, recursionLeft);
        if (result && state.hasBlockEntity()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && MachineOwners.modOf(blockEntity) != null) {
                MachineOwners.inherit(blockEntity.getPersistentData());
            }
        }
        return result;
    }

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z", cancellable = true, at = @At("HEAD"))
    private void fusioncore$machineDestroy(BlockPos pos, boolean drop, Entity entity, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (MachineOwners.hasContext() && !MachineOwners.checkDestroy((Level) (Object) this, pos)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(method = "guardEntityTick", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private <T extends Entity> void fusioncore$machineEntityTick(Consumer<T> consumer, Object entity, Operation<Void> original) {
        if (!MachineOwners.enter((Entity) entity)) {
            original.call(consumer, entity);
            return;
        }
        try {
            original.call(consumer, entity);
        } finally {
            MachineOwners.exit();
        }
    }
}
