package io.izzel.arclight.common.mixin.core.server.commands;

import io.izzel.arclight.common.bridge.core.world.entity.player.PlayerBridge;
import net.minecraft.server.commands.GiveCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fusion Core: /give drops without PlayerDropItemEvent, as CraftBukkit (SPIGOT-2942) and Paper. Both drops of the command
 * (what did not fit, and the display copy of what did) are not a player dropping an item; a plugin cancelling the event
 * put the item back into the inventory a second time.
 */
@Mixin(GiveCommand.class)
public class GiveCommandMixin {

    @Redirect(method = "giveItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private static ItemEntity fusioncore$dropWithoutEvent(ServerPlayer player, ItemStack stack, boolean traceItem) {
        return ((PlayerBridge) player).bridge$drop(stack, false, traceItem, false);
    }
}
