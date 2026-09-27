package io.izzel.arclight.neoforge.mixin.compat.alternatecurrent;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/* Fusion Core: fire BlockRedstoneEvent from Alternate Current's WireNode#setPower, as Paper's port does. */
@Pseudo
@LoadIfMod(modid = "alternate_current", condition = LoadIfMod.ModCondition.PRESENT)
@Mixin(targets = "alternate.current.wire.WireNode", remap = false)
public abstract class WireNodeMixin {

    // @formatter:off
    @Shadow int currentPower;
    // @formatter:on

    @WrapOperation(method = "setPower", at = @At(value = "INVOKE", target = "Lalternate/current/wire/LevelHelper;setWireState(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z"))
    private boolean fusioncore$redstoneEvent(ServerLevel level, BlockPos pos, BlockState state, boolean updateNeighborShapes, Operation<Boolean> original) {
        int oldPower = level.getBlockState(pos).getValue(RedStoneWireBlock.POWER);
        BlockRedstoneEvent event = new BlockRedstoneEvent(CraftBlock.at(level, pos), oldPower, this.currentPower);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getNewCurrent() != this.currentPower) {
            this.currentPower = Math.max(0, Math.min(15, event.getNewCurrent()));
            state = state.setValue(RedStoneWireBlock.POWER, this.currentPower);
        }
        return original.call(level, pos, state, updateNeighborShapes);
    }
}
