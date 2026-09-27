package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's attribute and category API on {@link org.bukkit.potion.PotionEffectType} (patch 0656). */
@Mixin(value = org.bukkit.potion.PotionEffectType.class, remap = false)
public abstract class PotionEffectTypeMixin implements io.papermc.paper.world.flag.FeatureDependant { // FeatureDependant: Paper

    public java.util.Map<org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier> getEffectAttributes() {
        return io.izzel.arclight.common.mod.util.FusionCorePotions.effectAttributes((org.bukkit.potion.PotionEffectType) (Object) this);
    }

    public double getAttributeModifierAmount(org.bukkit.attribute.Attribute attribute, int effectAmplifier) {
        return io.izzel.arclight.common.mod.util.FusionCorePotions.attributeModifierAmount((org.bukkit.potion.PotionEffectType) (Object) this, attribute, effectAmplifier);
    }

    public org.bukkit.potion.PotionEffectType$Category getEffectCategory() {
        return io.izzel.arclight.common.mod.util.FusionCorePotions.effectCategory((org.bukkit.potion.PotionEffectType) (Object) this);
    }
}
