package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.inventory.AnvilMenuBridge;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryView;
import org.bukkit.inventory.view.AnvilView;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code AnvilView#bypassEnchantmentLevelRestriction} (patch 1066), kept on the anvil menu. */
@Mixin(value = AnvilView.class, remap = false)
public interface AnvilViewMixin {

    default boolean bypassesEnchantmentLevelRestriction() {
        return ((AnvilMenuBridge) ((CraftInventoryView<?, ?>) (Object) this).getHandle()).fusioncore$bypassEnchantmentLevelRestriction();
    }

    default void bypassEnchantmentLevelRestriction(boolean bypassEnchantmentLevelRestriction) {
        ((AnvilMenuBridge) ((CraftInventoryView<?, ?>) (Object) this).getHandle()).fusioncore$setBypassEnchantmentLevelRestriction(bypassEnchantmentLevelRestriction);
    }
}
