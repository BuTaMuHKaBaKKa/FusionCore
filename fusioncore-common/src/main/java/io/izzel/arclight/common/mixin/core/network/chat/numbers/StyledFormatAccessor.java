package io.izzel.arclight.common.mixin.core.network.chat.numbers;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.StyledFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: the package-private style of a styled number format (Paper's access transformer, patch 0930). */
@Mixin(StyledFormat.class)
public interface StyledFormatAccessor {

    @Accessor("style")
    Style fusioncore$style();
}
