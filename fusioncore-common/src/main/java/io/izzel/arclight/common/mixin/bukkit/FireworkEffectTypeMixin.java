package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code translationKey()} of a firework shape (patch 0404), vanilla's shape names. */
@Mixin(value = org.bukkit.FireworkEffect.Type.class, remap = false)
public abstract class FireworkEffectTypeMixin implements net.kyori.adventure.translation.Translatable {

    public String translationKey() {
        // a switch on the name: an enum switch would need a synthetic inner class, which a mixin cannot carry
        String constant = ((org.bukkit.FireworkEffect.Type) (Object) this).name();
        String name = switch (constant) {
            case "BALL" -> "small_ball";
            case "BALL_LARGE" -> "large_ball";
            default -> constant.toLowerCase(java.util.Locale.ENGLISH);
        };
        return "item.minecraft.firework_star.shape." + name;
    }
}
