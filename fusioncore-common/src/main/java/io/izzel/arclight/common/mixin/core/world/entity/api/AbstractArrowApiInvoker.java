package io.izzel.arclight.common.mixin.core.world.entity.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public AbstractArrow methods Paper's entity API calls. */
@Mixin(net.minecraft.world.entity.projectile.AbstractArrow.class)
public interface AbstractArrowApiInvoker {

    @Invoker("getPickupItem")
    net.minecraft.world.item.ItemStack fusioncore$getPickupItem();

    @Invoker("setPickupItemStack")
    void fusioncore$setPickupItemStack(net.minecraft.world.item.ItemStack stack);
}
