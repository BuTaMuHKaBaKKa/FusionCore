package io.izzel.arclight.common.mixin.bukkit.event;

import com.google.common.base.Preconditions;
import io.izzel.arclight.common.bridge.bukkit.BrewingStartEventBridge;
import org.bukkit.event.block.BrewingStartEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's brewing times on {@link BrewingStartEvent} (patch 0491). */
@Mixin(value = BrewingStartEvent.class, remap = false)
public abstract class BrewingStartEventMixin implements BrewingStartEventBridge {

    // @formatter:off
    @Shadow public abstract int getTotalBrewTime();
    @Shadow public abstract void setTotalBrewTime(int brewTime);
    // @formatter:on

    @Unique private int fusioncore$recipeBrewTime = 400;

    @Override
    public int getRecipeBrewTime() {
        return this.fusioncore$recipeBrewTime;
    }

    public void setRecipeBrewTime(int recipeBrewTime) {
        Preconditions.checkArgument(recipeBrewTime > 0, "recipeBrewTime must be positive");
        this.fusioncore$recipeBrewTime = recipeBrewTime;
    }

    public int getBrewingTime() {
        return this.getTotalBrewTime();
    }

    public void setBrewingTime(int brewTime) {
        Preconditions.checkArgument(brewTime >= 0, "brewTime must be non-negative");
        this.setTotalBrewTime(brewTime);
    }
}
