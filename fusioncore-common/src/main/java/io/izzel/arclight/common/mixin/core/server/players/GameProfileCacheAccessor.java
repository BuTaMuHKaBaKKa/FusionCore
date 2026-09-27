package io.izzel.arclight.common.mixin.core.server.players;

import net.minecraft.server.players.GameProfileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

/** Fusion Core: the cache lookup without a web request behind Paper's {@code getOfflinePlayerIfCached}. */
@Mixin(GameProfileCache.class)
public interface GameProfileCacheAccessor {

    /** Values are {@code GameProfileCache.GameProfileInfo}, see {@link GameProfileInfoAccessor}. */
    @Accessor("profilesByName")
    Map<String, Object> fusioncore$profilesByName();

    @Invoker("getNextOperation")
    long fusioncore$getNextOperation();
}
