package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code SuspiciousStewMeta#addCustomEffect(SuspiciousEffectEntry, boolean)}. */
@Mixin(value = org.bukkit.inventory.meta.SuspiciousStewMeta.class, remap = false)
public interface SuspiciousStewMetaMixin {

    default boolean addCustomEffect(io.papermc.paper.potion.SuspiciousEffectEntry suspiciousEffectEntry, boolean overwrite) {
        com.google.common.base.Preconditions.checkArgument(suspiciousEffectEntry != null, "Suspicious effect entry cannot be null");
        return ((org.bukkit.inventory.meta.SuspiciousStewMeta) this).addCustomEffect(
            new org.bukkit.potion.PotionEffect(suspiciousEffectEntry.effect(), suspiciousEffectEntry.duration(), 0), overwrite);
    }
}
