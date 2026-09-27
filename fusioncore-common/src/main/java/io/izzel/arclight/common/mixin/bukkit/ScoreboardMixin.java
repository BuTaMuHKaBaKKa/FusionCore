package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.scoreboard.Scoreboard} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.scoreboard.Scoreboard.class, remap = false)
public interface ScoreboardMixin {

    default org.bukkit.scoreboard.Objective registerNewObjective(String name, String criteria, net.kyori.adventure.text.Component displayName) {
        return ((org.bukkit.scoreboard.Scoreboard) this).registerNewObjective(name, criteria, io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(displayName));
    }

    default org.bukkit.scoreboard.Objective registerNewObjective(String name, String criteria, net.kyori.adventure.text.Component displayName, org.bukkit.scoreboard.RenderType renderType) {
        return ((org.bukkit.scoreboard.Scoreboard) this).registerNewObjective(name, criteria, io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(displayName), renderType);
    }

    default org.bukkit.scoreboard.Objective registerNewObjective(String name, org.bukkit.scoreboard.Criteria criteria, net.kyori.adventure.text.Component displayName) {
        return ((org.bukkit.scoreboard.Scoreboard) this).registerNewObjective(name, criteria, io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(displayName));
    }

    default org.bukkit.scoreboard.Objective registerNewObjective(String name, org.bukkit.scoreboard.Criteria criteria, net.kyori.adventure.text.Component displayName, org.bukkit.scoreboard.RenderType renderType) {
        return ((org.bukkit.scoreboard.Scoreboard) this).registerNewObjective(name, criteria, io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(displayName), renderType);
    }

    // Fusion Core: Paper's entity entries (patch 0637)

    default java.util.Set<org.bukkit.scoreboard.Score> getScoresFor(org.bukkit.entity.Entity entity) {
        return ((org.bukkit.scoreboard.Scoreboard) this).getScores(io.izzel.arclight.common.mod.util.FusionCoreScoreboards.entry(entity));
    }

    default void resetScoresFor(org.bukkit.entity.Entity entity) {
        ((org.bukkit.scoreboard.Scoreboard) this).resetScores(io.izzel.arclight.common.mod.util.FusionCoreScoreboards.entry(entity));
    }

    default org.bukkit.scoreboard.Team getEntityTeam(org.bukkit.entity.Entity entity) {
        return ((org.bukkit.scoreboard.Scoreboard) this).getEntryTeam(io.izzel.arclight.common.mod.util.FusionCoreScoreboards.entry(entity));
    }
}
