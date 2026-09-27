package io.izzel.arclight.common.mixin.bukkit;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.izzel.arclight.common.mod.util.FusionCoreProfiles;
import org.bukkit.inventory.meta.SkullMeta;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's profile accessors on {@link SkullMeta} (Paper patch 0165), over Spigot's owner profile. */
@Mixin(value = SkullMeta.class, remap = false)
public interface SkullMetaMixin {

    default void setPlayerProfile(PlayerProfile profile) {
        FusionCoreProfiles.setPlayerProfile((SkullMeta) this, profile);
    }

    default PlayerProfile getPlayerProfile() {
        return FusionCoreProfiles.getPlayerProfile((SkullMeta) this);
    }
}
