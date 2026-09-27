package org.bukkit.event.player;

/**
 * Fusion Core: Paper's {@code PlayerKickEvent.Cause}. Spigot's event class is not ours to edit, so the enum is
 * compiled as a top-level class under the binary name the JVM looks up.
 */
@SuppressWarnings("checkstyle:TypeName")
public enum PlayerKickEvent$Cause {
    PLUGIN,
    WHITELIST,
    BANNED,
    IP_BANNED,
    KICK_COMMAND,
    FLYING_PLAYER,
    FLYING_VEHICLE,
    TIMEOUT,
    IDLING,
    INVALID_VEHICLE_MOVEMENT,
    INVALID_PLAYER_MOVEMENT,
    INVALID_ENTITY_ATTACKED,
    INVALID_PAYLOAD,
    INVALID_COOKIE,
    SPAM,
    ILLEGAL_ACTION,
    ILLEGAL_CHARACTERS,
    OUT_OF_ORDER_CHAT,
    UNSIGNED_CHAT,
    CHAT_VALIDATION_FAILED,
    EXPIRED_PROFILE_PUBLIC_KEY,
    INVALID_PUBLIC_KEY_SIGNATURE,
    TOO_MANY_PENDING_CHATS,
    SELF_INTERACTION,
    DUPLICATE_LOGIN,
    RESOURCE_PACK_REJECTION,
    RESTART_COMMAND,
    UNKNOWN,
}
