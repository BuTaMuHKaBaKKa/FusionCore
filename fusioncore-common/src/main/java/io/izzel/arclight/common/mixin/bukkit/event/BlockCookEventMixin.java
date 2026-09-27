package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.BlockCookEventBridge;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.inventory.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's recipe on {@link BlockCookEvent} (patch 0497). */
@Mixin(value = BlockCookEvent.class, remap = false)
public abstract class BlockCookEventMixin implements BlockCookEventBridge {

    @Unique private CookingRecipe<?> fusioncore$recipe;

    public CookingRecipe<?> getRecipe() {
        return this.fusioncore$recipe;
    }

    @Override
    public void fusioncore$setRecipe(CookingRecipe<?> recipe) {
        this.fusioncore$recipe = recipe;
    }
}
