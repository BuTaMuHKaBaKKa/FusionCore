package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEnchantments;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fusion Core: Paper's Adventure and enchantment API on {@link org.bukkit.enchantments.Enchantment}. The registry-set
 * getters are not implemented.
 */
@Mixin(value = org.bukkit.enchantments.Enchantment.class, remap = false)
public abstract class EnchantmentMixin {

    private Enchantment fusioncore$self() {
        return (Enchantment) (Object) this;
    }

    /** Paper: the enchantment's name with the level, coloured as vanilla shows it (curses red). */
    public net.kyori.adventure.text.Component displayName(int level) {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.enchantmentName((org.bukkit.enchantments.Enchantment) (Object) this, level);
    }

    public net.kyori.adventure.text.Component description() {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.enchantmentDescription((org.bukkit.enchantments.Enchantment) (Object) this);
    }

    /** Paper: whether villagers can trade the enchantment ({@code #minecraft:tradeable}). */
    public boolean isTradeable() {
        return FusionCoreEnchantments.isTradeable(this.fusioncore$self());
    }

    /** Paper: whether the enchantment can be found at all (table, loot, mob and trade equipment, trades). */
    public boolean isDiscoverable() {
        return FusionCoreEnchantments.isDiscoverable(this.fusioncore$self());
    }

    public int getMinModifiedCost(int level) {
        return FusionCoreEnchantments.minModifiedCost(this.fusioncore$self(), level);
    }

    public int getMaxModifiedCost(int level) {
        return FusionCoreEnchantments.maxModifiedCost(this.fusioncore$self(), level);
    }

    public int getAnvilCost() {
        return FusionCoreEnchantments.anvilCost(this.fusioncore$self());
    }

    public int getWeight() {
        return FusionCoreEnchantments.weight(this.fusioncore$self());
    }

    /** Paper: enchantments have had no rarity since 1.20.5; Paper throws as well. */
    @SuppressWarnings("removal")
    public io.papermc.paper.enchantments.EnchantmentRarity getRarity() {
        throw new UnsupportedOperationException("Enchantments don't have a rarity anymore in 1.20.5+.");
    }

    /** Paper throws as well: damage comes from effect maps since 1.21. */
    public float getDamageIncrease(int level, org.bukkit.entity.EntityCategory entityCategory) {
        throw new UnsupportedOperationException("Enchantments are based on complex effect maps since 1.21, cannot compute a simple damage increase");
    }

    public float getDamageIncrease(int level, org.bukkit.entity.EntityType entityType) {
        throw new UnsupportedOperationException("Enchantments are based on complex effect maps since 1.21, cannot compute a simple damage increase");
    }

    public Set<EquipmentSlotGroup> getActiveSlotGroups() {
        return FusionCoreEnchantments.activeSlotGroups(this.fusioncore$self());
    }

    /** Paper: the slots covered by {@link #getActiveSlotGroups()}. */
    public Set<EquipmentSlot> getActiveSlots() {
        final Set<EquipmentSlotGroup> slots = this.getActiveSlotGroups();
        return Arrays.stream(EquipmentSlot.values()).filter(e -> {
            for (final EquipmentSlotGroup group : slots) {
                if (group.test(e)) return true;
            }
            return false;
        }).collect(Collectors.toSet());
    }

    // Paper patch "Add even more Enchantment API": the vanilla holder sets as Paper's registry sets
    public io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.inventory.ItemType> getSupportedItems() {
        return FusionCoreEnchantments.supportedItems(this.fusioncore$self());
    }

    public io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.inventory.ItemType> getPrimaryItems() {
        return FusionCoreEnchantments.primaryItems(this.fusioncore$self());
    }

    public io.papermc.paper.registry.set.RegistryKeySet<Enchantment> getExclusiveWith() {
        return FusionCoreEnchantments.exclusiveWith(this.fusioncore$self());
    }
}
