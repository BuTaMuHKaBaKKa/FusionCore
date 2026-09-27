package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreRegistries;
import net.minecraft.core.Registry;
import org.bukkit.Keyed;
import org.bukkit.craftbukkit.v.CraftRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's tags on server-backed registries (patch "Registry Modification API", RegistrySet API). */
@Mixin(value = CraftRegistry.class, remap = false)
public abstract class CraftRegistryMixin<B extends Keyed, M> {

    @Shadow @Final private Registry<M> minecraftRegistry;

    public boolean hasTag(io.papermc.paper.registry.tag.TagKey<B> key) {
        return FusionCoreRegistries.hasTag(this.minecraftRegistry, key);
    }

    public io.papermc.paper.registry.tag.Tag<B> getTag(io.papermc.paper.registry.tag.TagKey<B> key) {
        return FusionCoreRegistries.tag(this.minecraftRegistry, key);
    }
}
