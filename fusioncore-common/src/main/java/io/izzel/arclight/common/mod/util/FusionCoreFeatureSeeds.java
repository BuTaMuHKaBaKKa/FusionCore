package io.izzel.arclight.common.mod.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fusion Core: Paper's feature-seeds (patch 0624). A configured feature with a seed of its own is decorated with that seed
 * instead of the world seed. Seeds live in {@code fusioncore-feature-seeds.json} in the main world folder, dimension id ->
 * configured feature id -> seed; with {@code world.feature-seeds.generate-random-seeds-for-all} (per world) every registered feature
 * missing there gets a random one, as Paper fills paper-world.yml. Read once per dimension, on the first decorated chunk
 * (world generation threads), and kept for the run. A seed is used only once it is in the file (it must survive a restart);
 * a file that cannot be read or written leaves the seeds it already holds in use, and the rest is tried again every 30 s.
 */
public final class FusionCoreFeatureSeeds {

    private static final Logger LOGGER = LogManager.getLogger("FusionCore");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE = "fusioncore-feature-seeds.json";
    private static final Map<ResourceKey<Level>, Loaded> SEEDS = new ConcurrentHashMap<>();
    private static final long RETRY_MILLIS = 30_000L;

    /** The seeds of one dimension; an incomplete load (the file could not be read or written) is tried again later */
    private record Loaded(Reference2LongMap<ConfiguredFeature<?, ?>> seeds, long retryAt) {
    }

    private FusionCoreFeatureSeeds() {
    }

    /** The seed of the feature in this level, or -1 for the world seed (Paper's "no seed") */
    public static long seed(ServerLevel level, ConfiguredFeature<?, ?> feature) {
        Loaded loaded = SEEDS.get(level.dimension());
        if (loaded == null || loaded.retryAt != 0 && System.currentTimeMillis() >= loaded.retryAt) {
            loaded = load(level);
            SEEDS.put(level.dimension(), loaded);
        }
        return loaded.seeds.getLong(feature);
    }

    private static synchronized Loaded load(ServerLevel level) {
        Reference2LongOpenHashMap<ConfiguredFeature<?, ?>> result = new Reference2LongOpenHashMap<>();
        result.defaultReturnValue(-1);
        Path file = level.getServer().getWorldPath(LevelResource.ROOT).resolve(FILE);
        String dimension = level.dimension().location().toString();
        boolean generate = FusionCoreWorldSettings.spec(level).getFeatureSeeds().isGenerateRandomSeedsForAll();
        Registry<ConfiguredFeature<?, ?>> registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        JsonObject root;
        Map<String, Long> seeds = new TreeMap<>();
        try {
            root = Files.isRegularFile(file)
                ? JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject() : new JsonObject();
            if (root.has(dimension)) {
                root.getAsJsonObject(dimension).entrySet().forEach(e -> seeds.put(e.getKey(), e.getValue().getAsLong()));
            }
        } catch (Exception e) {
            // never overwrite a file we cannot read; generate with the world seed meanwhile and try again later
            LOGGER.error("Cannot read feature seeds in {}; {} uses the world seed until it can be read", file, dimension, e);
            return new Loaded(result, System.currentTimeMillis() + RETRY_MILLIS);
        }
        // the seeds already in the file are always used
        apply(seeds, registry, result);
        if (!generate) {
            return new Loaded(result, 0L);
        }
        Map<String, Long> added = new TreeMap<>();
        SecureRandom random = new SecureRandom();
        for (ResourceLocation id : registry.keySet()) {
            if (!seeds.containsKey(id.toString())) {
                added.put(id.toString(), random.nextLong());
            }
        }
        if (added.isEmpty()) {
            return new Loaded(result, 0L);
        }
        Map<String, Long> all = new TreeMap<>(seeds);
        all.putAll(added);
        try {
            root.add(dimension, GSON.toJsonTree(all));
            Path tmp = file.resolveSibling(FILE + ".tmp");
            Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8);
            Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            // a seed that is not on disk would change after a restart: the new features keep the world seed until written
            LOGGER.error("Cannot write {} new feature seeds to {}; they use the world seed until it can be written", added.size(), file, e);
            return new Loaded(result, System.currentTimeMillis() + RETRY_MILLIS);
        }
        apply(added, registry, result);
        LOGGER.info("Generated {} random feature seeds for {}", added.size(), dimension);
        return new Loaded(result, 0L);
    }

    private static void apply(Map<String, Long> seeds, Registry<ConfiguredFeature<?, ?>> registry, Reference2LongOpenHashMap<ConfiguredFeature<?, ?>> result) {
        for (Map.Entry<String, Long> e : seeds.entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(e.getKey());
            ConfiguredFeature<?, ?> feature = id == null ? null : registry.get(id);
            if (feature != null && e.getValue() != -1) {
                result.put(feature, (long) e.getValue());
            }
        }
    }
}
