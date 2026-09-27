// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.ban;

import org.bukkit.BanList;
import org.bukkit.ban.IpBanList;
import org.bukkit.ban.ProfileBanList;

/**
 * Represents a ban-type that a {@link BanList} may track.
 * It enforces the correct return value at compile time.
 */
public interface BanListType<T> {

    /**
     * Banned IP addresses
     */
    BanListType<IpBanList> IP = new BanListTypeImpl<>(IpBanList.class);
    /**
     * Banned player profiles
     */
    BanListType<ProfileBanList> PROFILE = new BanListTypeImpl<>(ProfileBanList.class);

    /**
     * Returns the type class of the ban list used generically
     *
     * @return the type class
     */
    Class<T> typeClass();
}
