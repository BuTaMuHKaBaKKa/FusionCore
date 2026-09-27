package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.entity.api.VillagerApiBridge;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fusion Core: Paper's {@code Villager#updateTrades(int)} (patch 0764): vanilla's {@code updateTrades} with the number of
 * trades to add (vanilla adds 2) and whether the villager's level has any trades.
 */
@Mixin(Villager.class)
public abstract class VillagerApiMixin implements VillagerApiBridge {

    @Unique private int fusioncore$tradeAmount = -1;
    @Unique private boolean fusioncore$tradesAdded;

    @WrapOperation(method = "updateTrades", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;addOffersFromItemListings(Lnet/minecraft/world/item/trading/MerchantOffers;[Lnet/minecraft/world/entity/npc/VillagerTrades$ItemListing;I)V"))
    private void fusioncore$tradeAmount(Villager villager, MerchantOffers offers, VillagerTrades.ItemListing[] listings, int amount, Operation<Void> original) {
        // NeoForge's trade table has an entry, maybe empty, for every profession and level: an empty one is "no trades"
        this.fusioncore$tradesAdded |= listings.length > 0;
        original.call(villager, offers, listings, this.fusioncore$tradeAmount >= 0 ? this.fusioncore$tradeAmount : amount);
    }

    @Override
    public boolean fusioncore$updateTrades(int amount) {
        this.fusioncore$tradeAmount = amount;
        this.fusioncore$tradesAdded = false;
        try {
            ((AbstractVillagerApiInvoker) this).fusioncore$updateTrades();
            return this.fusioncore$tradesAdded;
        } finally {
            this.fusioncore$tradeAmount = -1;
        }
    }
}
