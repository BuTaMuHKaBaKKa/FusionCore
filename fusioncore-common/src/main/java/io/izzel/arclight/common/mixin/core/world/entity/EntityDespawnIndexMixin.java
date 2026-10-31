package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.mod.util.FusionCoreDespawn;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fusion Core: a player that moves makes the optimize-mob-despawn snapshot of its level out of date (FusionCoreDespawn)
@Mixin(Entity.class)
public abstract class EntityDespawnIndexMixin {

    // @formatter:off
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();
    @Shadow public abstract Level level();
    // @formatter:on

    @Inject(method = "setPosRaw", at = @At("HEAD"))
    private void fusioncore$playerMoved(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer && (x != this.getX() || y != this.getY() || z != this.getZ())) {
            FusionCoreDespawn.invalidate(this.level());
        }
    }
}
