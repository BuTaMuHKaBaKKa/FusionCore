package io.izzel.arclight.common.mixin.core.world.item;

import io.izzel.arclight.common.mod.util.FusionCorePaperPlayerEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fusion Core: Paper's PlayerNameEntityEvent (patch 0496). The event may cancel the naming, name another entity, change the
 * name or not make the mob persistent; the name tag is used up unless the event is cancelled.
 */
@Mixin(NameTagItem.class)
public abstract class NameTagItemMixin {

    @Inject(method = "interactLivingEntity", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setCustomName(Lnet/minecraft/network/chat/Component;)V"))
    private void fusioncore$nameEntityEvent(ItemStack stack, Player player, LivingEntity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = FusionCorePaperPlayerEvents.nameEntity(stack, player, target);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
