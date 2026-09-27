package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.CraftEntitySchedulerBridge;
import io.izzel.arclight.common.mod.server.entity.EntityClassLookup;
import io.izzel.arclight.common.mod.server.scheduler.EntityTaskScheduler;
import io.izzel.arclight.common.mod.server.scheduler.FoliaEntityScheduler;
import io.izzel.arclight.common.mod.server.scheduler.PaperSchedulers;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftEntity.class, remap = false)
public abstract class CraftEntityMixin implements org.bukkit.entity.Entity, CraftEntitySchedulerBridge {

    @Shadow protected Entity entity;
    @Shadow @Final protected CraftServer server;

    // Fusion Core start: Paper's Folia entity scheduler (patch 0843), created on first use instead of with the CraftEntity
    private volatile EntityTaskScheduler fusioncore$taskScheduler;
    private volatile FoliaEntityScheduler fusioncore$apiScheduler;

    @Override
    public EntityScheduler fusioncore$apiScheduler() {
        FoliaEntityScheduler api = this.fusioncore$apiScheduler;
        if (api == null) {
            synchronized (this) {
                api = this.fusioncore$apiScheduler;
                if (api == null) {
                    this.fusioncore$taskScheduler = PaperSchedulers.createScheduler((CraftEntity) (Object) this);
                    this.fusioncore$apiScheduler = api = new FoliaEntityScheduler(this.fusioncore$taskScheduler);
                }
            }
        }
        return api;
    }

    @Override
    public EntityTaskScheduler fusioncore$taskSchedulerIfCreated() {
        return this.fusioncore$taskScheduler;
    }
    // Fusion Core end

    // Fusion Core start: Paper's teleport flags (Paper patch 0721), see FusionCoreTeleport
    private static final String FUSIONCORE$TELEPORT = "teleport(Lorg/bukkit/Location;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Z";

    @Inject(method = FUSIONCORE$TELEPORT, cancellable = true, at = @At("HEAD"))
    private void fusioncore$teleportFlags(org.bukkit.Location location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause, CallbackInfoReturnable<Boolean> cir,
                                        @com.llamalad7.mixinextras.sugar.Share("fusioncore$flags") com.llamalad7.mixinextras.sugar.ref.LocalRef<java.util.Set<io.papermc.paper.entity.TeleportFlag>> flags) {
        flags.set(io.izzel.arclight.common.mod.util.FusionCoreTeleport.take());
        if (io.izzel.arclight.common.mod.util.FusionCoreTeleport.refused(this.entity, this.getWorld(), location, flags.get())) {
            cir.setReturnValue(false);
        }
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = FUSIONCORE$TELEPORT, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isVehicle()Z"))
    private boolean fusioncore$retainPassengers(Entity handle, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original,
                                              @com.llamalad7.mixinextras.sugar.Share("fusioncore$flags") com.llamalad7.mixinextras.sugar.ref.LocalRef<java.util.Set<io.papermc.paper.entity.TeleportFlag>> flags) {
        return !flags.get().contains(io.papermc.paper.entity.TeleportFlag.EntityState.RETAIN_PASSENGERS) && original.call(handle);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = FUSIONCORE$TELEPORT, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;stopRiding()V"))
    private void fusioncore$retainVehicle(Entity handle, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                        @com.llamalad7.mixinextras.sugar.Share("fusioncore$flags") com.llamalad7.mixinextras.sugar.ref.LocalRef<java.util.Set<io.papermc.paper.entity.TeleportFlag>> flags) {
        if (!flags.get().contains(io.papermc.paper.entity.TeleportFlag.EntityState.RETAIN_VEHICLE)) {
            original.call(handle);
        }
    }
    // Fusion Core end

    // @Inject(method = "getEntity", cancellable = true, at = @At("HEAD"))
    // private static void arclight$fakePlayer(CraftServer server, Entity entity, CallbackInfoReturnable<CraftEntity> cir) {
    //     if (entity instanceof FakePlayer) {
    //         cir.setReturnValue(new ArclightFakePlayer(server, (FakePlayer) entity));
    //     }
    // }

    @Inject(method = "getEntity", cancellable = true, at = @At(value = "NEW", target = "java/lang/AssertionError"))
    private static void arclight$modEntity(CraftServer server, Entity entity, CallbackInfoReturnable<CraftEntity> cir) {
        var craftEntity = (CraftEntity) EntityClassLookup.getEntityTypeData(entity).convertFunction().apply(server, entity);
        cir.setReturnValue(craftEntity);
    }
}
