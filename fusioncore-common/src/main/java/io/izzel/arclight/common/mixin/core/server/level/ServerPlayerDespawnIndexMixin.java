package io.izzel.arclight.common.mixin.core.server.level;

import io.izzel.arclight.common.mod.util.FusionCoreDespawn;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fusion Core: a player that becomes or stops being a spectator changes who keeps mobs from despawning (FusionCoreDespawn)
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDespawnIndexMixin {

    @Inject(method = "setGameMode", at = @At("RETURN"))
    private void fusioncore$gameModeChanged(GameType gameMode, CallbackInfoReturnable<Boolean> cir) {
        FusionCoreDespawn.invalidate(((ServerPlayer) (Object) this).level());
    }
}
