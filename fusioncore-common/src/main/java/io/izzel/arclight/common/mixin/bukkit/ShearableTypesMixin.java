package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: the Bukkit types Paper declares as {@link io.papermc.paper.entity.Shearable}, with {@code PaperShearable}'s bodies (patch 0802). */
@Mixin(value = {
    org.bukkit.entity.Bogged.class, org.bukkit.entity.MushroomCow.class, org.bukkit.entity.Sheep.class, org.bukkit.entity.Snowman.class
}, remap = false)
public interface ShearableTypesMixin extends io.papermc.paper.entity.Shearable {

    @Override
    default boolean readyToBeSheared() {
        return FusionCoreEntities.shearable(this).readyForShearing();
    }

    @Override
    default void shear(net.kyori.adventure.sound.Sound.Source source) {
        FusionCoreEntities.shear(this, source);
    }
}
