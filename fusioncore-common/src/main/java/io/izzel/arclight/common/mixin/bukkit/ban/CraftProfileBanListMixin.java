package io.izzel.arclight.common.mixin.bukkit.ban;

import io.izzel.arclight.common.mod.util.FusionCoreProfiles;
import org.bukkit.craftbukkit.v.ban.CraftProfileBanList;
import org.bukkit.profile.PlayerProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Fusion Core: the profile ban list accepts Paper's profiles. */
@Mixin(value = CraftProfileBanList.class, remap = false)
public abstract class CraftProfileBanListMixin {

    @ModifyVariable(method = {
        "getBanEntry(Lorg/bukkit/profile/PlayerProfile;)Lorg/bukkit/BanEntry;",
        "addBan(Lorg/bukkit/profile/PlayerProfile;Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;)Lorg/bukkit/BanEntry;",
        "isBanned(Lorg/bukkit/profile/PlayerProfile;)Z",
        "pardon(Lorg/bukkit/profile/PlayerProfile;)V"
    }, argsOnly = true, at = @At("HEAD"))
    private PlayerProfile fusioncore$paperProfile(PlayerProfile profile) {
        return FusionCoreProfiles.toSpigotForBan(profile);
    }
}
