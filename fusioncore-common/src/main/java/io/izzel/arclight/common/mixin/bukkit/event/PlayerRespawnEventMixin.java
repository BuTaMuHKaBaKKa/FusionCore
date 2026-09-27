package io.izzel.arclight.common.mixin.bukkit.event;

import com.google.common.collect.ImmutableSet;
import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/** Fusion Core: Paper's respawn flags (patch 0519). */
@Mixin(value = PlayerRespawnEvent.class, remap = false)
public abstract class PlayerRespawnEventMixin {

    @Shadow public abstract boolean isBedSpawn();

    @Shadow public abstract boolean isAnchorSpawn();

    private Set<org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag> fusioncore$flags;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/Location;ZZLorg/bukkit/event/player/PlayerRespawnEvent$RespawnReason;)V", at = @At("RETURN"))
    private void fusioncore$flags(CallbackInfo ci) {
        ImmutableSet.Builder<org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag> builder = FusionCoreEvents.takeRespawnFlags();
        if (this.isBedSpawn()) {
            builder.add(org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag.BED_SPAWN);
        }
        if (this.isAnchorSpawn()) {
            builder.add(org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag.ANCHOR_SPAWN);
        }
        this.fusioncore$flags = builder.build();
    }

    public Set<org.bukkit.event.player.PlayerRespawnEvent$RespawnFlag> getRespawnFlags() {
        return this.fusioncore$flags;
    }
}
