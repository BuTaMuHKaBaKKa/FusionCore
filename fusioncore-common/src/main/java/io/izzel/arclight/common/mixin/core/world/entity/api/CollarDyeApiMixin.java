package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's EntityDyeEvent for collars (patch 0695): a listener may change the colour or cancel the dyeing. */
@Mixin({Cat.class, Wolf.class})
public abstract class CollarDyeApiMixin {

    // one @At per target class: each class has exactly one of them
    @Inject(method = "mobInteract", cancellable = true, at = {
        @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Cat;setCollarColor(Lnet/minecraft/world/item/DyeColor;)V"),
        @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;setCollarColor(Lnet/minecraft/world/item/DyeColor;)V")
    })
    private void fusioncore$collarDyeEvent(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, @Local LocalRef<DyeColor> color) {
        DyeColor result = FusionCorePaperEntityEvents.collarDye((Entity) (Object) this, color.get(), player);
        if (result == null) {
            cir.setReturnValue(InteractionResult.FAIL);
        } else {
            color.set(result);
        }
    }
}
