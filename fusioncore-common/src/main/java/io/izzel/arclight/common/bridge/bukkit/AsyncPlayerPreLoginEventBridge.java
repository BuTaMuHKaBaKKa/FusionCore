package io.izzel.arclight.common.bridge.bukkit;

import com.destroystokyo.paper.profile.PlayerProfile;

import java.net.InetAddress;

/**
 * Fusion Core: Paper's fields of {@code AsyncPlayerPreLoginEvent} that only its Paper constructors set, for
 * {@code FusionCoreEvents} and the login code.
 */
public interface AsyncPlayerPreLoginEventBridge {

    void fusioncore$setPaperFields(PlayerProfile profile, InetAddress rawAddress, String hostname);

    /** Paper's {@code getPlayerProfile()}, which javac does not see on the event. */
    PlayerProfile fusioncore$getPlayerProfile();
}
