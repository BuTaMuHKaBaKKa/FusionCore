package org.bukkit.event.player;

/**
 * Fusion Core: Paper's {@code PlayerRespawnEvent.RespawnFlag}. Spigot's event class is not ours to edit, so the enum is
 * compiled as a top-level class under the binary name the JVM looks up.
 */
@SuppressWarnings("checkstyle:TypeName")
public enum PlayerRespawnEvent$RespawnFlag {
    /** Will use the bed spawn location. */
    BED_SPAWN,
    /** Will use the respawn anchor location. */
    ANCHOR_SPAWN,
    /** Is caused by going to the end portal in the end. */
    END_PORTAL,
}
