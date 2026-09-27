package io.izzel.arclight.common.mixin.bukkit.inventory;

import io.izzel.arclight.common.mod.util.FusionCoreProfiles;
import org.bukkit.craftbukkit.v.inventory.CraftMetaSkull;
import org.bukkit.profile.PlayerProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Fusion Core: {@code setOwnerProfile} accepts Paper's profile (patch 0165). */
@Mixin(value = CraftMetaSkull.class, remap = false)
public abstract class CraftMetaSkullMixin {

    @ModifyVariable(method = "setOwnerProfile", argsOnly = true, at = @At("HEAD"))
    private PlayerProfile fusioncore$paperProfile(PlayerProfile profile) {
        return FusionCoreProfiles.toSpigotForSkull(profile);
    }
}
