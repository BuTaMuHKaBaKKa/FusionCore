package io.izzel.arclight.common.mixin.core.world.entity.monster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import io.izzel.arclight.common.bridge.core.world.entity.monster.ZombieVillagerPaperBridge;
import io.izzel.arclight.mixin.Eject;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(net.minecraft.world.entity.monster.ZombieVillager.class)
public abstract class ZombieVillagerMixin extends ZombieMixin implements ZombieVillagerPaperBridge {

    private boolean fusioncore$silentConversion;

    @Override
    public void fusioncore$startConverting(UUID starter, int delay, boolean broadcastEntityEvent) {
        this.fusioncore$silentConversion = !broadcastEntityEvent;
        try {
            ((net.minecraft.world.entity.monster.ZombieVillager) (Object) this).startConverting(starter, delay);
        } finally {
            this.fusioncore$silentConversion = false;
        }
    }

    @WrapOperation(method = "startConverting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
    private void fusioncore$conversionEvent(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity, byte event, Operation<Void> original) {
        if (!this.fusioncore$silentConversion) {
            original.call(level, entity, event);
        }
    }

    @Inject(method = "startConverting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;removeEffect(Lnet/minecraft/core/Holder;)Z"))
    private void arclight$convert1(UUID conversionStarterIn, int conversionTimeIn, CallbackInfo ci) {
        this.persist = true;
        bridge$pushEffectCause(EntityPotionEffectEvent.Cause.CONVERSION);
    }

    @Inject(method = "startConverting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private void arclight$convert2(UUID conversionStarterIn, int conversionTimeIn, CallbackInfo ci) {
        bridge$pushEffectCause(EntityPotionEffectEvent.Cause.CONVERSION);
    }

    @Eject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;convertTo(Lnet/minecraft/world/entity/EntityType;Z)Lnet/minecraft/world/entity/Mob;"))
    private <T extends Mob> T arclight$cure(net.minecraft.world.entity.monster.ZombieVillager zombieVillagerEntity, EntityType<T> entityType, boolean flag, CallbackInfo ci) {
        T t = this.convertTo(entityType, flag, EntityTransformEvent.TransformReason.CURED, CreatureSpawnEvent.SpawnReason.CURED);
        if (t == null) {
            ((ZombieVillager) this.bridge$getBukkitEntity()).setConversionTime(-1);
            ci.cancel();
        } else {
            ((LivingEntityBridge) t).bridge$pushEffectCause(EntityPotionEffectEvent.Cause.CONVERSION);
        }
        return t;
    }

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;dropPreservedEquipment(Ljava/util/function/Predicate;)Ljava/util/Set;"))
    private void arclight$dropPre(ServerLevel world, CallbackInfo ci) {
        this.forceDrops = true;
    }

    @Inject(method = "finishConversion", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/monster/ZombieVillager;dropPreservedEquipment(Ljava/util/function/Predicate;)Ljava/util/Set;"))
    private void arclight$dropPost(ServerLevel world, CallbackInfo ci) {
        this.forceDrops = false;
    }
}
