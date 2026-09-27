package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.scoreboard.Team} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.scoreboard.Team.class, remap = false)
public interface TeamMixin {

    default net.kyori.adventure.text.Component displayName() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(((org.bukkit.scoreboard.Team) this).getDisplayName());
    }

    default void displayName(net.kyori.adventure.text.Component displayName) {
        ((org.bukkit.scoreboard.Team) this).setDisplayName(displayName == null ? "" : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(displayName));
    }

    default net.kyori.adventure.text.Component prefix() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(((org.bukkit.scoreboard.Team) this).getPrefix());
    }

    default void prefix(net.kyori.adventure.text.Component prefix) {
        ((org.bukkit.scoreboard.Team) this).setPrefix(prefix == null ? "" : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(prefix));
    }

    default net.kyori.adventure.text.Component suffix() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(((org.bukkit.scoreboard.Team) this).getSuffix());
    }

    default void suffix(net.kyori.adventure.text.Component suffix) {
        ((org.bukkit.scoreboard.Team) this).setSuffix(suffix == null ? "" : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(suffix));
    }

    /** Paper: the team colour; a team without one (RESET) has none, which Paper reports with an exception. */
    default net.kyori.adventure.text.format.TextColor color() {
        org.bukkit.ChatColor color = ((org.bukkit.scoreboard.Team) this).getColor();
        net.kyori.adventure.text.format.NamedTextColor named = color == null ? null
            : net.kyori.adventure.text.format.NamedTextColor.NAMES.value(color.name().toLowerCase(java.util.Locale.ROOT));
        if (named == null) {
            throw new IllegalStateException("Team colors must have hex values");
        }
        return named;
    }

    default void color(net.kyori.adventure.text.format.NamedTextColor color) {
        ((org.bukkit.scoreboard.Team) this).setColor(color == null ? org.bukkit.ChatColor.RESET
            : org.bukkit.ChatColor.valueOf(net.kyori.adventure.text.format.NamedTextColor.NAMES.key(color).toUpperCase(java.util.Locale.ROOT)));
    }

    /** Paper: whether the team has a colour (not reset or a formatting code). */
    default boolean hasColor() {
        return ((org.bukkit.scoreboard.Team) this).getColor().isColor();
    }

    // Fusion Core: Paper's entity entries and batches (patches 0637, 0649)

    default void addEntity(org.bukkit.entity.Entity entity) {
        ((org.bukkit.scoreboard.Team) this).addEntry(io.izzel.arclight.common.mod.util.FusionCoreScoreboards.entry(entity));
    }

    default boolean removeEntity(org.bukkit.entity.Entity entity) {
        return ((org.bukkit.scoreboard.Team) this).removeEntry(io.izzel.arclight.common.mod.util.FusionCoreScoreboards.entry(entity));
    }

    default boolean hasEntity(org.bukkit.entity.Entity entity) {
        return ((org.bukkit.scoreboard.Team) this).hasEntry(io.izzel.arclight.common.mod.util.FusionCoreScoreboards.entry(entity));
    }

    default void addEntities(org.bukkit.entity.Entity... entities) {
        this.addEntities(java.util.List.of(entities));
    }

    default void addEntities(java.util.Collection<org.bukkit.entity.Entity> entities) {
        io.izzel.arclight.common.mod.util.FusionCoreScoreboards.addEntries((org.bukkit.scoreboard.Team) this, io.izzel.arclight.common.mod.util.FusionCoreScoreboards.entries(entities));
    }

    default void addEntries(String... entries) {
        this.addEntries(java.util.List.of(entries));
    }

    default void addEntries(java.util.Collection<String> entries) {
        io.izzel.arclight.common.mod.util.FusionCoreScoreboards.addEntries((org.bukkit.scoreboard.Team) this, entries);
    }

    default boolean removeEntities(org.bukkit.entity.Entity... entities) {
        return this.removeEntities(java.util.List.of(entities));
    }

    default boolean removeEntities(java.util.Collection<org.bukkit.entity.Entity> entities) {
        return io.izzel.arclight.common.mod.util.FusionCoreScoreboards.removeEntries((org.bukkit.scoreboard.Team) this, io.izzel.arclight.common.mod.util.FusionCoreScoreboards.entries(entities));
    }

    default boolean removeEntries(String... entries) {
        return this.removeEntries(java.util.List.of(entries));
    }

    default boolean removeEntries(java.util.Collection<String> entries) {
        return io.izzel.arclight.common.mod.util.FusionCoreScoreboards.removeEntries((org.bukkit.scoreboard.Team) this, entries);
    }
}
