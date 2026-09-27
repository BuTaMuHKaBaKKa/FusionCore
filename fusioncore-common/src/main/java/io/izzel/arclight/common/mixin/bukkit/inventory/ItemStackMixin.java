package io.izzel.arclight.common.mixin.bukkit.inventory;

import io.izzel.arclight.common.mod.util.FusionCoreItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Fusion Core: Paper's {@link ItemStack} additions: hover source, {@code displayName()}, {@code lore()}, {@code editMeta}.
 */
@Mixin(value = ItemStack.class, remap = false)
public abstract class ItemStackMixin implements HoverEventSource<HoverEvent.ShowItem>, io.papermc.paper.persistence.PersistentDataViewHolder {

    @Shadow public abstract boolean hasItemMeta();

    @Shadow public abstract ItemMeta getItemMeta();

    @Shadow public abstract boolean setItemMeta(ItemMeta itemMeta);

    @Shadow public abstract org.bukkit.Material getType();

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowItem> asHoverEvent(@NotNull UnaryOperator<HoverEvent.ShowItem> op) {
        return FusionCoreItems.asHoverEvent((ItemStack) (Object) this, op);
    }

    public @NotNull Component displayName() {
        return FusionCoreItems.displayName((ItemStack) (Object) this);
    }

    public boolean editMeta(@NotNull Consumer<? super ItemMeta> consumer) {
        return this.editMeta(ItemMeta.class, consumer);
    }

    @SuppressWarnings("unchecked")
    public <M extends ItemMeta> boolean editMeta(@NotNull Class<M> metaClass, @NotNull Consumer<? super M> consumer) {
        final @Nullable ItemMeta meta = this.getItemMeta();
        if (metaClass.isInstance(meta)) {
            consumer.accept((M) meta);
            this.setItemMeta(meta);
            return true;
        }
        return false;
    }

    @Deprecated
    public @Nullable List<String> getLore() {
        if (!this.hasItemMeta()) {
            return null;
        }
        ItemMeta itemMeta = getItemMeta();
        if (!itemMeta.hasLore()) {
            return null;
        }
        return itemMeta.getLore();
    }

    public @Nullable List<Component> lore() {
        if (!this.hasItemMeta()) {
            return null;
        }
        final ItemMeta itemMeta = getItemMeta();
        if (!itemMeta.hasLore()) {
            return null;
        }
        return FusionCoreItems.lore(itemMeta);
    }

    @Deprecated
    public void setLore(@Nullable List<String> lore) {
        ItemMeta itemMeta = getItemMeta();
        if (itemMeta == null) {
            throw new IllegalStateException("Cannot set lore on " + getType());
        }
        itemMeta.setLore(lore);
        setItemMeta(itemMeta);
    }

    public void lore(@Nullable List<? extends Component> lore) {
        ItemMeta itemMeta = getItemMeta();
        if (itemMeta == null) {
            throw new IllegalStateException("Cannot set lore on " + getType());
        }
        FusionCoreItems.lore(itemMeta, lore);
        this.setItemMeta(itemMeta);
    }

    // Fusion Core: Paper's byte serialisation and small ItemStack helpers (Paper patches "Add API for
    // ItemStack serialization", "ItemStack#isEmpty", "PersistentDataContainerView")

    @Shadow public abstract int getAmount();

    /** Paper: the stack as compressed NBT with its data version (see {@code UnsafeValues#serializeItem}). */
    public byte[] serializeAsBytes() {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.serializeItem((ItemStack) (Object) this);
    }

    public boolean isEmpty() {
        return this.getType().isAir() || this.getAmount() <= 0;
    }

    /** Paper: a read-only view of the stack's persistent data (a copy, as meta is). */
    public io.papermc.paper.persistence.PersistentDataContainerView getPersistentDataContainer() {
        return io.izzel.arclight.common.mod.util.FusionCoreItems.persistentData((ItemStack) (Object) this);
    }

    // Fusion Core: Paper's ItemStack convenience API (Paper patches "ItemStack convenience methods", "ItemStack#getMaxItemUseDuration",
    // "ItemStack repair check API", "enchantWithLevels", "ItemStack Tooltip API", "Deprecate ItemStack#setType",
    // "Add ItemStack#damage"). Bodies are Paper's; what Paper delegates to CraftItemStack is in FusionCoreItems.

    @Shadow public abstract void setAmount(int amount);

    @Shadow public abstract int getMaxStackSize();

    @Shadow public abstract ItemStack clone();

