package io.izzel.arclight.i18n;

import com.google.common.reflect.TypeToken;
import io.izzel.arclight.i18n.conf.ConfigSpec;
import io.izzel.arclight.i18n.conf.WorldSpec;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class ArclightConfig {

    private static ArclightConfig instance;

    private final CommentedConfigurationNode node;
    private final ConfigSpec spec;

    public ArclightConfig(CommentedConfigurationNode node) throws ObjectMappingException {
        this.node = node;
        this.spec = this.node.getValue(TypeToken.of(ConfigSpec.class));
    }

    public CommentedConfigurationNode getNode() {
        return node;
    }

    public ConfigSpec getSpec() {
        return spec;
    }

    public ConfigurationNode get(String path) {
        return this.node.getNode((Object[]) path.split("\\."));
    }

    public static ConfigSpec spec() {
        return instance.spec;
    }

    private final java.util.Map<String, WorldSpec> worldSpecs = new ConcurrentHashMap<>();

    /**
     * Fusion Core: section {@code world} for one world by its folder name: the keys under {@code world.worlds.<name>} with the
     * rest from {@code world}, as Paper's world*{@literal /}paper-world.yml over paper-world-defaults.yml. Resolved once per name.
     */
    public static WorldSpec world(String name) {
        if (name == null) {
            return instance.spec.getWorld();
        }
        return instance.worldSpecs.computeIfAbsent(name, instance::resolveWorld);
    }

    private WorldSpec resolveWorld(String name) {
        ConfigurationNode base = this.node.getNode("world");
        ConfigurationNode override = base.getNode("worlds", name);
        if (override.isVirtual() && name.lastIndexOf('/') >= 0) {
            // the nether and end of a vanilla-layout server are named world/DIM-1, world/DIM1: their folder name works too
            override = base.getNode("worlds", name.substring(name.lastIndexOf('/') + 1));
        }
        if (override.isVirtual()) {
            return this.spec.getWorld();
        }
        try {
            ConfigurationNode merged = override.copy();
            merged.mergeValuesFrom(base);
            WorldSpec spec = merged.getValue(TypeToken.of(WorldSpec.class));
            return spec == null ? this.spec.getWorld() : spec;
        } catch (ObjectMappingException e) {
            System.err.println("[FusionCore] Invalid world.worlds." + name + " in fusioncore.conf, using the common values: " + e);
            return this.spec.getWorld();
        }
    }

    private static void load() throws Exception {
        Path path = FusionCoreFiles.config();
        CommentedConfigurationNode node = HoconConfigurationLoader.builder().setSource(
            () -> new BufferedReader(new InputStreamReader(ArclightConfig.class.getResourceAsStream("/META-INF/fusioncore.conf"), StandardCharsets.UTF_8))
        ).build().load();
        boolean fresh = !java.nio.file.Files.exists(path) && isNewServer();
        HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setPath(path).build();
        CommentedConfigurationNode cur = loader.load();
        if (fresh) {
            // Fusion Core: a new server (no config file and no world yet) gets CraftBukkit/Paper world names (world_nether, world_the_end),
            // which plugins and their configs expect; existing servers keep the names their plugin data is stored under
            cur.getNode("compatibility", "symlink-world").setValue(true);
        }
        cur.mergeValuesFrom(node);
        cur.getNode("locale", "current").setValue(ArclightLocale.getInstance().getCurrent());
        fillComments(cur, ArclightLocale.getInstance());
        instance = new ArclightConfig(cur);
        loader.save(cur);
    }

    private static boolean isNewServer() {
        String levelName = "world";
        Path properties = Paths.get("server.properties");
        if (java.nio.file.Files.exists(properties)) {
            java.util.Properties p = new java.util.Properties();
            try (var reader = java.nio.file.Files.newBufferedReader(properties, StandardCharsets.UTF_8)) {
                p.load(reader);
                levelName = p.getProperty("level-name", levelName);
            } catch (Exception e) {
                return false;
            }
        }
        return !java.nio.file.Files.exists(Paths.get(levelName));
    }

    private static void fillComments(CommentedConfigurationNode node, ArclightLocale locale) {
        if (!node.getComment().isPresent()) {
            String path = pathOf(node);
            Optional<String> option = locale.getOption("comments." + path + ".comment");
            option.ifPresent(node::setComment);
        }
        if (node.hasMapChildren()) {
            for (CommentedConfigurationNode value : node.getChildrenMap().values()) {
                fillComments(value, locale);
            }
        }
    }

    private static String pathOf(ConfigurationNode node) {
        StringJoiner joiner = new StringJoiner(".");
        for (Object o : node.getPath()) {
            if (o != null) {
                joiner.add(o.toString());
            }
        }
        String s = joiner.toString();
        return s.isEmpty() ? "__root__" : s;
    }

    static {
        try {
            load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
