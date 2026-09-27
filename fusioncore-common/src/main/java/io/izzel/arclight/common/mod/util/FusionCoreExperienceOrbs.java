package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.world.entity.api.ExperienceOrbApiBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import org.bukkit.entity.ExperienceOrb$SpawnReason;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/** Fusion Core: Paper's orb spawn reasons (patch 0125), set through a context while spawning code runs. */
public final class FusionCoreExperienceOrbs {

    private record Context(ExperienceOrb$SpawnReason reason, @Nullable UUID trigger, @Nullable UUID source) {}

    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    private FusionCoreExperienceOrbs() {
    }

    /** Gives a freshly built orb the current context, if any. */
    public static void applyContext(ExperienceOrb orb) {
        Context context = CONTEXT.get();
        if (context != null) {
            ((ExperienceOrbApiBridge) orb).fusioncore$setSpawnData(context.reason(), context.trigger(), context.source());
        }
    }

    /** Whether orbs built now already get a reason from an outer context. */
    public static boolean hasContext() {
        return CONTEXT.get() != null;
    }

    public static ExperienceOrb tag(ExperienceOrb orb, ExperienceOrb$SpawnReason reason, @Nullable Entity trigger, @Nullable Entity source) {
        ((ExperienceOrbApiBridge) orb).fusioncore$setSpawnData(reason, uuid(trigger), uuid(source));
        return orb;
    }

    /** Paper's {@code ExperienceOrb.award(level, pos, amount, reason, trigger, source)}. */
    public static void award(ServerLevel level, Vec3 pos, int amount, ExperienceOrb$SpawnReason reason, @Nullable Entity trigger, @Nullable Entity source) {
        with(reason, trigger, source, () -> {
            ExperienceOrb.award(level, pos, amount);
            return null;
        });
    }

    /** Runs {@code action}; orbs it builds take {@code reason}, {@code trigger} and {@code source}. */
    public static <T> T with(ExperienceOrb$SpawnReason reason, @Nullable Entity trigger, @Nullable Entity source, Supplier<T> action) {
        Context previous = CONTEXT.get();
        CONTEXT.set(new Context(reason, uuid(trigger), uuid(source)));
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CONTEXT.remove();
            } else {
                CONTEXT.set(previous);
            }
        }
    }

    private static UUID uuid(@Nullable Entity entity) {
        return entity == null ? null : entity.getUUID();
    }
}
