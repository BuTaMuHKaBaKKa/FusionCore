package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.PlayerEventExtrasBridge;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's experience consumed by a mending step (API patch 0393, server 0815). Spigot's constructors give half the repair
 * amount, as Paper's deprecated one does. The durability-to-xp operation throws, as in Paper: enchantment effects compute it since 1.21.
 */
@Mixin(value = PlayerItemMendEvent.class, remap = false)
public abstract class PlayerItemMendEventMixin implements PlayerEventExtrasBridge.ItemMend {

    @Shadow public abstract int getRepairAmount();

    @Unique private int fusioncore$consumedExperience;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void fusioncore$init(CallbackInfo ci) {
        this.fusioncore$consumedExperience = this.getRepairAmount() / 2;
    }

    public int getConsumedExperience() {
        return this.fusioncore$consumedExperience;
    }

    public java.util.function.IntUnaryOperator getDurabilityToXpOperation() {
        throw new UnsupportedOperationException("Enchantments use effects to compute xp to durability since 1.21.");
    }

    public void setDurabilityToXpOperation(java.util.function.IntUnaryOperator durabilityToXpOp) {
        throw new UnsupportedOperationException("Enchantments use effects to compute xp to durability since 1.21.");
    }

    @Override
    public void bridge$setConsumedExperience(int consumedExperience) {
        this.fusioncore$consumedExperience = consumedExperience;
    }
}
