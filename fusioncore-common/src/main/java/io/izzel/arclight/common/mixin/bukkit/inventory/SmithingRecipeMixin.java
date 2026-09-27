package io.izzel.arclight.common.mixin.bukkit.inventory;

import io.izzel.arclight.common.bridge.core.world.item.crafting.SmithingCopyDataBridge;
import org.bukkit.inventory.SmithingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's option to prevent data component copy in smithing recipes (patch 0579). */
@Mixin(value = SmithingRecipe.class, remap = false)
public abstract class SmithingRecipeMixin implements SmithingCopyDataBridge {

    @Unique private boolean fusioncore$copyDataComponents = true;

    public boolean willCopyDataComponents() {
        return this.fusioncore$copyDataComponents;
    }

    @Deprecated
    public boolean willCopyNbt() {
        return this.willCopyDataComponents();
    }

    @Override
    public boolean fusioncore$copyDataComponents() {
        return this.fusioncore$copyDataComponents;
    }

    @Override
    public void fusioncore$setCopyDataComponents(boolean copyDataComponents) {
        this.fusioncore$copyDataComponents = copyDataComponents;
    }
}
