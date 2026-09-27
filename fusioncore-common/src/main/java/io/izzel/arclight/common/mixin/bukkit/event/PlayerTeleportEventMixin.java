package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreTeleport;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/** Fusion Core: Paper's relative teleport flags on {@link PlayerTeleportEvent}. */
@Mixin(value = PlayerTeleportEvent.class, remap = false)
public abstract class PlayerTeleportEventMixin {

    private Set<TeleportFlag.Relative> fusioncore$relative = Set.of();

    // every constructor, the Spigot 4-argument one and those of subclasses included, runs this one exactly once
    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/Location;Lorg/bukkit/Location;)V", at = @At("RETURN"))
    private void fusioncore$takeRelative(CallbackInfo ci) {
        this.fusioncore$relative = FusionCoreTeleport.takeEventRelative();
    }

    public Set<TeleportFlag.Relative> getRelativeTeleportationFlags() {
        return this.fusioncore$relative;
    }

    /** Paper: deprecated for removal, always {@code true}. */
    @Deprecated
    public boolean willDismountPlayer() {
        return true;
    }
}
