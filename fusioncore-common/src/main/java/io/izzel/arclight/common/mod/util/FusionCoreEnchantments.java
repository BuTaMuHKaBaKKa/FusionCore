package io.izzel.arclight.common.mod.util;

import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.enchantments.CraftEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.Set;
import java.util.stream.Collectors;

/** Fusion Core: bodies of Paper's enchantment API. Plugin-made enchantments throw {@link UnsupportedOperationException}. */
public final class FusionCoreEnchantments {

    private FusionCoreEnchantments() {
    }

    private static Holder<net.minecraft.world.item.enchantment.Enchantment> holder(Enchantment enchantment) {
        if (!(enchantment instanceof CraftEnchantment)) {
            throw new UnsupportedOperationException(enchantment.getKey() + " is not a server enchantment");
        }
        return CraftEnchantment.bukkitToMinecraftHolder(enchantment);
    }

    private static net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition definition(Enchantment enchantment) {
        return holder(enchantment).value().definition();
    }

    public static boolean isTreasure(Enchantment enchantment) {
        return holder(enchantment).is(EnchantmentTags.TREASURE);
    }

    public static boolean isTradeable(Enchantment enchantment) {
        return holder(enchantment).is(EnchantmentTags.TRADEABLE);
    }

    public static boolean isDiscoverable(Enchantment enchantment) {
        Holder<net.minecraft.world.item.enchantment.Enchantment> holder = holder(enchantment);
        return holder.is(EnchantmentTags.IN_ENCHANTING_TABLE)
            || holder.is(EnchantmentTags.ON_RANDOM_LOOT)
            || holder.is(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
            || holder.is(EnchantmentTags.TRADEABLE)
            || holder.is(EnchantmentTags.ON_TRADED_EQUIPMENT);
    }

    public static int minModifiedCost(Enchantment enchantment, int level) {
        return definition(enchantment).minCost().calculate(level);
    }

    public static int maxModifiedCost(Enchantment enchantment, int level) {
        return definition(enchantment).maxCost().calculate(level);
    }

    public static int anvilCost(Enchantment enchantment) {
        return definition(enchantment).anvilCost();
    }

    public static int weight(Enchantment enchantment) {
        return holder(enchantment).value().getWeight();
    }

    public static Set<EquipmentSlotGroup> activeSlotGroups(Enchantment enchantment) {
        return definition(enchantment).slots().stream().map(CraftEquipmentSlot::getSlot).collect(Collectors.toSet());
    }

    /** Paper's {@code getSupportedItems} (patch "Add even more Enchantment API"): the items the enchantment can go on. */
    public static io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.inventory.ItemType> supportedItems(Enchantment enchantment) {
        return io.papermc.paper.registry.set.PaperRegistrySets.convertToApi(io.papermc.paper.registry.RegistryKey.ITEM, holder(enchantment).value().getSupportedItems());
    }

    /** Paper's {@code getPrimaryItems}: the items the enchanting table offers it for, {@code null} when that is the supported items. */
    public static io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.inventory.ItemType> primaryItems(Enchantment enchantment) {
        return definition(enchantment).primaryItems()
            .map(holders -> io.papermc.paper.registry.set.PaperRegistrySets.convertToApi(io.papermc.paper.registry.RegistryKey.ITEM, holders))
            .orElse(null);
    }

    /** Paper's {@code getExclusiveWith}: the enchantments this one cannot be combined with. */
    public static io.papermc.paper.registry.set.RegistryKeySet<Enchantment> exclusiveWith(Enchantment enchantment) {
        return io.papermc.paper.registry.set.PaperRegistrySets.convertToApi(io.papermc.paper.registry.RegistryKey.ENCHANTMENT, holder(enchantment).value().exclusiveSet());
    }
}
