package io.izzel.arclight.common.mixin.core.server.players;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: see {@link GameProfileCacheAccessor}. */
@Mixin(targets = "net.minecraft.server.players.GameProfileCache$GameProfileInfo")
public interface GameProfileInfoAccessor {

    @Invoker("getProfile")
    GameProfile fusioncore$getProfile();

    @Invoker("setLastAccess")
    void fusioncore$setLastAccess(long lastAccess);
}
