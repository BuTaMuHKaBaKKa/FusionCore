package io.izzel.arclight.common.mixin.core.world.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: Paper's {@code Player.setPlayerProfile} replaces the player's game profile (Paper makes the field public, non-final). */
@Mixin(Player.class)
public interface PlayerGameProfileAccessor {

    @Mutable
    @Accessor("gameProfile")
    void fusioncore$setGameProfile(GameProfile gameProfile);
}
