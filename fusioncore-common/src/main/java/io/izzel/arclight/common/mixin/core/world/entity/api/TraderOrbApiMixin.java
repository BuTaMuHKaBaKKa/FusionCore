package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.mod.util.FusionCoreExperienceOrbs;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.bukkit.entity.ExperienceOrb$SpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's orb reason for trades (patch 0125): {@code VILLAGER_TRADE}, triggered by the trading player. */
@Mixin({Villager.class, WanderingTrader.class})
public abstract class TraderOrbApiMixin {

    @ModifyExpressionValue(method = "rewardTradeXp", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/Level;DDDI)Lnet/minecraft/world/entity/ExperienceOrb;"))
    private ExperienceOrb fusioncore$tradeOrb(ExperienceOrb orb) {
        AbstractVillager trader = (AbstractVillager) (Object) this;
        return FusionCoreExperienceOrbs.tag(orb, ExperienceOrb$SpawnReason.VILLAGER_TRADE, trader.getTradingPlayer(), trader);
    }
}
