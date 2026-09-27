package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.CraftBlockType;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fusion Core: Paper's PlayerFlowerPotManipulateEvent (Paper patch 0455) when a player puts a plant into a pot or takes it
 * out; cancelled, nothing changes and the player's inventory is resent.
 */
@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockMixin {

    @Shadow protected abstract boolean isEmpty();

    @Inject(method = "useItemOn", cancellable = true, at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void fusioncore$placePlant(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit,
                                     CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!this.fusioncore$allowed(level, pos, player, CraftItemStack.asBukkitCopy(stack), true)) {
            cir.setReturnValue(ItemInteractionResult.CONSUME);
        }
    }

    @Inject(method = "useWithoutItem", cancellable = true, at = @At("HEAD"))
    private void fusioncore$takePlant(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!this.isEmpty() && !this.fusioncore$allowed(level, pos, player, new org.bukkit.inventory.ItemStack(CraftBlockType.minecraftToBukkit(((FlowerPotBlock) (Object) this).getPotted())), false)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    private boolean fusioncore$allowed(Level level, BlockPos pos, Player player, org.bukkit.inventory.ItemStack item, boolean placing) {
        if (!(player instanceof ServerPlayer serverPlayer) || PlayerFlowerPotManipulateEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return true;
        }
        PlayerFlowerPotManipulateEvent event = new PlayerFlowerPotManipulateEvent(((ServerPlayerBridge) serverPlayer).bridge$getBukkitEntity(), CraftBlock.at(level, pos), item, placing);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            player.containerMenu.sendAllDataToRemote(); // update the client
            return false;
        }
        return true;
    }
}