    public @NotNull ItemStack withType(@NotNull org.bukkit.Material type) {
        return FusionCoreItems.withType((ItemStack) (Object) this, type);
    }

    public @NotNull ItemStack enchantWithLevels(int levels, boolean allowTreasure, @NotNull java.util.Random random) {
        return FusionCoreItems.enchantWithLevels((ItemStack) (Object) this, levels, allowTreasure, random);
    }

    public @NotNull ItemStack enchantWithLevels(int levels, @NotNull io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.enchantments.Enchantment> keySet, @NotNull java.util.Random random) {
        return FusionCoreItems.enchantWithLevels((ItemStack) (Object) this, levels, keySet, random);
    }

    public @NotNull ItemStack ensureServerConversions() {
        return FusionCoreItems.ensureServerConversions((ItemStack) (Object) this);
    }

    @Deprecated
    public @Nullable String getI18NDisplayName() {
        return FusionCoreItems.i18nDisplayName((ItemStack) (Object) this);
    }

    @Deprecated
    public int getMaxItemUseDuration() {
        return getMaxItemUseDuration(null);
    }

    public int getMaxItemUseDuration(@NotNull final org.bukkit.entity.LivingEntity entity) {
        return FusionCoreItems.maxItemUseDuration((ItemStack) (Object) this, entity);
    }

    public @NotNull ItemStack asOne() {
        return asQuantity(1);
    }

    public @NotNull ItemStack asQuantity(int qty) {
        ItemStack clone = clone();
        clone.setAmount(qty);
        return clone;
    }

    public @NotNull ItemStack add() {
        return add(1);
    }

    public @NotNull ItemStack add(int qty) {
        setAmount(Math.min(getMaxStackSize(), getAmount() + qty));
        return (ItemStack) (Object) this;
    }

    public @NotNull ItemStack subtract() {
        return subtract(1);
    }

    public @NotNull ItemStack subtract(int qty) {
        setAmount(Math.max(0, getAmount() - qty));
        return (ItemStack) (Object) this;
    }

    public void addItemFlags(@NotNull org.bukkit.inventory.ItemFlag... itemFlags) {
        ItemMeta itemMeta = getItemMeta();
        if (itemMeta == null) {
            throw new IllegalStateException("Cannot add flags on " + getType());
        }
        itemMeta.addItemFlags(itemFlags);
        setItemMeta(itemMeta);
    }

    public void removeItemFlags(@NotNull org.bukkit.inventory.ItemFlag... itemFlags) {
        ItemMeta itemMeta = getItemMeta();
        if (itemMeta == null) {
            throw new IllegalStateException("Cannot remove flags on " + getType());
        }
        itemMeta.removeItemFlags(itemFlags);
        setItemMeta(itemMeta);
    }

    public @NotNull java.util.Set<org.bukkit.inventory.ItemFlag> getItemFlags() {
        ItemMeta itemMeta = getItemMeta();
        if (itemMeta == null) {
            return java.util.Collections.emptySet();
        }
        return itemMeta.getItemFlags();
    }

    public boolean hasItemFlag(@NotNull org.bukkit.inventory.ItemFlag flag) {
        ItemMeta itemMeta = getItemMeta();
        return itemMeta != null && itemMeta.hasItemFlag(flag);
    }

    @Deprecated(forRemoval = true, since = "1.20.5")
    public @NotNull io.papermc.paper.inventory.ItemRarity getRarity() {
        return io.papermc.paper.inventory.ItemRarity.valueOf(this.getItemMeta().getRarity().name());
    }

    public boolean isRepairableBy(@NotNull ItemStack repairMaterial) {
        return FusionCoreItems.isValidRepairItemStack((ItemStack) (Object) this, repairMaterial);
    }

    public boolean canRepair(@NotNull ItemStack toBeRepaired) {
        return FusionCoreItems.isValidRepairItemStack(toBeRepaired, (ItemStack) (Object) this);
    }

    public @NotNull ItemStack damage(int amount, @NotNull org.bukkit.entity.LivingEntity livingEntity) {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.damageItemStack(livingEntity, (ItemStack) (Object) this, amount);
    }

    public @NotNull List<Component> computeTooltipLines(final @NotNull io.papermc.paper.inventory.tooltip.TooltipContext tooltipContext, final @Nullable org.bukkit.entity.Player player) {
        return FusionCoreItems.computeTooltipLines((ItemStack) (Object) this, tooltipContext, player);
    }
}
