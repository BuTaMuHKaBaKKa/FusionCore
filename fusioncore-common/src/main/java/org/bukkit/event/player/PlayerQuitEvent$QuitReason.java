package org.bukkit.event.player;

/**
 * Fusion Core: Paper's {@code PlayerQuitEvent.QuitReason}. Spigot's event class is not ours to edit, so the enum is
 * compiled as a top-level class under the binary name the JVM looks up.
 */
@SuppressWarnings("checkstyle:TypeName")
public enum PlayerQuitEvent$QuitReason {
    /** The player left on their own behalf (the client closed the connection). */
    DISCONNECTED,
    /** The player was kicked from the server. */
    KICKED,
    /** The player has timed out. */
    TIMED_OUT,
    /** The player's connection has entered an erroneous state (invalid packets, uncaught exceptions, ...). */
    ERRONEOUS_STATE,
}
