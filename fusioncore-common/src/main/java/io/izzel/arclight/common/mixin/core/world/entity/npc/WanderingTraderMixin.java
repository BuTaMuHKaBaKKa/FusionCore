package io.izzel.arclight.common.mixin.core.world.entity.npc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.entity.npc.WanderingTraderBridge;
import io.izzel.arclight.common.bridge.core.world.item.trading.MerchantOfferBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.inventory.CraftMerchantRecipe;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillagerMixin implements WanderingTraderBridge {

    // @formatter:off
    @Shadow abstract BlockPos getWanderTarget();
    // @formatter:on

    // Fusion Core start: Paper's WanderingTrader API (Paper patch 0523): may the trader drink its invisibility potion
    // at night and its milk by day. Not saved, as in Paper.
    @Unique private boolean fusioncore$canDrinkPotion = true;
    @Unique private boolean fusioncore$canDrinkMilk = true;

    @WrapOperation(method = "*(Lnet/minecraft/world/entity/npc/WanderingTrader;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isNight()Z"))
    private boolean fusioncore$canDrinkPotion(Level level, Operation<Boolean> original) {
        return this.fusioncore$canDrinkPotion && original.call(level);
    }

    @WrapOperation(method = "*(Lnet/minecraft/world/entity/npc/WanderingTrader;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    private boolean fusioncore$canDrinkMilk(Level level, Operation<Boolean> original) {
        return this.fusioncore$canDrinkMilk && original.call(level);
    }

    @Override
    public boolean bridge$canDrinkPotion() {
        return this.fusioncore$canDrinkPotion;
    }

    @Override
    public void bridge$setCanDrinkPotion(boolean canDrink) {
        this.fusioncore$canDrinkPotion = canDrink;
    }

    @Override
    public boolean bridge$canDrinkMilk() {
        return this.fusioncore$canDrinkMilk;
    }

    @Override
    public void bridge$setCanDrinkMilk(boolean canDrink) {
        this.fusioncore$canDrinkMilk = canDrink;
    }

    @Override
    public BlockPos bridge$getWanderTarget() {
        return this.getWanderTarget();
    }
    // Fusion Core end

    @Redirect(method = "updateTrades", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffers;add(Ljava/lang/Object;)Z"))
    private boolean arclight$gainOffer(MerchantOffers merchantOffers, Object e) {
        MerchantOffer offer = (MerchantOffer) e;
        VillagerAcquireTradeEvent event = new VillagerAcquireTradeEvent((AbstractVillager) getBukkitEntity(), ((MerchantOfferBridge) offer).bridge$asBukkit());
        if (this.valid) {
            Bukkit.getPluginManager().callEvent(event);
        }
        if (!event.isCancelled()) {
            return merchantOffers.add(CraftMerchantRecipe.fromBukkit(event.getRecipe()).toMinecraft());
        }
        return false;
    }

    @Inject(method = "maybeDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/WanderingTrader;discard()V"))
    private void arclight$despawn(CallbackInfo ci) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.DESPAWN);
    }
}
