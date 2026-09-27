package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's {@code resetOffers} (patch 0419).
 */
@Mixin(value = org.bukkit.entity.AbstractVillager.class, remap = false)
public interface AbstractVillagerMixin {

    default void resetOffers() {
        net.minecraft.world.entity.npc.AbstractVillager h = FusionCoreEntities.nms(this);
        h.offers = new net.minecraft.world.item.trading.MerchantOffers();
        ((io.izzel.arclight.common.mixin.core.world.entity.api.AbstractVillagerApiInvoker) h).fusioncore$updateTrades();
    }
}
