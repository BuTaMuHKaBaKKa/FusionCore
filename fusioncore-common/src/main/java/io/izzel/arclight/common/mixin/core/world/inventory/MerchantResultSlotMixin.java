package io.izzel.arclight.common.mixin.core.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.mod.util.FusionCoreTrades;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.craftbukkit.v.inventory.CraftMerchantRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's PlayerTradeEvent / PlayerPurchaseEvent on taking a trade (Paper patch 0923), see FusionCoreTrades. */
@Mixin(MerchantResultSlot.class)
public abstract class MerchantResultSlotMixin {

    @Shadow @Final private MerchantContainer slots;
    @Shadow @Final private Merchant merchant;

    @Inject(method = "onTake", cancellable = true, at = @At("HEAD"))
    private void fusioncore$tradeEvent(Player player, ItemStack stack, CallbackInfo ci) {
        PlayerPurchaseEvent event = FusionCoreTrades.takePending();
        if (event == null && player instanceof ServerPlayer serverPlayer) {
            event = FusionCoreTrades.fire(serverPlayer, this.merchant, this.slots.getActiveOffer());
        }
        if (event != null && event.isCancelled()) {
            // as Paper: nothing paid, the taken result is gone, the client is resynced
            stack.setCount(0);
            ((ServerPlayerBridge) player).bridge$getBukkitEntity().updateInventory();
            FusionCoreTrades.setTaking(null);
            ci.cancel();
            return;
        }
        FusionCoreTrades.setTaking(event);
    }

    @ModifyExpressionValue(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantContainer;getActiveOffer()Lnet/minecraft/world/item/trading/MerchantOffer;"))
    private MerchantOffer fusioncore$tradeFromEvent(MerchantOffer offer) {
        PlayerPurchaseEvent event = FusionCoreTrades.taking();
        return event == null || offer == null ? offer : CraftMerchantRecipe.fromBukkit(event.getTrade()).toMinecraft();
    }

    @WrapOperation(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/Merchant;notifyTrade(Lnet/minecraft/world/item/trading/MerchantOffer;)V"))
    private void fusioncore$notifyWithEvent(Merchant merchant, MerchantOffer offer, Operation<Void> original) {
        FusionCoreTrades.notifying(merchant, offer, FusionCoreTrades.taking(), () -> original.call(merchant, offer));
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    private void fusioncore$tradeDone(Player player, ItemStack stack, CallbackInfo ci) {
        FusionCoreTrades.setTaking(null);
    }
}
