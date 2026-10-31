package io.izzel.arclight.common.mixin.core.world.entity.projectile;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.entity.player.InventoryBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.entity.CraftItem;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.projectile.AbstractArrow.class)
public abstract class AbstractArrowMixin extends ProjectileMixin {

    // @formatter:off
    @Shadow public boolean inGround;
    @Shadow public abstract boolean isNoPhysics();
    @Shadow public int shakeTime;
    @Shadow public net.minecraft.world.entity.projectile.AbstractArrow.Pickup pickup;
    @Shadow protected abstract ItemStack getPickupItem();
    @Shadow public ItemStack pickupItemStack;
    // @formatter:on

    // Fusion Core: Paper's critical damage (patch 0588): a critical arrow deals critical damage
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "onHitEntity", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/damagesource/DamageSources;arrow(Lnet/minecraft/world/entity/projectile/AbstractArrow;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;"))
    private net.minecraft.world.damagesource.DamageSource fusioncore$criticalArrow(net.minecraft.world.damagesource.DamageSource source) {
        if (((net.minecraft.world.entity.projectile.AbstractArrow) (Object) this).isCritArrow()) {
            ((io.izzel.arclight.common.bridge.core.world.damagesource.DamageSourceBridge) source).bridge$critical(true);
        }
        return source;
    }

    @Decorate(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"))
    private void arclight$fireShot(Entity entity, float seconds) throws Throwable {
        EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.bridge$getBukkitEntity(), seconds);
        Bukkit.getPluginManager().callEvent(combustEvent);
        if (!combustEvent.isCancelled()) {
            DecorationOps.callsite().invoke(entity, seconds);
        }
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;discard()V"))
    private void arclight$hit(CallbackInfo ci) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.HIT);
    }

    // Fusion Core: Spigot's arrow-despawn-rate and trident-despawn-rate instead of vanilla's 1200 ticks
    @org.spongepowered.asm.mixin.injection.ModifyConstant(method = "tickDespawn", constant = @org.spongepowered.asm.mixin.injection.Constant(intValue = 1200))
    private int fusioncore$despawnRate(int vanilla) {
        org.spigotmc.SpigotWorldConfig spigot = ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) this.level()).bridge$spigotConfig();
        return (Object) this instanceof net.minecraft.world.entity.projectile.ThrownTrident ? spigot.tridentDespawnRate : spigot.arrowDespawnRate;
    }

    @Inject(method = "tickDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;discard()V"))
    private void arclight$despawn(CallbackInfo ci) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.DESPAWN);
    }

    @Decorate(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;tryPickup(Lnet/minecraft/world/entity/player/Player;)Z"))
    private boolean arclight$pickupArrow(net.minecraft.world.entity.projectile.AbstractArrow instance, Player player) throws Throwable {
        ItemStack itemstack = this.getPickupItem();
        if (this.pickup == net.minecraft.world.entity.projectile.AbstractArrow.Pickup.ALLOWED && !itemstack.isEmpty() && ((InventoryBridge) player.getInventory()).bridge$canHold(itemstack) > 0) {
            ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemstack);
            PlayerPickupArrowEvent event = new PlayerPickupArrowEvent(((ServerPlayerBridge) player).bridge$getBukkitEntity(), new CraftItem(((CraftServer) Bukkit.getServer()), item), (AbstractArrow) this.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return (boolean) DecorationOps.cancel().invoke();
            }
            itemstack = item.getItem();
        }
        this.pickupItemStack = itemstack;
        var result = (boolean) DecorationOps.callsite().invoke(instance, player);
        if (result) {
            this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.PICKUP);
        }
        return result;
    }

    @Inject(method = "setOwner", at = @At("HEAD"))
    private void arclight$setShooter(Entity entityIn, CallbackInfo ci) {
        if (entityIn != null) {
            var craftEntity = entityIn.bridge$getBukkitEntity();
            if (craftEntity instanceof ProjectileSource p) {
                this.projectileSource = p;
                return;
            }
        }

        this.projectileSource = null;
    }

    // Fusion Core: Paper's EntityPushedByEntityAttackEvent (Paper patch 0202)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "doKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;push(DDD)V"))
    private void fusioncore$pushedBy(net.minecraft.world.entity.LivingEntity pushed, double x, double y, double z,
                                   com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.pushedBy(pushed, (net.minecraft.world.entity.Entity) (Object) this, x, y, z, original);
    }
}
