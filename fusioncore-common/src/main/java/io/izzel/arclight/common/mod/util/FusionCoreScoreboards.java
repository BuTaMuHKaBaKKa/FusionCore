package io.izzel.arclight.common.mod.util;

import com.google.common.base.Preconditions;
import io.papermc.paper.scoreboard.numbers.FixedFormat;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import io.papermc.paper.scoreboard.numbers.StyledFormat;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.craftbukkit.v.scoreboard.CraftScoreboard;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's number format API on objectives and scores (patch 0930). */
public final class FusionCoreScoreboards {

    private FusionCoreScoreboards() {
    }

    public static net.minecraft.network.chat.numbers.NumberFormat asVanilla(NumberFormat format) {
        if (format instanceof StyledFormat styled) {
            return new net.minecraft.network.chat.numbers.StyledFormat(FusionCoreAdventure.asVanilla(styled.style()));
        } else if (format instanceof FixedFormat fixed) {
            return new net.minecraft.network.chat.numbers.FixedFormat(FusionCoreAdventure.asVanilla(fixed.component()));
        } else if (format.equals(NumberFormat.blank())) {
            return BlankFormat.INSTANCE;
        }
        throw new IllegalArgumentException("Unknown format type " + format.getClass());
    }

    public static NumberFormat asPaper(net.minecraft.network.chat.numbers.NumberFormat vanilla) {
        if (vanilla instanceof net.minecraft.network.chat.numbers.StyledFormat styled) {
            return NumberFormat.styled(FusionCoreAdventure.asAdventure(((io.izzel.arclight.common.mixin.core.network.chat.numbers.StyledFormatAccessor) styled).fusioncore$style()));
        } else if (vanilla instanceof net.minecraft.network.chat.numbers.FixedFormat fixed) {
            return NumberFormat.fixed(FusionCoreAdventure.asAdventure(((io.izzel.arclight.common.mixin.core.network.chat.numbers.FixedFormatAccessor) fixed).fusioncore$value()));
        } else if (vanilla instanceof BlankFormat) {
            return NumberFormat.blank();
        }
        throw new IllegalArgumentException("Unknown format type " + vanilla.getClass());
    }

    private static Scoreboard board(org.bukkit.scoreboard.Objective objective) {
        return ((CraftScoreboard) objective.getScoreboard()).getHandle();
    }

    private static Objective handle(org.bukkit.scoreboard.Objective objective) {
        Objective handle = board(objective).getObjective(objective.getName());
        Preconditions.checkState(handle != null, "Unregistered scoreboard component");
        return handle;
    }

    public static @Nullable NumberFormat numberFormat(org.bukkit.scoreboard.Objective objective) {
        net.minecraft.network.chat.numbers.NumberFormat vanilla = handle(objective).numberFormat();
        return vanilla == null ? null : asPaper(vanilla);
    }

    public static void numberFormat(org.bukkit.scoreboard.Objective objective, @Nullable NumberFormat format) {
        handle(objective).setNumberFormat(format == null ? null : asVanilla(format));
    }

    public static @Nullable NumberFormat numberFormat(org.bukkit.scoreboard.Score score) {
        org.bukkit.scoreboard.Objective objective = score.getObjective();
        ReadOnlyScoreInfo info = board(objective).getPlayerScoreInfo(ScoreHolder.forNameOnly(score.getEntry()), handle(objective));
        if (info == null || info.numberFormat() == null) {
            return null;
        }
        return asPaper(info.numberFormat());
    }

    public static void numberFormat(org.bukkit.scoreboard.Score score, @Nullable NumberFormat format) {
        org.bukkit.scoreboard.Objective objective = score.getObjective();
        board(objective).getOrCreatePlayerScore(ScoreHolder.forNameOnly(score.getEntry()), handle(objective))
            .numberFormatOverride(format == null ? null : asVanilla(format));
    }

    // Fusion Core: Paper's entity entries, score state and team batches (patches 0592, 0637, 0649, 0912)

    /** The entry an entity has on scoreboards: a player's name, otherwise its UUID (vanilla's scoreboard name). */
    public static String entry(org.bukkit.entity.Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");
        return ((org.bukkit.craftbukkit.v.entity.CraftEntity) entity).getHandle().getScoreboardName();
    }

    public static boolean autoUpdateDisplay(org.bukkit.scoreboard.Objective objective) {
        return handle(objective).displayAutoUpdate();
    }

    public static void autoUpdateDisplay(org.bukkit.scoreboard.Objective objective, boolean autoUpdate) {
        handle(objective).setDisplayAutoUpdate(autoUpdate);
    }

