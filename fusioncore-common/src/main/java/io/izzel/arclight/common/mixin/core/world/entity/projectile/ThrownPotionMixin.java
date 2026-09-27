package io.izzel.arclight.common.mixin.core.world.entity.projectile;

import io.izzel.arclight.common.bridge.bukkit.LingeringPotionSplashEventBridge;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import io.izzel.arclight.common.bridge.core.world.entity.projectile.ThrownPotionBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ThrownPotion.class)
public abstract class ThrownPotionMixin extends ThrowableItemProjectileMixin implements ThrownPotionBridge {

    // @formatter:off
    @Shadow protected abstract void applyWater();
    @Shadow protected abstract void makeAreaOfEffectCloud(PotionContents potionContents);
    @Shadow public abstract boolean isLingering();
    // @formatter:on

    @Unique private transient HitResult arclight$hitResult;
    /** Fusion Core: false when the splash event was cancelled, so no particles or sound (Paper patch 0554). */
    @Unique private transient boolean fusioncore$showParticles = true;

    // Fusion Core start: Paper's splash fixes (Paper patch 0554 "Fix potions splash events") and ThrownPotion#splash
    // (0674 "More Projectile API"). Splash events built without a hit (splash()) get a MISS result instead of null.

    @Inject(method = "onHit", at = @At("HEAD"))
    private void fusioncore$hitStart(HitResult hitResult, CallbackInfo ci) {
        this.arclight$hitResult = hitResult;
        this.fusioncore$showParticles = true;
    }

