package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's Adventure API on {@link org.bukkit.scoreboard.Objective} (Paper patch 0006/0010 "Adventure") and its
 * number format (Paper patch 0930 "add number format api").
 */
@Mixin(value = org.bukkit.scoreboard.Objective.class, remap = false)
public interface ObjectiveMixin {

    default net.kyori.adventure.text.Component displayName() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(((org.bukkit.scoreboard.Objective) this).getDisplayName());
    }

    default void displayName(net.kyori.adventure.text.Component displayName) {
        ((org.bukkit.scoreboard.Objective) this).setDisplayName(displayName == null ? "" : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(displayName));
    }

    default io.papermc.paper.scoreboard.numbers.NumberFormat numberFormat() {
        return io.izzel.arclight.common.mod.util.FusionCoreScoreboards.numberFormat((org.bukkit.scoreboard.Objective) this);
    }

    default void numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat format) {
        io.izzel.arclight.common.mod.util.FusionCoreScoreboards.numberFormat((org.bukkit.scoreboard.Objective) this, format);
    }

    // Fusion Core: Paper's entity scores (patch 0637) and auto-updated display (patch 0912)

    default org.bukkit.scoreboard.Score getScoreFor(org.bukkit.entity.Entity entity) {
        return ((org.bukkit.scoreboard.Objective) this).getScore(io.izzel.arclight.common.mod.util.FusionCoreScoreboards.entry(entity));
    }

    default boolean willAutoUpdateDisplay() {
        return io.izzel.arclight.common.mod.util.FusionCoreScoreboards.autoUpdateDisplay((org.bukkit.scoreboard.Objective) this);
    }

    default void setAutoUpdateDisplay(boolean autoUpdateDisplay) {
        io.izzel.arclight.common.mod.util.FusionCoreScoreboards.autoUpdateDisplay((org.bukkit.scoreboard.Objective) this, autoUpdateDisplay);
    }
}
