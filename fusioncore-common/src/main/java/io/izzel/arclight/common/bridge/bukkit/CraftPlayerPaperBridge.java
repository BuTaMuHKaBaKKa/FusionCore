package io.izzel.arclight.common.bridge.bukkit;

/**
 * Fusion Core: per-player state of Paper's Player API kept on the {@code CraftPlayer}, which outlives the
 * {@code ServerPlayer} across respawns and dimension changes on NeoForge.
 */
public interface CraftPlayerPaperBridge {

    boolean fusioncore$affectsSpawning();

    void fusioncore$setAffectsSpawning(boolean affects);

    net.kyori.adventure.util.TriState fusioncore$flyingFallDamage();

    void fusioncore$setFlyingFallDamage(net.kyori.adventure.util.TriState flyingFallDamage);

    String fusioncore$clientBrand();

    void fusioncore$setClientBrand(String brand);

    long fusioncore$loginTime();

    void fusioncore$setLoginTime(long loginTime);

    long fusioncore$lastSaveTime();

    org.bukkit.event.player.PlayerResourcePackStatusEvent.Status fusioncore$resourcePackStatus();

    void fusioncore$setResourcePackStatus(org.bukkit.event.player.PlayerResourcePackStatusEvent.Status status);

    /** Paper's per-player view distance, or -1 for the world's. */
    int fusioncore$viewDistance();

    void fusioncore$setViewDistance(int viewDistance);

    /** Adventure boss bars shown to this player. */
    java.util.Set<net.kyori.adventure.bossbar.BossBar> fusioncore$bossBars();

    /** Adventure resource pack callbacks by pack id. */
    java.util.Map<java.util.UUID, net.kyori.adventure.resource.ResourcePackCallback> fusioncore$packCallbacks();

    /** Chunks sent to the client and not yet forgotten. Main thread only. */
    it.unimi.dsi.fastutil.longs.LongOpenHashSet fusioncore$sentChunks();
}
