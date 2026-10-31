package io.izzel.arclight.common.mixin.core.world.level.chunk;

import io.izzel.arclight.common.mod.util.FusionCoreTickThrottles;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Fusion Core: failed natural spawn attempts per category, for optimization.throttle-mob-spawning (not saved); the array is
// made on the first counted attempt, so chunks cost nothing while the option is off
@Mixin(LevelChunk.class)
public abstract class LevelChunkSpawnThrottleMixin implements FusionCoreTickThrottles.ChunkState {

    @Unique private static final int FUSIONCORE$CATEGORIES = MobCategory.values().length;
    @Unique private long[] fusioncore$failedSpawnAttempts;

    @Override
    public long[] fusioncore$failedSpawnAttempts() {
        long[] failed = this.fusioncore$failedSpawnAttempts;
        if (failed == null) {
            this.fusioncore$failedSpawnAttempts = failed = new long[FUSIONCORE$CATEGORIES];
        }
        return failed;
    }
}
