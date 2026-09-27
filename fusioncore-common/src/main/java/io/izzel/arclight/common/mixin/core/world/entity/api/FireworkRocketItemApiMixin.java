package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.bridge.core.world.entity.api.FireworkApiBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper records who placed a firework on a block or boosted with one as its spawning entity (patch 0121). */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemApiMixin {

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean fusioncore$placedBy(Level level, Entity entity, Operation<Boolean> original, @Local(argsOnly = true) UseOnContext context) {
        ((FireworkApiBridge) entity).fusioncore$setSpawningEntity(context.getPlayer() == null ? null : context.getPlayer().getUUID());
        return original.call(level, entity);
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean fusioncore$boostedBy(Level level, Entity entity, Operation<Boolean> original, @Local(argsOnly = true) Player player) {
        ((FireworkApiBridge) entity).fusioncore$setSpawningEntity(player.getUUID());
        return original.call(level, entity);
    }
}
