package io.izzel.arclight.common.mixin.core.server.level;

import io.izzel.arclight.common.mod.util.FusionCoreAfk;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fusion Core: Purpur's AFK state; any action (resetLastActionTime) ends it
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerAfkMixin implements FusionCoreAfk.Afk {

    @Unique private boolean fusioncore$afk;

    @Override
    public boolean fusioncore$isAfk() {
        return this.fusioncore$afk;
    }

    @Override
    public void fusioncore$setAfk(boolean afk) {
        this.fusioncore$afk = afk;
    }

    @Inject(method = "resetLastActionTime", at = @At("HEAD"))
    private void fusioncore$back(CallbackInfo ci) {
        this.fusioncore$afk = false;
    }
}