    private static ReadOnlyScoreInfo info(org.bukkit.scoreboard.Score score) {
        org.bukkit.scoreboard.Objective objective = score.getObjective();
        return board(objective).getPlayerScoreInfo(ScoreHolder.forNameOnly(score.getEntry()), handle(objective));
    }

    private static net.minecraft.world.scores.ScoreAccess access(org.bukkit.scoreboard.Score score) {
        org.bukkit.scoreboard.Objective objective = score.getObjective();
        return board(objective).getOrCreatePlayerScore(ScoreHolder.forNameOnly(score.getEntry()), handle(objective));
    }

    /** Paper: a trigger score that the player may currently use with /trigger. */
    public static boolean triggerable(org.bukkit.scoreboard.Score score) {
        if (score.getObjective().getTrackedCriteria() != org.bukkit.scoreboard.Criteria.TRIGGER) {
            return false;
        }
        ReadOnlyScoreInfo info = info(score);
        return info != null && !info.isLocked();
    }

    public static void triggerable(org.bukkit.scoreboard.Score score, boolean triggerable) {
        Preconditions.checkArgument(score.getObjective().getTrackedCriteria() == org.bukkit.scoreboard.Criteria.TRIGGER, "the criteria isn't 'trigger'");
        if (triggerable) {
            access(score).unlock();
        } else {
            access(score).lock();
        }
    }

    /** Paper: the score's display name; null when the score is not set (it is not created). */
    public static net.kyori.adventure.text.@Nullable Component customName(org.bukkit.scoreboard.Score score) {
        if (info(score) == null) {
            return null;
        }
        net.minecraft.network.chat.Component display = access(score).display();
        return display == null ? null : FusionCoreAdventure.asAdventure(display);
    }

    public static void customName(org.bukkit.scoreboard.Score score, net.kyori.adventure.text.@Nullable Component name) {
        access(score).display(name == null ? null : FusionCoreAdventure.asVanilla(name));
    }

    /** Paper patch 0592: removes this one score, leaving the entry's other objectives alone. */
    public static void resetScore(org.bukkit.scoreboard.Score score) {
        org.bukkit.scoreboard.Objective objective = score.getObjective();
        board(objective).resetSinglePlayerScore(ScoreHolder.forNameOnly(score.getEntry()), handle(objective));
    }

    /**
     * Paper patch 0649: every entry added to the team. Paper sends one team packet for the batch; here each entry goes
     * through the team as {@code addEntry} does, with the same end state for clients.
     */
    public static void addEntries(org.bukkit.scoreboard.Team team, java.util.Collection<String> entries) {
        Preconditions.checkArgument(entries != null, "Entries cannot be null");
        for (String entry : entries) {
            team.addEntry(entry);
        }
    }

    /** Paper patch 0649: the entries of this team removed; true when at least one was in it. */
    public static boolean removeEntries(org.bukkit.scoreboard.Team team, java.util.Collection<String> entries) {
        Preconditions.checkArgument(entries != null, "Entry cannot be null");
        boolean removed = false;
        for (String entry : entries) {
            if (team.hasEntry(entry)) {
                removed |= team.removeEntry(entry);
            }
        }
        return removed;
    }

    public static java.util.List<String> entries(java.util.Collection<org.bukkit.entity.Entity> entities) {
        Preconditions.checkArgument(entities != null, "Entities cannot be null");
        return entities.stream().map(FusionCoreScoreboards::entry).toList();
    }

    /** Paper's {@code DisplaySlot#getId}: vanilla's slot id ({@code sidebar.team.<color>} for the team sidebars). */
    public static String slotId(org.bukkit.scoreboard.DisplaySlot slot) {
        return switch (slot) {
            case BELOW_NAME -> "below_name";
            case PLAYER_LIST -> "list";
            case SIDEBAR -> "sidebar";
            default -> "sidebar.team." + slot.name().substring("SIDEBAR_".length()).toLowerCase(java.util.Locale.ROOT);
        };
    }
    /** CraftBukkit's package-private {@code CraftScoreboardTranslations#toBukkitSlot}, by vanilla's slot id. */
    public static org.bukkit.scoreboard.DisplaySlot toBukkitSlot(net.minecraft.world.scores.DisplaySlot slot) {
        for (org.bukkit.scoreboard.DisplaySlot bukkit : org.bukkit.scoreboard.DisplaySlot.values()) {
            if (slotId(bukkit).equals(slot.getSerializedName())) {
                return bukkit;
            }
        }
        throw new IllegalArgumentException("Unknown display slot " + slot.getSerializedName());
    }
}
