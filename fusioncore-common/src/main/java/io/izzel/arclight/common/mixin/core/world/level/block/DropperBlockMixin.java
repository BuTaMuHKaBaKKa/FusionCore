package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.bridge.core.world.IInventoryBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.Direction;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DropperBlock;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DropperBlock.class)
public abstract class DropperBlockMixin extends BlockMixin {

    @Decorate(method = "dispenseFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack arclight$moveItemEvent(Container from, Container to, ItemStack stack, Direction direction) throws Throwable {
        CraftItemStack craftItemStack = CraftItemStack.asCraftMirror(stack);
        Inventory destinationInventory;
        // Have to special case large chests as they work oddly
        if (to instanceof CompoundContainer) {
            destinationInventory = new CraftInventoryDoubleChest((CompoundContainer) to);
        } else {
            destinationInventory = ((IInventoryBridge) to).getOwnerInventory();
        }
        InventoryMoveItemEvent event = new InventoryMoveItemEvent(((IInventoryBridge) from).getOwner().getInventory(), craftItemStack, destinationInventory, true);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return (ItemStack) DecorationOps.cancel().invoke();
        }
        var result = (ItemStack) DecorationOps.callsite().invoke(from, to, CraftItemStack.asNMSCopy(event.getItem()), direction);
        if (result.isEmpty() && !event.getItem().equals(craftItemStack)) {
            result = stack.copyWithCount(1);
        }
        return result;
    }

    // Fusion Core: Paper's BlockPreDispenseEvent (Paper patch 0477): a cancelled slot counts as empty, nothing moves
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "dispenseFrom", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean fusioncore$preDispense(boolean empty, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.server.level.ServerLevel level,
                                         @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.core.BlockPos pos, @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) ItemStack stack,
                                         @com.llamalad7.mixinextras.sugar.Local(ordinal = 0) int slot) {
        return empty || !io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.preDispense(level, pos, stack, slot);
    }

    // Fusion Core: Paper's BlockFailedDispenseEvent (patch 0463): no click sound when a listener turns the effect off
    @org.spongepowered.asm.mixin.injection.Inject(method = "dispenseFrom", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private void fusioncore$failedDispense(net.minecraft.server.level.ServerLevel level, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos,
                                         org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (!io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.failedDispense(level, pos)) {
            ci.cancel();
        }
    }
}
