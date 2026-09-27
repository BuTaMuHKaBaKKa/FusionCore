package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.UnaryOperator;

/** Fusion Core: Paper's Adventure methods on {@link ItemFactory} (Paper patch 0006 "Adventure"). */
@Mixin(value = ItemFactory.class, remap = false)
public interface ItemFactoryMixin {

    default HoverEvent<HoverEvent.ShowItem> asHoverEvent(ItemStack item, UnaryOperator<HoverEvent.ShowItem> op) {
        return FusionCoreItems.asHoverEvent(item, op);
    }

    default Component displayName(ItemStack itemStack) {
        return FusionCoreItems.displayName(itemStack);
    }

    // Fusion Core: Paper patches "ensureServerConversions API", "Implement getI18NDisplayName", "Implement enchantWithLevels API"

    default ItemStack ensureServerConversions(ItemStack item) {
        return FusionCoreItems.ensureServerConversions(item);
    }

    @Deprecated
    default String getI18NDisplayName(ItemStack item) {
        return FusionCoreItems.i18nDisplayName(item);
    }

    default ItemStack enchantWithLevels(ItemStack itemStack, int levels, boolean allowTreasure, java.util.Random random) {
        return FusionCoreItems.enchantWithLevels(itemStack, levels, allowTreasure, random);
    }

    default ItemStack enchantWithLevels(ItemStack itemStack, int levels, io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.enchantments.Enchantment> keySet, java.util.Random random) {
        return FusionCoreItems.enchantWithLevels(itemStack, levels, keySet, random);
    }

    // Fusion Core: Paper's BungeeCord hover contents (patch 0405 "Create HoverEvent from ItemStack Entity"), same bodies;
    // Paper refuses items too, BungeeCord's item hover has no data components
    @Deprecated
    default net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(ItemStack itemStack) {
        throw new UnsupportedOperationException("BungeeCord Chat API does not support data components");
    }

    @Deprecated
    default net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity entity) {
        String customName = entity.getCustomName();
        return this.hoverContentOf(entity, customName == null || customName.isBlank() ? null : new net.md_5.bungee.api.chat.TextComponent(customName));
    }

    @Deprecated
    default net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity entity, String customName) {
        return this.hoverContentOf(entity, customName == null || customName.isBlank() ? null : new net.md_5.bungee.api.chat.TextComponent(customName));
    }

    @Deprecated
    default net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity entity, net.md_5.bungee.api.chat.BaseComponent customName) {
        return new net.md_5.bungee.api.chat.hover.content.Entity(entity.getType().getKey().toString(), entity.getUniqueId().toString(), customName);
    }

    @Deprecated
    default net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity entity, net.md_5.bungee.api.chat.BaseComponent[] customName) {
        return new net.md_5.bungee.api.chat.hover.content.Entity(entity.getType().getKey().toString(), entity.getUniqueId().toString(), new net.md_5.bungee.api.chat.TextComponent(customName));
    }
}
