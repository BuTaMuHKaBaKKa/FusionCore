package io.izzel.arclight.common.mixin.core.world.entity.animal.horse;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityDeathBridge;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: a donkey, mule or llama whose death a plugin cancels keeps its chest (Paper patch 0242). */
@Mixin(AbstractChestedHorse.class)
public abstract class AbstractChestedHorseDeathMixin {

    @WrapOperation(method = "dropEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/AbstractChestedHorse;setChest(Z)V"))
    private void fusioncore$clearAfterEvent(AbstractChestedHorse instance, boolean chest, Operation<Void> original) {
        if (!((LivingEntityDeathBridge) instance).fusioncore$deferDeathClear(() -> original.call(instance, chest))) {
            original.call(instance, chest);
        }
    }
}
