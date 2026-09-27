package io.izzel.arclight.common.mixin.core.world.level.block.entity.api;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: the furnace's total cook time (already scaled by Paper's cook speed multiplier) for {@code Furnace#setCookSpeedMultiplier}. */
@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceApiInvoker {

    @Invoker("getTotalCookTime")
    static int fusioncore$totalCookTime(Level level, AbstractFurnaceBlockEntity furnace) {
        throw new AssertionError();
    }
}
