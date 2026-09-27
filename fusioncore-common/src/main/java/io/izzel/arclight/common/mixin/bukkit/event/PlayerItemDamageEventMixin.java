package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.PlayerEventExtrasBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's damage before Unbreaking (API patch 0344, server 0691). Spigot's constructor gives the damage itself, as
 * Paper's deprecated one does.
 */
@Mixin(value = PlayerItemDamageEvent.class, remap = false)
public abstract class PlayerItemDamageEventMixin implements PlayerEventExtrasBridge.ItemDamage {

    @Unique private int fusioncore$originalDamage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fusioncore$init(Player player, ItemStack what, int damage, CallbackInfo ci) {
        this.fusioncore$originalDamage = damage;
    }

    public int getOriginalDamage() {
        return this.fusioncore$originalDamage;
    }

    @Override
    public void bridge$setOriginalDamage(int originalDamage) {
        this.fusioncore$originalDamage = originalDamage;
    }
}
