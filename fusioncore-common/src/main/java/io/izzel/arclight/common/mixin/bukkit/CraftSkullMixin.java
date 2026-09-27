package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.craftbukkit.v.block.CraftSkull;
import org.bukkit.craftbukkit.v.profile.CraftPlayerProfile;
import org.bukkit.profile.PlayerProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: {@code setOwnerProfile} with Paper's profile (Paper patch 0165 "Add setPlayerProfile API for Skulls"):
 * Spigot casts to its own {@code CraftPlayerProfile}; Paper validates the profile's {@code buildResolvableProfile()}.
 */
@Mixin(value = CraftSkull.class, remap = false)
public abstract class CraftSkullMixin {

    @Shadow private ResolvableProfile profile;

    @Inject(method = "setOwnerProfile", cancellable = true, at = @At("HEAD"))
    private void fusioncore$paperProfile(PlayerProfile profile, CallbackInfo ci) {
        if (profile instanceof com.destroystokyo.paper.profile.CraftPlayerProfile paper) {
            this.profile = CraftPlayerProfile.validateSkullProfile(paper.buildResolvableProfile());
            ci.cancel();
        }
    }
}
