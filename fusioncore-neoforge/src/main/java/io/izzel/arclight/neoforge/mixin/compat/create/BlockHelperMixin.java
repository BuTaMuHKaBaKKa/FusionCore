package io.izzel.arclight.neoforge.mixin.compat.create;

import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import io.izzel.arclight.neoforge.mod.machines.MachineOwners;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/* Fusion Core: ask the machine owner before Create's drill, saw, harvester or plough breaks a block. */
@Pseudo
@LoadIfMod(modid = "create", condition = LoadIfMod.ModCondition.PRESENT)
@Mixin(targets = "com.simibubi.create.foundation.utility.BlockHelper", remap = false)
public class BlockHelperMixin {

    @Inject(method = "destroyBlockAs", cancellable = true, at = @At("HEAD"))
    private static void fusioncore$machineBreak(Level level, BlockPos pos, Player player, ItemStack usedTool, float effectChance,
                                              Consumer<ItemStack> droppedItemCallback, CallbackInfo ci) {
        if (player == null && MachineOwners.hasContext() && !MachineOwners.checkDestroy(level, pos)) {
            ci.cancel();
        }
    }
}
