package io.izzel.arclight.common.mixin.core.world.inventory;

import io.izzel.arclight.common.bridge.core.world.entity.player.PlayerBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryMerchant;
import org.bukkit.craftbukkit.v.inventory.view.CraftMerchantView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import io.izzel.arclight.common.bridge.core.world.entity.player.InventoryBridge;
import io.izzel.arclight.common.mod.util.FusionCoreTrades;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin extends AbstractContainerMenuMixin {

    @Shadow @Final private Merchant trader;
    @Shadow @Final private MerchantContainer tradeContainer;

    private CraftMerchantView bukkitEntity;
    private Inventory inventory;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/trading/Merchant;)V", at = @At("RETURN"))
    public void arclight$init(int id, Inventory inventory, Merchant merchant, CallbackInfo ci) {
        this.inventory = inventory;
    }

    /** Fusion Core: Paper's trade event on shift-click (patch 0923), fired before the result moves. */
    @Inject(method = "quickMoveStack", cancellable = true, at = @At("HEAD"))
    private void fusioncore$tradeBeforeMove(net.minecraft.world.entity.player.Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        FusionCoreTrades.clearPending();
        if (index != 2 || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        Slot slot = ((AbstractContainerMenu) (Object) this).getSlot(2);
        if (!slot.hasItem() || ((InventoryBridge) serverPlayer.getInventory()).bridge$canHold(slot.getItem()) <= 0) {
            return;
        }
        if (!FusionCoreTrades.firePending(serverPlayer, this.trader, this.tradeContainer.getActiveOffer())) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "quickMoveStack", at = @At("RETURN"))
    private void fusioncore$tradeMoved(net.minecraft.world.entity.player.Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        FusionCoreTrades.clearPending();
    }

    @Inject(method = "playTradeSound", cancellable = true, at = @At("HEAD"))
    public void arclight$returnIfFail(CallbackInfo ci) {
        if (!(this.trader instanceof Entity)) {
            ci.cancel();
        }
    }

    @Override
    public CraftMerchantView getBukkitView() {
        if (bukkitEntity == null) {
            bukkitEntity = new CraftMerchantView(((PlayerBridge) this.inventory.player).bridge$getBukkitEntity(), new CraftInventoryMerchant(this.trader, this.tradeContainer), (MerchantMenu) (Object) this, trader);
        }
        return bukkitEntity;
    }
}
