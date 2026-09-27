package io.izzel.arclight.common.mixin.core.world.item;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fusion Core: Paper's PlayerElytraBoostEvent (Paper patch 0212) when a gliding player uses a firework: cancelled, no
 * rocket and no item used; {@code setShouldConsume(false)} keeps the firework.
 */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {

    /** The event of the boost being handled on this thread (use() runs on the server thread only). */
    @Unique private static final ThreadLocal<PlayerElytraBoostEvent> fusioncore$BOOST = new ThreadLocal<>();
    @Unique private static final ThreadLocal<Boolean> fusioncore$SPAWNED = new ThreadLocal<>();

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean fusioncore$boostEvent(Level level, Entity rocket, Operation<Boolean> original, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) Player player,
                                        @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer) || PlayerElytraBoostEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return original.call(level, rocket);
        }
        PlayerElytraBoostEvent event = new PlayerElytraBoostEvent(((ServerPlayerBridge) serverPlayer).bridge$getBukkitEntity(),
            CraftItemStack.asCraftMirror(player.getItemInHand(hand)), (org.bukkit.entity.Firework) ((EntityBridge) rocket).bridge$getBukkitEntity(),
            CraftEquipmentSlot.getHand(hand));
        Bukkit.getPluginManager().callEvent(event);
        fusioncore$BOOST.set(event);
        boolean spawned = !event.isCancelled() && original.call(level, rocket);
        fusioncore$SPAWNED.set(spawned);
        return spawned;
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"))
    private void fusioncore$boostConsume(ItemStack stack, int amount, LivingEntity entity, Operation<Void> original) {
        PlayerElytraBoostEvent event = fusioncore$BOOST.get();
        if (event == null) {
            original.call(stack, amount, entity);
        } else if (Boolean.TRUE.equals(fusioncore$SPAWNED.get()) && event.shouldConsume()) {
            original.call(stack, amount, entity);
        } else if (entity instanceof ServerPlayer player) {
            ((ServerPlayerBridge) player).bridge$getBukkitEntity().updateInventory();
        }
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V"))
    private void fusioncore$boostStat(Player player, Stat<?> stat, Operation<Void> original) {
        if (fusioncore$BOOST.get() == null || Boolean.TRUE.equals(fusioncore$SPAWNED.get())) {
            original.call(player, stat);
        }
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void fusioncore$boostDone(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        fusioncore$BOOST.remove();
        fusioncore$SPAWNED.remove();
    }

    // Fusion Core: Paper's PlayerLaunchProjectileEvent (patch 0213) for a firework launched from a block
    @Inject(method = "useOn", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void fusioncore$launchFirework(net.minecraft.world.item.context.UseOnContext context, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir,
                                         @com.llamalad7.mixinextras.sugar.Local net.minecraft.world.entity.projectile.FireworkRocketEntity rocket) {
        if (context.getPlayer() != null && !io.izzel.arclight.common.mod.util.FusionCorePaperPlayerEvents.launchProjectile(context.getPlayer(), context.getItemInHand(), rocket)) {
            cir.setReturnValue(net.minecraft.world.InteractionResult.PASS);
        }
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void fusioncore$launchFireworkConsume(ItemStack stack, int amount, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                                @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.world.item.context.UseOnContext context) {
        if (context.getPlayer() == null || io.izzel.arclight.common.mod.util.FusionCorePaperPlayerEvents.consumeLaunched(context.getPlayer())) {
            original.call(stack, amount);
        }
    }
}
