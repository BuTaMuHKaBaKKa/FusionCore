package io.izzel.arclight.common.mixin.bukkit;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/** Fusion Core: Paper's Adventure accessors on {@link ItemMeta}; {@code CraftMetaItem} overrides them. */
@Mixin(value = ItemMeta.class, remap = false)
public interface ItemMetaMixin {

    default Component displayName() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void displayName(Component displayName) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default Component itemName() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void itemName(Component name) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default List<Component> lore() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void lore(List<? extends Component> lore) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    // Fusion Core: Paper's deprecated BungeeCord-component name and lore
    default net.md_5.bungee.api.chat.BaseComponent[] getDisplayNameComponent() {
        Component name = this.displayName();
        return name == null ? new net.md_5.bungee.api.chat.BaseComponent[0]
            : net.md_5.bungee.chat.ComponentSerializer.parse(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(name));
    }

    default void setDisplayNameComponent(net.md_5.bungee.api.chat.BaseComponent[] component) {
        this.displayName(component == null ? null
            : net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(net.md_5.bungee.chat.ComponentSerializer.toString(component)));
    }

    default List<net.md_5.bungee.api.chat.BaseComponent[]> getLoreComponents() {
        List<Component> lore = this.lore();
        if (lore == null) {
            return null;
        }
        List<net.md_5.bungee.api.chat.BaseComponent[]> out = new java.util.ArrayList<>();
        for (Component line : lore) {
            out.add(net.md_5.bungee.chat.ComponentSerializer.parse(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(line)));
        }
        return out;
    }

    default void setLoreComponents(List<net.md_5.bungee.api.chat.BaseComponent[]> lore) {
        if (lore == null) {
            this.lore(null);
            return;
        }
        List<Component> out = new java.util.ArrayList<>();
        for (net.md_5.bungee.api.chat.BaseComponent[] line : lore) {
            out.add(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(net.md_5.bungee.chat.ComponentSerializer.toString(line)));
        }
        this.lore(out);
    }

    // Fusion Core: Paper's can-place-on/can-destroy API (patch 1037), bodies in CraftMetaItemMixin
    @Deprecated
    default java.util.Set<org.bukkit.Material> getCanDestroy() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Deprecated
    default void setCanDestroy(java.util.Set<org.bukkit.Material> canDestroy) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Deprecated
    default java.util.Set<org.bukkit.Material> getCanPlaceOn() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    @Deprecated
    default void setCanPlaceOn(java.util.Set<org.bukkit.Material> canPlaceOn) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default java.util.Set<com.destroystokyo.paper.Namespaced> getDestroyableKeys() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void setDestroyableKeys(java.util.Collection<com.destroystokyo.paper.Namespaced> canDestroy) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default java.util.Set<com.destroystokyo.paper.Namespaced> getPlaceableKeys() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void setPlaceableKeys(java.util.Collection<com.destroystokyo.paper.Namespaced> canPlaceOn) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default boolean hasPlaceableKeys() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default boolean hasDestroyableKeys() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }
}
