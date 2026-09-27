package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.configuration.ConfigurationSection} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.configuration.ConfigurationSection.class, remap = false)
public interface ConfigurationSectionMixin {

    default net.kyori.adventure.text.Component getRichMessage(String path) {
        return this.getRichMessage(path, null);
    }

    default net.kyori.adventure.text.Component getRichMessage(String path, net.kyori.adventure.text.Component fallback) {
        return this.getComponent(path, net.kyori.adventure.text.minimessage.MiniMessage.miniMessage(), fallback);
    }

    default void setRichMessage(String path, net.kyori.adventure.text.Component value) {
        this.setComponent(path, net.kyori.adventure.text.minimessage.MiniMessage.miniMessage(), value);
    }

    default <T extends net.kyori.adventure.text.Component> void setComponent(String path, net.kyori.adventure.text.serializer.ComponentEncoder<T, String> encoder, T value) {
        ((org.bukkit.configuration.ConfigurationSection) this).set(path, value == null ? null : encoder.serialize(value));
    }

    default <T extends net.kyori.adventure.text.Component> T getComponent(String path, net.kyori.adventure.text.serializer.ComponentDecoder<? super String, T> decoder) {
        return this.getComponent(path, decoder, null);
    }

    default <T extends net.kyori.adventure.text.Component> T getComponent(String path, net.kyori.adventure.text.serializer.ComponentDecoder<? super String, T> decoder, T fallback) {
        final String value = ((org.bukkit.configuration.ConfigurationSection) this).getString(path);
        return decoder.deserializeOr(value, fallback);
    }
}
