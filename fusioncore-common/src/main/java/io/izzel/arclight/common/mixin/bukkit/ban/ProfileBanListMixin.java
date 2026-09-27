package io.izzel.arclight.common.mixin.bukkit.ban;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.profile.PlayerProfile;
import org.spongepowered.asm.mixin.Mixin;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Fusion Core: the {@link ProfileBanList} overloads Paper declares (patch 0398); {@code CraftProfileBanList} overrides most.
 */
@Mixin(value = ProfileBanList.class, remap = false)
@SuppressWarnings({"unchecked", "rawtypes"})
public interface ProfileBanListMixin {

    default BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(com.destroystokyo.paper.profile.PlayerProfile target, String reason, Date expires, String source) {
        return (BanEntry) ((ProfileBanList) (Object) this).addBan((PlayerProfile) target, reason, expires, source);
    }

    default BanEntry getBanEntry(PlayerProfile target) {
        return ((BanList<Object>) (Object) this).getBanEntry((Object) target);
    }

    default boolean isBanned(PlayerProfile target) {
        return ((BanList<Object>) (Object) this).isBanned((Object) target);
    }

    default void pardon(PlayerProfile target) {
        ((BanList<Object>) (Object) this).pardon((Object) target);
    }

    default BanEntry addBan(PlayerProfile target, String reason, Instant expires, String source) {
        return ((BanList<Object>) (Object) this).addBan((Object) target, reason, expires, source);
    }

    default BanEntry addBan(PlayerProfile target, String reason, Duration duration, String source) {
        return ((BanList<Object>) (Object) this).addBan((Object) target, reason, duration, source);
    }
}
