package io.izzel.arclight.common.mixin.bukkit;

import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.craftbukkit.v.profile.CraftPlayerProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: lets Paper's profile share its property map with Spigot's {@code CraftPlayerTextures}. */
@Mixin(value = CraftPlayerProfile.class, remap = false)
public interface CraftPlayerProfileAccessor {

    @Mutable
    @Accessor("properties")
    void fusioncore$setProperties(PropertyMap properties);
}
