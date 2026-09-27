package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.mixin.core.world.entity.animal.AnimalMixin;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin extends AnimalMixin {

    // @formatter:off
    @Shadow public abstract boolean isTame();
    // @formatter:on

    @Decorate(method = "maybeTeleportTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/TamableAnimal;moveTo(DDDFF)V"))
    private void arclight$teleportEvent(TamableAnimal instance, double x, double y, double z, float yaw, float pitch) throws Throwable {
        EntityTeleportEvent event = CraftEventFactory.callEntityTeleportEvent(instance, x, y, z);
        if (event.isCancelled()) {
            DecorationOps.cancel().invoke(false);
            return;
        }
        Location to = event.getTo();
        DecorationOps.callsite().invoke(instance, to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
    }

    // Fusion Core: Paper's "Expand EntityUnleashEvent" (patch 0481): a sitting pet pulled too far fires the unleash event
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "handleLeashAtDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/TamableAnimal;dropLeash(ZZ)V"))
    private void fusioncore$unleashAtDistance(TamableAnimal animal, boolean broadcast, boolean dropItem, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        var event = io.izzel.arclight.common.mod.util.FusionCoreLeash.call(animal, org.bukkit.event.entity.EntityUnleashEvent.UnleashReason.DISTANCE, dropItem);
        if (!io.izzel.arclight.common.mod.util.FusionCoreLeash.cancelled(event)) {
            original.call(animal, broadcast, io.izzel.arclight.common.mod.util.FusionCoreLeash.dropLeash(event));
        }
    }

    // Fusion Core: Paper's EntityToggleSitEvent (patch 0755), not for the pose loaded from NBT
    @org.spongepowered.asm.mixin.injection.Inject(method = "setInSittingPose", cancellable = true, at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void fusioncore$setInSittingPoseEvent(boolean sitting, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (true && !io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.toggleSit((net.minecraft.world.entity.Entity) (Object) this, sitting)) {
            ci.cancel();
        }
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "readAdditionalSaveData", at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE", target = "Lnet/minecraft/world/entity/TamableAnimal;setInSittingPose(Z)V"))
    private void fusioncore$sittingPoseFromNbt(net.minecraft.world.entity.TamableAnimal self, boolean sitting, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.withoutSitEvent(() -> original.call(self, sitting));
    }

    // Fusion Core: Paper's TameableDeathMessageEvent (patch 0682): cancelled, the owner gets no message
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "die", at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"))
    private void fusioncore$deathMessage(net.minecraft.world.entity.LivingEntity owner, net.minecraft.network.chat.Component message,
                                       com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        net.minecraft.network.chat.Component result = io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.tameableDeathMessage((net.minecraft.world.entity.Entity) (Object) this, message);
        if (result != null) {
            original.call(owner, result);
        }
    }
}
