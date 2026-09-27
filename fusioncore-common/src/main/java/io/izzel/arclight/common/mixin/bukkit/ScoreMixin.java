package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's number format of a single score (Paper patch 0930 "add number format api"). */
@Mixin(value = org.bukkit.scoreboard.Score.class, remap = false)
public interface ScoreMixin {

    default io.papermc.paper.scoreboard.numbers.NumberFormat numberFormat() {
        return io.izzel.arclight.common.mod.util.FusionCoreScoreboards.numberFormat((org.bukkit.scoreboard.Score) this);
    }

    default void numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat format) {
        io.izzel.arclight.common.mod.util.FusionCoreScoreboards.numberFormat((org.bukkit.scoreboard.Score) this, format);
    }

    // Fusion Core: Paper's trigger lock, display name and single reset (patches 0592, 0912)

    default boolean isTriggerable() {
        return io.izzel.arclight.common.mod.util.FusionCoreScoreboards.triggerable((org.bukkit.scoreboard.Score) this);
    }

    default void setTriggerable(boolean triggerable) {
        io.izzel.arclight.common.mod.util.FusionCoreScoreboards.triggerable((org.bukkit.scoreboard.Score) this, triggerable);
    }

    default net.kyori.adventure.text.Component customName() {
        return io.izzel.arclight.common.mod.util.FusionCoreScoreboards.customName((org.bukkit.scoreboard.Score) this);
    }

    default void customName(net.kyori.adventure.text.Component customName) {
        io.izzel.arclight.common.mod.util.FusionCoreScoreboards.customName((org.bukkit.scoreboard.Score) this, customName);
    }

    default void resetScore() {
        io.izzel.arclight.common.mod.util.FusionCoreScoreboards.resetScore((org.bukkit.scoreboard.Score) this);
    }
}
