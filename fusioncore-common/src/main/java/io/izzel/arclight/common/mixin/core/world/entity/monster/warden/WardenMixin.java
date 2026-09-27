package io.izzel.arclight.common.mixin.core.world.entity.monster.warden;

import io.izzel.arclight.common.mod.util.ArclightCaptures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Warden.class)
public class WardenMixin {

    @org.spongepowered.asm.mixin.Shadow net.minecraft.world.entity.monster.warden.AngerManagement angerManagement;

    @Inject(method = "applyDarknessAround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectUtil;addEffectToPlayersAround(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;DLnet/minecraft/world/effect/MobEffectInstance;I)Ljava/util/List;"))
    private static void arclight$reason(ServerLevel p_219376_, Vec3 p_219377_, Entity p_219378_, int p_219379_, CallbackInfo ci) {
        ArclightCaptures.captureEffectCause(EntityPotionEffectEvent.Cause.WARDEN);
    }

    // Fusion Core: Paper's WardenAngerChangeEvent (patch 0709): cancelled, no anger (and no dig cooldown); otherwise the listener's new anger
    @org.spongepowered.asm.mixin.Unique
    private Integer fusioncore$angerAmount;

    @Inject(method = "increaseAngerAt(Lnet/minecraft/world/entity/Entity;IZ)V", cancellable = true, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/monster/warden/WardenAi;setDigCooldown(Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void fusioncore$angerChange(net.minecraft.world.entity.Entity target, int amount, boolean listening, CallbackInfo ci) {
        Integer result = io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.wardenAnger((net.minecraft.world.entity.Entity) (Object) this, target, this.angerManagement.getActiveAnger(target), amount);
        if (result == null) {
            ci.cancel();
        } else {
            this.fusioncore$angerAmount = result;
        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyArg(method = "increaseAngerAt(Lnet/minecraft/world/entity/Entity;IZ)V", index = 1, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/monster/warden/AngerManagement;increaseAnger(Lnet/minecraft/world/entity/Entity;I)I"))
    private int fusioncore$angerAmount(int amount) {
        Integer result = this.fusioncore$angerAmount;
        this.fusioncore$angerAmount = null;
        return result != null ? result : amount;
    }
}
