package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.ItemMetaBridge;
import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import org.bukkit.craftbukkit.v.inventory.CraftMetaItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = CraftMetaItem.class, remap = false)
public abstract class CraftMetaItemMixin implements ItemMetaBridge {

    // @formatter:off
    @Shadow(remap = false) protected net.minecraft.core.component.DataComponentPatch.Builder unhandledTags;
    @Shadow(remap = false) private net.minecraft.network.chat.Component displayName;
    @Shadow(remap = false) private net.minecraft.network.chat.Component itemName;
    @Shadow(remap = false) private List<net.minecraft.network.chat.Component> lore;
    // @formatter:on

    // Fusion Core start: Paper's Adventure accessors (patch 0010 "Adventure", CraftMetaItem), see ItemMetaMixin

    public net.kyori.adventure.text.Component displayName() {
        return this.displayName == null ? null : FusionCoreAdventure.asAdventure(this.displayName);
    }

    public void displayName(net.kyori.adventure.text.Component displayName) {
        this.displayName = displayName == null ? null : FusionCoreAdventure.asVanilla(displayName);
    }

    public net.kyori.adventure.text.Component itemName() {
        return FusionCoreAdventure.asAdventure(this.itemName);
    }

    public void itemName(net.kyori.adventure.text.Component name) {
        this.itemName = FusionCoreAdventure.asVanilla(name);
    }

    public List<net.kyori.adventure.text.Component> lore() {
        return this.lore != null ? FusionCoreAdventure.asAdventure(this.lore) : null;
    }

    public void lore(List<? extends net.kyori.adventure.text.Component> lore) {
        this.lore = lore != null ? FusionCoreAdventure.asVanilla(lore) : null;
    }

    @Override
    public List<net.kyori.adventure.text.Component> fusioncore$lore() {
        return this.lore();
    }

    @Override
    public void fusioncore$lore(List<? extends net.kyori.adventure.text.Component> lore) {
        this.lore(lore);
    }
    // Fusion Core end

    // Fusion Core start: Paper's Damageable#hasDamageValue/resetDamage (patch 0960 "General ItemMeta fixes"). Spigot keeps
    // the damage as an int written only when above 0, so "has a value" is "damage > 0" and reset is "damage 0"
    @Shadow(remap = false) private int damage;
    @Shadow(remap = false) private java.util.Set<net.minecraft.core.component.DataComponentType<?>> removedTags;
    @Shadow(remap = false) public abstract boolean hasDamage();
    @Shadow(remap = false) public abstract boolean hasItemFlag(org.bukkit.inventory.ItemFlag flag);

    public boolean hasDamageValue() {
        return this.hasDamage();
    }

    public void resetDamage() {
        this.damage = 0;
    }
    // Fusion Core end

    // Fusion Core start: Paper's can-place-on/can-destroy API (patch 1037). Spigot keeps the minecraft:can_place_on and
    // minecraft:can_break components among the unhandled tags, so the API reads and writes them there: the item keeps
    // its component as is, and equals/hashCode/clone/serialisation already cover it. The tooltip flag of an existing
    // component is kept; a new one is shown unless HIDE_PLACED_ON / HIDE_DESTROYS is set, as in Paper.
    public java.util.Set<org.bukkit.Material> getCanDestroy() {
        return fusioncore$toMaterials(this.fusioncore$predicates(net.minecraft.core.component.DataComponents.CAN_BREAK));
    }

    public void setCanDestroy(java.util.Set<org.bukkit.Material> canDestroy) {
        com.google.common.base.Preconditions.checkArgument(canDestroy != null, "Cannot replace with null set!");
        this.fusioncore$setPredicates(net.minecraft.core.component.DataComponents.CAN_BREAK, org.bukkit.inventory.ItemFlag.HIDE_DESTROYS, fusioncore$fromMaterials(canDestroy));
    }

    public java.util.Set<org.bukkit.Material> getCanPlaceOn() {
        return fusioncore$toMaterials(this.fusioncore$predicates(net.minecraft.core.component.DataComponents.CAN_PLACE_ON));
    }

