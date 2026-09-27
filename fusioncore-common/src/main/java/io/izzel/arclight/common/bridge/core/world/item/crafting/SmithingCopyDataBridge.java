package io.izzel.arclight.common.bridge.core.world.item.crafting;

/**
 * Fusion Core: Paper's "copy data components" option of smithing recipes (patch 0579), on the vanilla recipes and on
 * Bukkit's {@code SmithingRecipe}.
 */
public interface SmithingCopyDataBridge {

    boolean fusioncore$copyDataComponents();

    void fusioncore$setCopyDataComponents(boolean copyDataComponents);
}
