package io.izzel.arclight.common.mixin.core.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper's EntityLoadCrossbowEvent (patch 0459). */
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/CrossbowItem;tryLoadProjectiles(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean fusioncore$loadCrossbow(LivingEntity user, ItemStack crossbow, Operation<Boolean> original, @Local(argsOnly = true) Level level) {
        if (level.isClientSide) {
            return original.call(user, crossbow);
        }
        boolean[] result = FusionCorePaperMobEvents.loadCrossbow(user, crossbow);
        if (result == null) {
            return original.call(user, crossbow);
        }
        if (!result[0] && result[1] && original.call(user, crossbow)) {
            return true;
        }
        if (!result[0] && !result[1]) {
            FusionCorePaperMobEvents.withoutAmmoUse(() -> original.call(user, crossbow));
        }
        if (user instanceof ServerPlayer player) {
            player.containerMenu.sendAllDataToRemote();
        }
        return false;
    }
}
