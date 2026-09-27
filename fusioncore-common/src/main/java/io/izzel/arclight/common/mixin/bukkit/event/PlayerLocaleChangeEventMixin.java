package io.izzel.arclight.common.mixin.bukkit.event;

import net.kyori.adventure.translation.Translator;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Locale;
import java.util.Objects;

/** Fusion Core: Paper's {@code locale()} on {@link PlayerLocaleChangeEvent} (Paper patch 0006 "Adventure"). */
@Mixin(value = PlayerLocaleChangeEvent.class, remap = false)
public abstract class PlayerLocaleChangeEventMixin {

    @Shadow public abstract String getLocale();

    public Locale locale() {
        return Objects.requireNonNullElse(Translator.parseLocale(this.getLocale()), Locale.US);
    }
}