    public void setCanPlaceOn(java.util.Set<org.bukkit.Material> canPlaceOn) {
        com.google.common.base.Preconditions.checkArgument(canPlaceOn != null, "Cannot replace with null set!");
        this.fusioncore$setPredicates(net.minecraft.core.component.DataComponents.CAN_PLACE_ON, org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON, fusioncore$fromMaterials(canPlaceOn));
    }

    public java.util.Set<com.destroystokyo.paper.Namespaced> getDestroyableKeys() {
        return fusioncore$toNamespaced(this.fusioncore$predicates(net.minecraft.core.component.DataComponents.CAN_BREAK));
    }

    public void setDestroyableKeys(java.util.Collection<com.destroystokyo.paper.Namespaced> canDestroy) {
        com.google.common.base.Preconditions.checkArgument(canDestroy != null, "Cannot replace with null collection!");
        com.google.common.base.Preconditions.checkArgument(fusioncore$ofAcceptableType(canDestroy), "Can only use NamespacedKey or NamespacedTag objects!");
        this.fusioncore$setPredicates(net.minecraft.core.component.DataComponents.CAN_BREAK, org.bukkit.inventory.ItemFlag.HIDE_DESTROYS, fusioncore$fromNamespaced(canDestroy));
    }

    public java.util.Set<com.destroystokyo.paper.Namespaced> getPlaceableKeys() {
        return fusioncore$toNamespaced(this.fusioncore$predicates(net.minecraft.core.component.DataComponents.CAN_PLACE_ON));
    }

    public void setPlaceableKeys(java.util.Collection<com.destroystokyo.paper.Namespaced> canPlaceOn) {
        com.google.common.base.Preconditions.checkArgument(canPlaceOn != null, "Cannot replace with null collection!");
        com.google.common.base.Preconditions.checkArgument(fusioncore$ofAcceptableType(canPlaceOn), "Can only use NamespacedKey or NamespacedTag objects!");
        this.fusioncore$setPredicates(net.minecraft.core.component.DataComponents.CAN_PLACE_ON, org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON, fusioncore$fromNamespaced(canPlaceOn));
    }

    public boolean hasPlaceableKeys() {
        return this.fusioncore$component(net.minecraft.core.component.DataComponents.CAN_PLACE_ON) != null;
    }

    public boolean hasDestroyableKeys() {
        return this.fusioncore$component(net.minecraft.core.component.DataComponents.CAN_BREAK) != null;
    }

    @org.spongepowered.asm.mixin.Unique
    private net.minecraft.world.item.AdventureModePredicate fusioncore$component(net.minecraft.core.component.DataComponentType<net.minecraft.world.item.AdventureModePredicate> type) {
        java.util.Optional<?> value = this.unhandledTags.map.get(type);
        return value != null && value.isPresent() ? (net.minecraft.world.item.AdventureModePredicate) value.get() : null;
    }

    @org.spongepowered.asm.mixin.Unique
    private List<net.minecraft.advancements.critereon.BlockPredicate> fusioncore$predicates(net.minecraft.core.component.DataComponentType<net.minecraft.world.item.AdventureModePredicate> type) {
        net.minecraft.world.item.AdventureModePredicate component = this.fusioncore$component(type);
        return component == null ? List.of() : component.predicates;
    }

    @org.spongepowered.asm.mixin.Unique
    private void fusioncore$setPredicates(net.minecraft.core.component.DataComponentType<net.minecraft.world.item.AdventureModePredicate> type, org.bukkit.inventory.ItemFlag hideFlag,
                                        List<net.minecraft.advancements.critereon.BlockPredicate> predicates) {
        net.minecraft.world.item.AdventureModePredicate old = this.fusioncore$component(type);
        if (predicates.isEmpty()) {
            // Paper writes no component for an empty list
            this.unhandledTags.map.remove(type);
            return;
        }
        boolean show = old != null ? old.showInTooltip() : !this.hasItemFlag(hideFlag);
        this.unhandledTags.set(type, new net.minecraft.world.item.AdventureModePredicate(predicates, show));
        this.removedTags.remove(type);
    }

