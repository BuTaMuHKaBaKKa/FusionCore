package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCorePlayers;
import org.bukkit.BanEntry;
import org.bukkit.OfflinePlayer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Date;

/** Fusion Core: Paper's OfflinePlayer API: the deprecated ban helpers and {@code isConnected} (Paper patch 0872). */
@Mixin(value = OfflinePlayer.class, remap = false)
public interface OfflinePlayerMixin {

    /** An offline player is never connected; {@code PlayerMixin} answers for online ones. */
    default boolean isConnected() {
        return false;
    }

    default BanEntry banPlayer(String reason) {
        return this.banPlayer(reason, null, null);
    }

    default BanEntry banPlayer(String reason, String source) {
        return this.banPlayer(reason, null, source);
    }

    default BanEntry banPlayer(String reason, Date expires) {
        return this.banPlayer(reason, expires, null);
    }

    default BanEntry banPlayer(String reason, Date expires, String source) {
        return this.banPlayer(reason, expires, source, true);
    }

    default BanEntry banPlayer(String reason, Date expires, String source, boolean kickIfOnline) {
        return FusionCorePlayers.banName((OfflinePlayer) this, reason, expires, source, kickIfOnline);
    }

    // Paper patch 0271; CraftOfflinePlayerMixin and PlayerMixin answer for the server's own players

    default long getLastLogin() {
        return 0;
    }

    default long getLastSeen() {
        return 0;
    }

    default io.papermc.paper.persistence.PersistentDataContainerView getPersistentDataContainer() {
        return (io.papermc.paper.persistence.PersistentDataContainerView) (Object) new org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer(
            new org.bukkit.craftbukkit.v.persistence.CraftPersistentDataTypeRegistry());
    }
}
