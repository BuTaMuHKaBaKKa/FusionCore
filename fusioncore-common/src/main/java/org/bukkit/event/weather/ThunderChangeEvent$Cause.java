package org.bukkit.event.weather;

/**
 * Fusion Core: Paper's {@code ThunderChangeEvent.Cause}. Spigot's event class is not ours to edit, so the enum is
 * compiled as a top-level class under the binary name the JVM looks up.
 */
@SuppressWarnings("checkstyle:TypeName")
public enum ThunderChangeEvent$Cause {
    /** Command caused the change. */
    COMMAND,
    /** Natural weather cycle. */
    NATURAL,
    /** Everyone sleeping (the weather is cleared). */
    SLEEP,
    /** A plugin changed the weather through the world API. */
    PLUGIN,
    /** Anything else. */
    UNKNOWN
}
