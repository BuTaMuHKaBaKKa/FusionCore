package io.izzel.arclight.common.bridge.bukkit;

/** Fusion Core: Paper's hidden potion effect (Paper patch 0526), set only while the effect is being built. */
public interface PotionEffectBridge {

    void bridge$setHiddenEffect(org.bukkit.potion.PotionEffect hiddenEffect);
}
