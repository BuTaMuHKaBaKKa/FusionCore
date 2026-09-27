package io.izzel.arclight.common.mixin.core.world.entity.api;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: non-public FireworkRocketEntity methods Paper's entity API calls. */
@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityApiInvoker {

    @Invoker("getDefaultItem")
    static ItemStack fusioncore$getDefaultItem() {
        throw new AssertionError();
    }
}
