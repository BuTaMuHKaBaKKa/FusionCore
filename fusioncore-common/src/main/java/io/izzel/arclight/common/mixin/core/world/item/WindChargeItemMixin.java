package io.izzel.arclight.common.mixin.core.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.mod.util.FusionCorePaperPlayerEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fusion Core: Paper's PlayerLaunchProjectileEvent (patch 0213): a cancelled launch fails the use (with no cooldown), and
 * {@code setShouldConsume(false)} keeps the item.
 */
@Mixin(WindChargeItem.class)
public abstract class WindChargeItemMixin {

    @Inject(method = "use", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void fusioncore$launchEvent(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                      @Local net.minecraft.world.entity.projectile.windcharge.WindCharge projectile) {
        if (!FusionCorePaperPlayerEvents.launchProjectile(player, player.getItemInHand(hand), projectile)) {
            player.containerMenu.sendAllDataToRemote();
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                // no cooldown was set: clear the one the client predicted on the slot
                serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundCooldownPacket((WindChargeItem) (Object) this, 0));
            }
            cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
        }
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"))
    private void fusioncore$launchConsume(ItemStack stack, int amount, LivingEntity entity, Operation<Void> original) {
        if (FusionCorePaperPlayerEvents.consumeLaunched(entity)) {
            original.call(stack, amount, entity);
        }
    }
}
