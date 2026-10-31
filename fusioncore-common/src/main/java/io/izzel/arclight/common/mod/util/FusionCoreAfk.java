package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.i18n.conf.WorldSpec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Fusion Core: Purpur's AFK state (gameplay-mechanics.player.idle-timeout). A player idle for longer than server.properties
 * player-idle-timeout becomes AFK; with kick-if-idle false (or the permission purpur.bypassIdleKick) they stay online. Any
 * action, or moving to another block or turning, ends it. While AFK, mobs-target false keeps mobs from targeting them and
 * tick-nearby-entities false keeps them from activating entities around them (entity activation range).
 */
public final class FusionCoreAfk {

    private FusionCoreAfk() {
    }

    /** Implemented by ServerPlayer through a mixin */
    public interface Afk {

        boolean fusioncore$isAfk();

        void fusioncore$setAfk(boolean afk);
    }

    public static boolean isAfk(Entity entity) {
        return entity instanceof ServerPlayer && entity instanceof Afk afk && afk.fusioncore$isAfk();
    }

    public static WorldSpec.IdleTimeout spec(Entity entity) {
        return FusionCoreWorldSettings.spec(entity.level()).getIdleTimeout();
    }

    /** Mobs may not target this player (AFK, and mobs-target false in its world) */
    public static boolean hiddenFromMobs(Entity target) {
        return isAfk(target) && !spec(target).isMobsTarget();
    }

    /** This player does not activate the entities around it (AFK, and tick-nearby-entities false in its world) */
    public static boolean skipsActivation(Entity player) {
        return isAfk(player) && !spec(player).isTickNearbyEntities();
    }

    /**
     * Called where vanilla kicks an idle player: the player becomes AFK; true if they are kicked anyway (kick-if-idle true
     * and no purpur.bypassIdleKick permission)
     */
    public static boolean idle(ServerPlayer player) {
        ((Afk) player).fusioncore$setAfk(true);
        if (!spec(player).isKickIfIdle()) {
            return false;
        }
        return !((io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge) player).bridge$getBukkitEntity().hasPermission("purpur.bypassIdleKick");
    }
}
