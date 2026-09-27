package io.izzel.arclight.common.bridge.bukkit;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;

public interface CraftServerBridge {

    void bridge$setPlayerList(PlayerList playerList);

    void bridge$removeWorld(ServerLevel world);

    ChunkGenerator bridge$consumeGeneratorCache(String name);

    // One-shot custom chunk generation cache
    void bridge$offerGeneratorCache(String name, ChunkGenerator generator);

    BiomeProvider bridge$consumeBiomeProviderCache(String name);

    // One-shot custom chunk generation cache
    void bridge$offerBiomeProviderCache(String name, BiomeProvider provider);

    World.Environment bridge$consumeEnvironmentCache(String name);

    void bridge$offerEnvironmentCache(String name, World.Environment environment);

    /** Fusion Core: Paper's {@code Server#reloadCommandAliases}. */
    boolean fusioncore$reloadCommandAliases();

    /** Fusion Core: Paper's {@code Server#reloadPermissions}. */
    void fusioncore$reloadPermissions();

    /** Fusion Core: Paper's {@code Server.Spigot#getBukkitConfig}. */
    org.bukkit.configuration.file.YamlConfiguration fusioncore$bukkitConfig();
}