    @org.spongepowered.asm.mixin.Unique
    private static List<net.minecraft.advancements.critereon.BlockPredicate> fusioncore$fromMaterials(java.util.Collection<org.bukkit.Material> materials) {
        return materials.stream().map(m -> net.minecraft.advancements.critereon.BlockPredicate.Builder.block()
            .of(org.bukkit.craftbukkit.v.block.CraftBlockType.bukkitToMinecraft(m)).build()).toList();
    }

    @org.spongepowered.asm.mixin.Unique
    private static java.util.Set<org.bukkit.Material> fusioncore$toMaterials(List<net.minecraft.advancements.critereon.BlockPredicate> predicates) {
        return predicates.stream()
            .flatMap(p -> p.blocks().map(net.minecraft.core.HolderSet::stream).orElse(java.util.stream.Stream.empty()))
            .map(holder -> org.bukkit.craftbukkit.v.block.CraftBlockType.minecraftToBukkit(holder.value()))
            .collect(java.util.stream.Collectors.toSet());
    }

    @org.spongepowered.asm.mixin.Unique
    private static List<net.minecraft.advancements.critereon.BlockPredicate> fusioncore$fromNamespaced(java.util.Collection<com.destroystokyo.paper.Namespaced> namespaceds) {
        List<net.minecraft.advancements.critereon.BlockPredicate> predicates = new java.util.ArrayList<>();
        for (com.destroystokyo.paper.Namespaced namespaced : namespaceds) {
            // NamespacedKey is a Namespaced through NamespacedKeyMixin, which javac does not see
            if ((Object) namespaced instanceof org.bukkit.NamespacedKey key) {
                predicates.add(net.minecraft.advancements.critereon.BlockPredicate.Builder.block()
                    .of(org.bukkit.craftbukkit.v.block.CraftBlockType.bukkitToMinecraft(java.util.Objects.requireNonNull(org.bukkit.Registry.MATERIAL.get(key)))).build());
            } else if (namespaced instanceof com.destroystokyo.paper.NamespacedTag tag) {
                predicates.add(net.minecraft.advancements.critereon.BlockPredicate.Builder.block()
                    .of(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(tag.getNamespace(), tag.getKey()))).build());
            }
        }
        return predicates;
    }

    @org.spongepowered.asm.mixin.Unique
    private static java.util.Set<com.destroystokyo.paper.Namespaced> fusioncore$toNamespaced(List<net.minecraft.advancements.critereon.BlockPredicate> predicates) {
        java.util.Set<com.destroystokyo.paper.Namespaced> namespaceds = new java.util.HashSet<>();
        for (net.minecraft.advancements.critereon.BlockPredicate predicate : predicates) {
            if (predicate.blocks().isEmpty()) {
                continue;
            }
            net.minecraft.core.HolderSet<net.minecraft.world.level.block.Block> holders = predicate.blocks().get();
            if (holders instanceof net.minecraft.core.HolderSet.Named<net.minecraft.world.level.block.Block> named) {
                namespaceds.add(new com.destroystokyo.paper.NamespacedTag(named.key().location().getNamespace(), named.key().location().getPath()));
            } else {
                holders.forEach(h -> h.unwrapKey().ifPresent(key ->
                    namespaceds.add((com.destroystokyo.paper.Namespaced) (Object) new org.bukkit.NamespacedKey(key.location().getNamespace(), key.location().getPath()))));
            }
        }
        return namespaceds;
    }

    @org.spongepowered.asm.mixin.Unique
    private static boolean fusioncore$ofAcceptableType(java.util.Collection<com.destroystokyo.paper.Namespaced> resources) {
        for (com.destroystokyo.paper.Namespaced resource : resources) {
            if (!((Object) resource instanceof org.bukkit.NamespacedKey || resource instanceof com.destroystokyo.paper.NamespacedTag)) {
                return false;
            }
        }
        return true;
    }
    // Fusion Core end
}
