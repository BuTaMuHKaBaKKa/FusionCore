package io.izzel.arclight.common.mod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Iterator;

/** Fusion Core: experience set by plugins in block break events, handed to the loader's drop handling. */
public final class FusionCoreBlockExperience {

    public interface Source {
        int expDrop(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool);
    }

    private record Pending(ServerLevel level, BlockPos pos, int experience) {
    }

    private static Source source = (state, level, pos, blockEntity, breaker, tool) -> 0;
    private static final ArrayDeque<Pending> PENDING = new ArrayDeque<>();

    private FusionCoreBlockExperience() {
    }

    public static void setSource(Source source) {
        FusionCoreBlockExperience.source = source;
    }

    /** The experience the loader would drop for this block. */
    public static int defaultExp(BlockState state, ServerLevel level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
        return source.expDrop(state, level, pos, blockEntity, breaker, tool);
    }

    /** Remembers the experience an event decided for the drops of the block at {@code pos}. */
    public static void expect(ServerLevel level, BlockPos pos, int experience) {
        PENDING.push(new Pending(level, pos.immutable(), experience));
    }

    /** The experience decided for the drops at {@code pos}, or null when no event decided it; consumed. */
    public static @Nullable Integer take(ServerLevel level, BlockPos pos) {
        for (Iterator<Pending> it = PENDING.iterator(); it.hasNext(); ) {
            Pending pending = it.next();
            if (pending.level == level && pending.pos.equals(pos)) {
                it.remove();
                return pending.experience;
            }
        }
        return null;
    }

    /**
     * Ends the break of the block at {@code pos}: experience the loader did not take (the block was removed without its
     * drops, e.g. with the wrong tool) is dropped here when {@code dropLeftover}, as Spigot drops the event's value.
     */
    public static void finish(ServerLevel level, BlockPos pos, boolean dropLeftover, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.Entity breaker) {
        Integer experience = take(level, pos);
        if (dropLeftover && experience != null && experience > 0 && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            FusionCoreExperienceOrbs.award(level, Vec3.atCenterOf(pos), experience, org.bukkit.entity.ExperienceOrb$SpawnReason.BLOCK_BREAK, breaker, null);
        }
    }

    /** Drops everything remembered (a new break session starts). */
    public static void clear() {
        PENDING.clear();
    }
}
