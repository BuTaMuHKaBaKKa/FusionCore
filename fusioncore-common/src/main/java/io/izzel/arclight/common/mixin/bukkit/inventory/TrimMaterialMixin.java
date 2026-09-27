package io.izzel.arclight.common.mixin.bukkit.inventory;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.v.inventory.trim.CraftTrimMaterial;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code TrimMaterial#description()} (Paper patch 0006 "Adventure"): the vanilla description. */
@Mixin(value = TrimMaterial.class, remap = false)
public interface TrimMaterialMixin {

    default Component description() {
        return FusionCoreAdventure.asAdventure(((CraftTrimMaterial) (Object) this).getHandle().description());
    }
}
