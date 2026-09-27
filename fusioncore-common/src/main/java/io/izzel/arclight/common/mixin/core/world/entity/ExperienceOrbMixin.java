package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin extends EntityMixin {

    // @formatter:off
    @Shadow private Player followingPlayer;
    @Shadow public abstract boolean hurt(DamageSource source, float amount);
    @Shadow public int value;
    // @formatter:on

    private transient Player arclight$lastPlayer;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;discard()V"))
    private void arclight$tickDespawn(CallbackInfo ci) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.DESPAWN);
    }

    @Inject(method = "merge", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;discard()V"))
    private void arclight$merge(CallbackInfo ci) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.MERGE);
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;discard()V"))
    private void arclight$pickup(CallbackInfo ci) {
        this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.PICKUP);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void arclight$captureLast(CallbackInfo ci) {
        arclight$lastPlayer = this.followingPlayer;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void arclight$captureReset(CallbackInfo ci) {
        arclight$lastPlayer = null;
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", ordinal = 4, target = "Lnet/minecraft/world/entity/ExperienceOrb;followingPlayer:Lnet/minecraft/world/entity/player/Player;"))
    private Player arclight$targetPlayer(ExperienceOrb entity) {
        if (this.followingPlayer != arclight$lastPlayer) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent((ExperienceOrb) (Object) this, this.followingPlayer, (this.followingPlayer != null) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.FORGOT_TARGET);
            LivingEntity target = (event.getTarget() == null) ? null : ((CraftLivingEntity) event.getTarget()).getHandle();

            if (event.isCancelled()) {
                this.followingPlayer = arclight$lastPlayer;
                return null;
            } else {
                this.followingPlayer = (target instanceof Player) ? (Player) target : null;
            }
        }
        return this.followingPlayer;
    }

    @Decorate(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"))
    private void arclight$expChange(Player player, int amount) throws Throwable {
        // Fusion Core: Paper's orb as the source (patch 0112); the amount stays what is left after mending
        var event = io.izzel.arclight.common.mod.util.FusionCoreEvents.newPlayerExpChangeEvent((org.bukkit.entity.Player) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) player).bridge$getBukkitEntity(),
            this.getBukkitEntity(), amount);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        DecorationOps.callsite().invoke(player, event.getAmount());
    }

    @Decorate(method = "playerTouch", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/entity/player/Player;takeXpDelay:I"))
    private void arclight$cooldown(Player instance, int value) throws Throwable {
        DecorationOps.callsite().invoke(instance, CraftEventFactory.callPlayerXpCooldownEvent(instance, value, PlayerExpCooldownChangeEvent.ChangeReason.PICKUP_ORB).getNewCooldown());
    }

    @Decorate(method = "repairPlayerItems", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V"))
    private void arclight$itemMend(ServerPlayer serverPlayer, int i, @Local(ordinal = -1) ItemStack itemstack,
                                   @Local(ordinal = -1) Optional<EnchantedItemInUse> optional, @Local(ordinal = -1) int k) throws Throwable {
        // Fusion Core: Paper's consumed experience (patch 0815), as vanilla computes it below from the repair before the event
        int possible = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyDurabilityToRepairFromXp(serverPlayer.serverLevel(), itemstack, i);
        org.bukkit.event.player.PlayerItemMendEvent event = io.izzel.arclight.common.mod.util.FusionCoreEvents.callPlayerItemMend(serverPlayer, (ExperienceOrb) (Object) this, itemstack, optional.get().inSlot(), k,
            k > 0 && possible > 0 ? k * i / possible : 0);
        k = event.getRepairAmount();
        if (event.isCancelled()) {
            DecorationOps.cancel().invoke(i);
            return;
        }
        DecorationOps.blackhole().invoke(k);
    }

    // Fusion Core: CraftBukkit wrote the leftover of a mending step into this.value here (for PlayerItemMendEvent). Paper
    // dropped that (patch 0355): the field is the value of each of the orb's `count` orbs, so every remaining orb of a
    // stacked orb then gave the leftover instead of its value. Removed as in Paper.

    @Inject(method = "getExperienceValue", cancellable = true, at = @At("HEAD"))
    private static void arclight$higherLevelSplit(int expValue, CallbackInfoReturnable<Integer> cir) {
        // @formatter:off
        if (expValue > 162670129) { cir.setReturnValue(expValue - 100000); return; }
        if (expValue > 81335063) { cir.setReturnValue(81335063); return; }
        if (expValue > 40667527) { cir.setReturnValue(40667527); return; }
        if (expValue > 20333759) { cir.setReturnValue(20333759); return; }
        if (expValue > 10166857) { cir.setReturnValue(10166857); return; }
        if (expValue > 5083423) { cir.setReturnValue(5083423); return; }
        if (expValue > 2541701) { cir.setReturnValue(2541701); return; }
        if (expValue > 1270849) { cir.setReturnValue(1270849); return; }
        if (expValue > 635413) { cir.setReturnValue(635413); return; }
        if (expValue > 317701) { cir.setReturnValue(317701); return; }
        if (expValue > 158849) { cir.setReturnValue(158849); return; }
        if (expValue > 79423) { cir.setReturnValue(79423); return; }
        if (expValue > 39709) { cir.setReturnValue(39709); return; }
        if (expValue > 19853) { cir.setReturnValue(19853); return; }
        if (expValue > 9923) { cir.setReturnValue(9923); return; }
        if (expValue > 4957) { cir.setReturnValue(4957); }
        // @formatter:on
    }

    // Fusion Core: Paper's PlayerPickupExperienceEvent (Paper patch 0162), checked when the player's pickup delay is 0;
    // a cancelled pickup reads as a running delay, so the orb stays

    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "playerTouch", at = @At(value = "FIELD", ordinal = 0, opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/world/entity/player/Player;takeXpDelay:I"))
    private int fusioncore$pickupEvent(int delay, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) Player player) {
        if (delay == 0 && !io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents.pickupExperience(player, (ExperienceOrb) (Object) this)) {
            return 1;
        }
        return delay;
    }

    // Fusion Core: Paper's ExperienceOrbMergeEvent (Paper patch 0355) for vanilla's orb merging
    @Inject(method = "merge", cancellable = true, at = @At("HEAD"))
    private void fusioncore$mergeEvent(ExperienceOrb other, CallbackInfo ci) {
        if (!io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents.orbMerge((ExperienceOrb) (Object) this, other)) {
            ci.cancel();
        }

    }
}
