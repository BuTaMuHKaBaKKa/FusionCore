package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code MenuType#create(HumanEntity, Component)}; {@code CraftMenuTypeMixin} overrides it. */
@Mixin(value = MenuType.class, remap = false)
public interface MenuTypeMixin extends io.papermc.paper.world.flag.FeatureDependant { // FeatureDependant: Paper

    default InventoryView create(HumanEntity player, net.kyori.adventure.text.Component title) {
        return ((MenuType) this).typed().create(player, io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(title));
    }
}
