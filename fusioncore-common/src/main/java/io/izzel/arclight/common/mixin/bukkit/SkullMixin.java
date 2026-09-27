package io.izzel.arclight.common.mixin.bukkit;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.izzel.arclight.common.mod.util.FusionCoreProfiles;
import org.bukkit.block.Skull;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's profile accessors on {@link Skull} (Paper patch 0165), over Spigot's owner profile. */
@Mixin(value = Skull.class, remap = false)
public interface SkullMixin {

    default void setPlayerProfile(PlayerProfile profile) {
        FusionCoreProfiles.setPlayerProfile((Skull) this, profile);
    }

    default PlayerProfile getPlayerProfile() {
        return FusionCoreProfiles.getPlayerProfile((Skull) this);
    }
}
