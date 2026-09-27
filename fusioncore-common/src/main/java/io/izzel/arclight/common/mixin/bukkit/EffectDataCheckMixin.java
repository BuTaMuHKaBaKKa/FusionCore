package io.izzel.arclight.common.mixin.bukkit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.bukkit.Effect;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fusion Core: {@code World#playEffect}/{@code Player#playEffect} check the data with Paper's {@code Effect#isApplicable}
 * instead of Spigot's single data class (Paper patch "Bandaid fix for Effect"), so {@code STEP_SOUND} takes {@code BlockData}.
 */
@Mixin(value = {CraftWorld.class, CraftPlayer.class}, remap = false)
public abstract class EffectDataCheckMixin {

    @WrapOperation(method = {"playEffect(Lorg/bukkit/Location;Lorg/bukkit/Effect;Ljava/lang/Object;I)V", "playEffect(Lorg/bukkit/Location;Lorg/bukkit/Effect;Ljava/lang/Object;)V"},
        at = @At(value = "INVOKE", target = "Ljava/lang/Class;isAssignableFrom(Ljava/lang/Class;)Z"))
    private boolean fusioncore$isApplicable(Class<?> effectData, Class<?> dataClass, Operation<Boolean> original,
                                          @Local(argsOnly = true) Effect effect, @Local(argsOnly = true) Object data) {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.effectApplicable(effect, data);
    }
}