    @com.llamalad7.mixinextras.injector.v2.WrapWithCondition(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private boolean fusioncore$particles(net.minecraft.world.level.Level level, int type, BlockPos pos, int data) {
        return this.fusioncore$showParticles;
    }

    @Unique
    private HitResult fusioncore$hit() {
        return this.arclight$hitResult != null ? this.arclight$hitResult : BlockHitResult.miss(this.position(), Direction.UP, this.blockPosition());
    }

    /** Paper's water splash: WaterBottleSplashEvent (a PotionSplashEvent) decides who is hurt, extinguished, rehydrated. */
    @Inject(method = "applyWater", cancellable = true, at = @At("HEAD"))
    private void fusioncore$applyWater(CallbackInfo ci) {
        ci.cancel();
        AABB box = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, box, ThrownPotion.WATER_SENSITIVE_OR_ON_FIRE.or(Axolotl.class::isInstance));
        Map<org.bukkit.entity.LivingEntity, Double> affected = new HashMap<>();
        java.util.Set<org.bukkit.entity.LivingEntity> rehydrate = new java.util.HashSet<>();
        java.util.Set<org.bukkit.entity.LivingEntity> extinguish = new java.util.HashSet<>();
        for (LivingEntity living : list) {
            org.bukkit.entity.LivingEntity bukkit = ((LivingEntityBridge) living).bridge$getBukkitEntity();
            if (living instanceof Axolotl) {
                rehydrate.add(bukkit);
            }
            if (this.distanceToSqr(living) < 16.0) {
                if (living.isSensitiveToWater()) {
                    affected.put(bukkit, 1.0);
                }
                if (living.isOnFire() && living.isAlive()) {
                    extinguish.add(bukkit);
                }
            }
        }
        HitResult hit = this.fusioncore$hit();
        org.bukkit.block.Block hitBlock = null;
        org.bukkit.block.BlockFace hitFace = null;
        org.bukkit.entity.Entity hitEntity = null;
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            hitBlock = org.bukkit.craftbukkit.v.block.CraftBlock.at(this.level(), blockHit.getBlockPos());
            hitFace = org.bukkit.craftbukkit.v.block.CraftBlock.notchToBlockFace(blockHit.getDirection());
        } else if (hit.getType() == HitResult.Type.ENTITY) {
            hitEntity = ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) ((net.minecraft.world.phys.EntityHitResult) hit).getEntity()).bridge$getBukkitEntity();
        }
        io.papermc.paper.event.entity.WaterBottleSplashEvent event = new io.papermc.paper.event.entity.WaterBottleSplashEvent(
            (org.bukkit.entity.ThrownPotion) this.getBukkitEntity(), hitEntity, hitBlock, hitFace, affected, rehydrate, extinguish);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        this.fusioncore$showParticles = !event.isCancelled();
        if (!event.isCancelled()) {
            for (org.bukkit.entity.LivingEntity entity : event.getToDamage()) {
                ((CraftLivingEntity) entity).getHandle().hurt(this.damageSources().indirectMagic((ThrownPotion) (Object) this, this.getOwner()), 1.0F);
            }
            for (org.bukkit.entity.LivingEntity entity : event.getToExtinguish()) {
                ((CraftLivingEntity) entity).getHandle().extinguishFire();
            }
            for (org.bukkit.entity.LivingEntity entity : event.getToRehydrate()) {
                if (((CraftLivingEntity) entity).getHandle() instanceof Axolotl axolotl) {
                    axolotl.rehydrate();
                }
            }
        }
    }

    @Override
    public void bridge$splash() {
        if (this.level().isClientSide) {
            return;
        }
        this.arclight$hitResult = null;
        this.fusioncore$showParticles = true;
        PotionContents contents = ((ThrownPotion) (Object) this).getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (contents.is(Potions.WATER)) {
            this.applyWater();
        } else if (this.isLingering()) {
            this.makeAreaOfEffectCloud(contents);
        } else {
            this.applySplash(contents.getAllEffects(), null);
        }
        if (this.fusioncore$showParticles) {
            int type = contents.potion().isPresent() && contents.potion().get().value().hasInstantEffects() ? 2007 : 2002;
            this.level().levelEvent(type, this.blockPosition(), contents.getColor());
        }
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.HIT);
        this.discard();
    }
    // Fusion Core end

    @Redirect(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;hasEffects()Z"))
    private boolean arclight$callEvent(PotionContents instance, HitResult hitResult) {
        arclight$hitResult = hitResult;
        return true;
    }

    @Inject(method = "onHit", at = @At("RETURN"))
    private void arclight$resetResult(HitResult p_37543_, CallbackInfo ci) {
        arclight$hitResult = null;
    }

    @Inject(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownPotion;discard()V"))
    private void arclight$hitCause(HitResult hitResult, CallbackInfo ci) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.HIT);
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    private void applySplash(Iterable<MobEffectInstance> list, @Nullable Entity entity) {
        AABB axisalignedbb = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list2 = this.level().getEntitiesOfClass(LivingEntity.class, axisalignedbb);
        Map<org.bukkit.entity.LivingEntity, Double> affected = new HashMap<>();
        if (!list2.isEmpty()) {
            for (LivingEntity entityliving : list2) {
                if (entityliving.isAffectedByPotions()) {
                    double d0 = this.distanceToSqr(entityliving);
                    if (d0 >= 16.0) {
                        continue;
                    }
                    double d2 = 1.0 - Math.sqrt(d0) / 4.0;
                    if (entityliving == entity) {
                        d2 = 1.0;
                    }
                    affected.put(((LivingEntityBridge) entityliving).bridge$getBukkitEntity(), d2);
                }
            }
        }
        PotionSplashEvent event = CraftEventFactory.callPotionSplashEvent((ThrownPotion) (Object) this, this.fusioncore$hit(), affected);
        this.fusioncore$showParticles = !event.isCancelled(); // Fusion Core: Paper patch 0554
        if (!event.isCancelled()) {
            for (org.bukkit.entity.LivingEntity victim : event.getAffectedEntities()) {
                if (!(victim instanceof CraftLivingEntity)) {
                    continue;
                }
                LivingEntity entityliving2 = ((CraftLivingEntity) victim).getHandle();
                double d2 = event.getIntensity(victim);
                for (MobEffectInstance mobeffect : list) {
                    var holder = mobeffect.getEffect();
                    if (!((WorldBridge) this.level()).bridge$isPvpMode() && this.getOwner() instanceof ServerPlayer && entityliving2 instanceof ServerPlayer && entityliving2 != this.getOwner()) {
                        var mobeffectlist = holder.value();
                        if (mobeffectlist == MobEffects.MOVEMENT_SLOWDOWN || mobeffectlist == MobEffects.DIG_SLOWDOWN || mobeffectlist == MobEffects.HARM || mobeffectlist == MobEffects.BLINDNESS
                            || mobeffectlist == MobEffects.HUNGER || mobeffectlist == MobEffects.WEAKNESS || mobeffectlist == MobEffects.POISON) {
                            continue;
                        }
                    }
                    if (holder.value().isInstantenous()) {
                        holder.value().applyInstantenousEffect((ThrownPotion) (Object) this, this.getOwner(), entityliving2, mobeffect.getAmplifier(), d2);
                    } else {
                        int i = (int) (d2 * mobeffect.getDuration() + 0.5);
                        if (i <= 20) {
                            continue;
                        }
                        ((LivingEntityBridge) entityliving2).bridge$pushEffectCause(EntityPotionEffectEvent.Cause.POTION_SPLASH);
                        // Fusion Core: keep vanilla's source-aware overload: mods such as Passive Skill Tree
                        // key their handling off the effect source.
                        entityliving2.addEffect(new MobEffectInstance(holder, i, mobeffect.getAmplifier(), mobeffect.isAmbient(), mobeffect.isVisible()),
                            ((ThrownPotion) (Object) this).getEffectSource());
                    }
                }
            }
        }
    }

    @Inject(method = "makeAreaOfEffectCloud", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void arclight$makeCloud(PotionContents potionContents, CallbackInfo ci, AreaEffectCloud entity) {
        // Fusion Core: Paper patches 0554/0900: no particles when cancelled; a cloud whose effects a listener cleared is not
        // spawned unless it allowed empty creation (Paper's check, including its name for the flag)
        boolean noEffects = potionContents.hasEffects();
        LingeringPotionSplashEvent event = CraftEventFactory.callLingeringPotionSplashEvent((ThrownPotion) (Object) this, this.fusioncore$hit(), entity);
        this.fusioncore$showParticles = !event.isCancelled();
        if (event.isCancelled() || entity.isRemoved() || (!((LingeringPotionSplashEventBridge) event).allowsEmptyCreation() && noEffects && !entity.potionContents.hasEffects())) {
            ci.cancel();
            entity.discard();
        }
    }

    @Inject(method = "dowseFire", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private void arclight$entityChangeBlock(BlockPos pos, CallbackInfo ci) {
        if (!CraftEventFactory.callEntityChangeBlockEvent((ThrownPotion) (Object) this, pos, ((ThrownPotion) (Object) this).level().getFluidState(pos).createLegacyBlock())) { // Fusion Core: new block = the fluid left behind, as in Paper (patch 0683)
            ci.cancel();
        }
    }

    @Inject(method = "dowseFire", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(Lnet/minecraft/world/entity/player/Player;ILnet/minecraft/core/BlockPos;I)V"))
    private void arclight$entityChangeBlock2(BlockPos pos, CallbackInfo ci, BlockState state) {
        if (!CraftEventFactory.callEntityChangeBlockEvent((ThrownPotion) (Object) this, pos, state.setValue(CampfireBlock.LIT, false))) {
            ci.cancel();
        }
    }

    @Inject(method = "dowseFire", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/AbstractCandleBlock;extinguish(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V"))
    private void arclight$entityChangeBlock3(BlockPos pos, CallbackInfo ci, BlockState state) {
        if (!CraftEventFactory.callEntityChangeBlockEvent((ThrownPotion) (Object) this, pos, state.setValue(AbstractCandleBlock.LIT, false))) {
            ci.cancel();
        }
    }
}
