package io.izzel.arclight.common.bridge.bukkit;

import net.minecraft.network.chat.Component;

/** Fusion Core: sets both tab list texts of a {@code CraftPlayer} and sends them, like Spigot's string setter. */
public interface CraftPlayerTabListBridge {

    void fusioncore$setPlayerListHeaderFooter(Component header, Component footer);

    Component fusioncore$getPlayerListHeader();

    Component fusioncore$getPlayerListFooter();

    /** Fusion Core: players this player unlisted from its tab list (Paper's listing API, patch 0862). */
    java.util.Set<java.util.UUID> fusioncore$unlisted();
}
