package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code translationKey()} of a villager profession (patch 0404). */
@Mixin(value = org.bukkit.entity.Villager.Profession.class, remap = false)
public interface VillagerProfessionMixin extends net.kyori.adventure.translation.Translatable {

    @Override
    default String translationKey() {
        return "entity.minecraft.villager." + ((org.bukkit.entity.Villager.Profession) this).getKey().getKey();
    }
}
