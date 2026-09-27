package io.izzel.arclight.common.mixin.core.network.chat.numbers;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.FixedFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: the package-private text of a fixed number format (Paper's access transformer, patch 0930). */
@Mixin(FixedFormat.class)
public interface FixedFormatAccessor {

    @Accessor("value")
    Component fusioncore$value();
}
