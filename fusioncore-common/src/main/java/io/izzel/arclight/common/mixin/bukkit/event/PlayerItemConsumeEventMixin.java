package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.PlayerEventExtrasBridge;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's custom replacement for a consumed item (API patch 0030); the core puts it into the hand. */
@Mixin(value = PlayerItemConsumeEvent.class, remap = false)
public abstract class PlayerItemConsumeEventMixin implements PlayerEventExtrasBridge.ItemConsume {

    @Unique private ItemStack fusioncore$replacement;

    public ItemStack getReplacement() {
        return this.fusioncore$replacement;
    }

    public void setReplacement(ItemStack replacement) {
        this.fusioncore$replacement = replacement;
    }

    @Override
    public ItemStack bridge$getReplacement() {
        return this.fusioncore$replacement;
    }
}
