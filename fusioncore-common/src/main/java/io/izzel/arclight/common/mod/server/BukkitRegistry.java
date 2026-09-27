package io.izzel.arclight.common.mod.server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.izzel.arclight.api.EnumHelper;
import io.izzel.arclight.api.Unsafe;
import io.izzel.arclight.common.bridge.bukkit.world.entity.EntityTypeBridge;
import io.izzel.arclight.common.bridge.bukkit.MaterialBridge;
import io.izzel.arclight.common.bridge.bukkit.SimpleRegistryBridge;
import io.izzel.arclight.common.mod.server.entity.EntityClassLookup;
import io.izzel.arclight.common.mod.util.ResourceLocationUtil;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.EntityPropertySpec;
import io.izzel.arclight.i18n.conf.MaterialPropertySpec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v.CraftStatistic;
import org.bukkit.craftbukkit.v.inventory.CraftRecipe;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v.util.CraftSpawnCategory;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pose;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.player.PlayerRecipeBookSettingsChangeEvent;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings({"ConstantConditions", "deprecation"})
public class BukkitRegistry {

    private static final List<Class<?>> MAT_CTOR = ImmutableList.of(int.class);
    private static final List<Class<?>> ENTITY_CTOR = ImmutableList.of(String.class, Class.class, int.class);
    private static final List<Class<?>> ENV_CTOR = ImmutableList.of(int.class);
    private static final Map<String, Material> BY_NAME = Unsafe.getStatic(Material.class, "BY_NAME");
    private static final Map<Block, Material> BLOCK_MATERIAL = Unsafe.getStatic(CraftMagicNumbers.class, "BLOCK_MATERIAL");
    private static final Map<Item, Material> ITEM_MATERIAL = Unsafe.getStatic(CraftMagicNumbers.class, "ITEM_MATERIAL");
    private static final Map<Material, Item> MATERIAL_ITEM = Unsafe.getStatic(CraftMagicNumbers.class, "MATERIAL_ITEM");
    private static final Map<Material, Block> MATERIAL_BLOCK = Unsafe.getStatic(CraftMagicNumbers.class, "MATERIAL_BLOCK");
    private static final Map<String, EntityType> ENTITY_NAME_MAP = Unsafe.getStatic(EntityType.class, "NAME_MAP");
    private static final Map<Integer, World.Environment> ENVIRONMENT_MAP = Unsafe.getStatic(World.Environment.class, "lookup");
    static final BiMap<ResourceKey<LevelStem>, World.Environment> DIM_MAP =
        HashBiMap.create(ImmutableMap.<ResourceKey<LevelStem>, World.Environment>builder()
            .put(LevelStem.OVERWORLD, World.Environment.NORMAL)
            .put(LevelStem.NETHER, World.Environment.NETHER)
            .put(LevelStem.END, World.Environment.THE_END)
            .build());
    private static final Map<String, Art> ART_BY_NAME = Unsafe.getStatic(Art.class, "BY_NAME");
    private static final Map<Integer, Art> ART_BY_ID = Unsafe.getStatic(Art.class, "BY_ID");
    private static final BiMap<ResourceLocation, Statistic> STATS = HashBiMap.create(Unsafe.getStatic(CraftStatistic.class, "statistics"));

    public static void registerAll(DedicatedServer console) {
        loadMaterials();
        loadPotions();
        loadEnchantmentTargets();
        loadEntities();
        loadBiomes(console);
        loadArts(console);
        loadStats();
        loadSpawnCategory();
        loadEndDragonPhase();
        loadCookingBookCategory();
        loadCraftingBookCategory();
        loadRecipeBookType();
        loadFluids();
        loadEffects();
        loadPaperEnumValues();
        loadGameRules();
        try {
            for (var field : org.bukkit.Registry.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.get(null) instanceof org.bukkit.Registry.SimpleRegistry<?> registry) {
                    ((SimpleRegistryBridge) (Object) registry).bridge$reload();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void loadGameRules() {
        Map<String, GameRule<?>> gameRules;
        Constructor<GameRule> constructor;
        try {
            var rules = GameRule.class.getDeclaredField("gameRules");
            rules.setAccessible(true);
            gameRules = (Map<String, GameRule<?>>) rules.get(null);
            constructor = GameRule.class.getDeclaredConstructor(String.class, Class.class);
            constructor.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            ArclightServer.LOGGER.warn("Cannot register all custom game rules for bukkit!", e);
            ArclightServer.LOGGER.warn("This is a bug, and will cause commands like mvgamerule not working properly. Please report this!");
            return;
        }
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                if (!gameRules.containsKey(key.getId())) {
                    Class<?> clazz;
                    var argType = type.createRule();
                    if (argType instanceof GameRules.BooleanValue) {
                        clazz = Boolean.class;
                    } else if (argType instanceof GameRules.IntegerValue) {
                        clazz = Integer.class;
                    } else {
                        clazz = String.class;
                    }
                    try {
                        var instance = constructor.newInstance(key.getId(), clazz);
                        gameRules.put(key.getId(), instance);
                    } catch (ReflectiveOperationException e) {
                        ArclightServer.LOGGER.warn("Cannot register custom game rule {} for bukkit!", key.getId(), e);
                    }
                }
            }
        });
    }

    /**
     * Fusion Core: the {@code Effect} constants Paper adds for 1.21.1 level events (Paper patch "Add missing effects"), with
     * Paper's ids and data types. Plugins reach them through {@code FusionCoreEnumConstants}; {@code Effect.getById} finds them.
     */
    @SuppressWarnings("unchecked")
    private static void loadEffects() {
        var id = org.bukkit.Effect.values().length;
        var newTypes = new ArrayList<org.bukkit.Effect>();
        Map<Integer, org.bukkit.Effect> byId;
        try {
            Field field = org.bukkit.Effect.class.getDeclaredField("BY_ID");
            field.setAccessible(true);
            byId = (Map<Integer, org.bukkit.Effect>) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        for (Object[] paper : new Object[][]{
            effect("WITHER_SPAWNED", 1023, org.bukkit.Effect.Type.SOUND, null),
            effect("ENDER_DRAGON_DEATH", 1028, org.bukkit.Effect.Type.SOUND, null),
            effect("END_PORTAL_CREATED_IN_OVERWORLD", 1038, org.bukkit.Effect.Type.SOUND, null),
            effect("SOUND_STOP_JUKEBOX_SONG", 1011, org.bukkit.Effect.Type.SOUND, null),
            effect("CRAFTER_CRAFT", 1049, org.bukkit.Effect.Type.SOUND, null),
            effect("CRAFTER_FAIL", 1050, org.bukkit.Effect.Type.SOUND, null),
            effect("SHOOT_WHITE_SMOKE", 2010, org.bukkit.Effect.Type.VISUAL, org.bukkit.block.BlockFace.class),
            effect("BEE_GROWTH", 2011, org.bukkit.Effect.Type.VISUAL, Integer.class),
            effect("TURTLE_EGG_PLACEMENT", 2012, org.bukkit.Effect.Type.VISUAL, Integer.class),
            effect("SMASH_ATTACK", 2013, org.bukkit.Effect.Type.VISUAL, Integer.class),
            effect("PARTICLES_SCULK_CHARGE", 3006, org.bukkit.Effect.Type.VISUAL, Integer.class),
            effect("PARTICLES_SCULK_SHRIEK", 3007, org.bukkit.Effect.Type.SOUND, null),
            effect("PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE", 3008, org.bukkit.Effect.Type.VISUAL, org.bukkit.block.data.BlockData.class),
            effect("PARTICLES_EGG_CRACK", 3009, org.bukkit.Effect.Type.VISUAL, null),
            effect("GUST_DUST", 3010, org.bukkit.Effect.Type.VISUAL, null),
            effect("TRIAL_SPAWNER_SPAWN", 3011, org.bukkit.Effect.Type.VISUAL, Boolean.class),
            effect("TRIAL_SPAWNER_SPAWN_MOB_AT", 3012, org.bukkit.Effect.Type.VISUAL, Boolean.class),
            effect("TRIAL_SPAWNER_DETECT_PLAYER", 3013, org.bukkit.Effect.Type.VISUAL, Integer.class),
            effect("TRIAL_SPAWNER_EJECT_ITEM", 3014, org.bukkit.Effect.Type.VISUAL, null),
            effect("VAULT_ACTIVATE", 3015, org.bukkit.Effect.Type.VISUAL, Boolean.class),
            effect("VAULT_DEACTIVATE", 3016, org.bukkit.Effect.Type.VISUAL, Boolean.class),
            effect("VAULT_EJECT_ITEM", 3017, org.bukkit.Effect.Type.VISUAL, null),
            effect("SPAWN_COBWEB", 3018, org.bukkit.Effect.Type.VISUAL, null),
            effect("TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS", 3019, org.bukkit.Effect.Type.VISUAL, Integer.class),
            effect("TRIAL_SPAWNER_BECOME_OMINOUS", 3020, org.bukkit.Effect.Type.VISUAL, Boolean.class),
            effect("TRIAL_SPAWNER_SPAWN_ITEM", 3021, org.bukkit.Effect.Type.VISUAL, Boolean.class),
            effect("SOUND_WITH_CHARGE_SHOT", 1051, org.bukkit.Effect.Type.SOUND, null)
        }) {
            var name = (String) paper[0];
            try {
                org.bukkit.Effect.valueOf(name);
            } catch (IllegalArgumentException e) {
                var effect = EnumHelper.makeEnum(org.bukkit.Effect.class, name, id++, List.of(int.class, org.bukkit.Effect.Type.class, Class.class),
                    Arrays.asList(paper[1], paper[2], paper[3]));
                newTypes.add(effect);
                byId.put((Integer) paper[1], effect);
            }
        }
        EnumHelper.addEnums(org.bukkit.Effect.class, newTypes);
    }

    private static Object[] effect(String name, int id, org.bukkit.Effect.Type type, Class<?> data) {
        return new Object[]{name, id, type, data};
    }

    /**
     * Fusion Core: values Paper adds to Bukkit enums that are not registry-backed. Plugins reach them through
     * {@code FusionCoreEnumConstants}; the core fires the event values where Paper does.
     */
    private static void loadPaperEnumValues() {
        addPaperEnum(org.bukkit.entity.Boat.Status.class, "NOT_IN_WORLD", List.of(), List.of());
        addPaperEnum(org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.class, "OBSTRUCTED", List.of(), List.of());
        addPaperEnum(org.bukkit.event.player.PlayerFishEvent.State.class, "LURED", List.of(), List.of());
        addPaperEnum(org.bukkit.event.entity.EntityTargetEvent.TargetReason.class, "TARGET_OTHER_LEVEL", List.of(), List.of());
        addPaperEnum(org.bukkit.event.entity.EntityTargetEvent.TargetReason.class, "TARGET_INVALID", List.of(), List.of());
        addPaperEnum(org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.class, "OMINOUS_ITEM_SPAWNER", List.of(), List.of());
        addPaperEnum(org.bukkit.event.entity.EntityRemoveEvent.Cause.class, "DISCARD", List.of(), List.of());
        addPaperEnum(org.bukkit.event.inventory.InventoryType.class, "DECORATED_POT",
            List.of(int.class, String.class, org.bukkit.inventory.MenuType.class, boolean.class), Arrays.asList(1, "Decorated Pot", null, false));
        addPaperEnum(org.bukkit.PortalType.class, "END_GATEWAY", List.of(), List.of());
        addPaperEnum(org.bukkit.inventory.ItemFlag.class, "HIDE_STORED_ENCHANTS", List.of(), List.of());
        addPaperEnum(org.bukkit.EntityEffect.class, "ARMADILLO_PEEK", List.of(int.class, Class.class), List.of(64, org.bukkit.entity.Armadillo.class));
        addPaperEnum(org.bukkit.EntityEffect.class, "BODY_BREAK", List.of(int.class, Class.class), List.of(65, org.bukkit.entity.LivingEntity.class));
        addPaperEnum(org.bukkit.MinecraftExperimental.Requires.class, "BUNDLE", List.of(), List.of());
        addPaperEnum(org.bukkit.MinecraftExperimental.Requires.class, "TRADE_REBALANCE", List.of(), List.of());
    }

    private static <E extends Enum<E>> void addPaperEnum(Class<E> type, String name, List<Class<?>> ctorTypes, List<Object> ctorArgs) {
        try {
            Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            var value = EnumHelper.makeEnum(type, name, type.getEnumConstants().length, ctorTypes, ctorArgs);
            EnumHelper.addEnums(type, List.of(value));
            ArclightServer.LOGGER.debug("Registered Paper's {}.{}", type.getSimpleName(), name);
        }
    }

    private static void loadFluids() {
        var id = org.bukkit.Fluid.values().length;
        var newTypes = new ArrayList<org.bukkit.Fluid>();
        Field keyField = Arrays.stream(org.bukkit.Fluid.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(keyField);
        for (var fluidType : BuiltInRegistries.FLUID) {
            var key = BuiltInRegistries.FLUID.getKey(fluidType);
            var name = ResourceLocationUtil.standardize(key);
            try {
                org.bukkit.Fluid.valueOf(name);
            } catch (Exception e) {
                var bukkit = EnumHelper.makeEnum(org.bukkit.Fluid.class, name, id++, List.of(), List.of());
                Unsafe.putObject(bukkit, keyOffset, CraftNamespacedKey.fromMinecraft(key));
                newTypes.add(bukkit);
                ArclightServer.LOGGER.debug("Registered {} as fluid {}", key, bukkit);
            }
        }
        EnumHelper.addEnums(org.bukkit.Fluid.class, newTypes);
    }

    private static void loadCraftingBookCategory() {
        var id = CraftingBookCategory.values().length;
        var newTypes = new ArrayList<org.bukkit.inventory.recipe.CraftingBookCategory>();
        for (CraftingBookCategory category : CraftingBookCategory.values()) {
            try {
                CraftRecipe.getCategory(category);
            } catch (Exception e) {
                var name = category.name();
                var bukkit = EnumHelper.makeEnum(org.bukkit.inventory.recipe.CraftingBookCategory.class, name, id++, List.of(), List.of());
                newTypes.add(bukkit);
                ArclightServer.LOGGER.debug("Registered {} as crafting category {}", name, bukkit);
            }
        }
        EnumHelper.addEnums(org.bukkit.inventory.recipe.CraftingBookCategory.class, newTypes);
    }

    private static void loadCookingBookCategory() {
        var id = CookingBookCategory.values().length;
        var newTypes = new ArrayList<org.bukkit.inventory.recipe.CookingBookCategory>();
        for (CookingBookCategory category : CookingBookCategory.values()) {
            try {
                CraftRecipe.getCategory(category);
            } catch (Exception e) {
                var name = category.name();
                var bukkit = EnumHelper.makeEnum(org.bukkit.inventory.recipe.CookingBookCategory.class, name, id++, List.of(), List.of());
                newTypes.add(bukkit);
                ArclightServer.LOGGER.debug("Registered {} as cooking category {}", name, bukkit);
            }
        }
        EnumHelper.addEnums(org.bukkit.inventory.recipe.CookingBookCategory.class, newTypes);
    }

    private static void loadRecipeBookType() {
        var knownTypes = new ArrayList<String>();
        for (PlayerRecipeBookSettingsChangeEvent.RecipeBookType type : PlayerRecipeBookSettingsChangeEvent.RecipeBookType.values()) {
            knownTypes.add(type.name());
        }
        var newTypes = new ArrayList<PlayerRecipeBookSettingsChangeEvent.RecipeBookType>();
        int id = PlayerRecipeBookSettingsChangeEvent.RecipeBookType.values().length;
        for (RecipeBookType type : RecipeBookType.values()) {
            var name = type.name();
            if (!knownTypes.remove(name)) {
                var bukkit = EnumHelper.makeEnum(PlayerRecipeBookSettingsChangeEvent.RecipeBookType.class, name, id++, List.of(), List.of());
                newTypes.add(bukkit);
                ArclightServer.LOGGER.debug("Registered {} as recipe book type {}", name, bukkit);
            }
        }
        if (!knownTypes.isEmpty()) {
            ArclightServer.LOGGER.fatal("Assertion failed, found unknown recipe book type in Bukkit: {}", knownTypes);
            throw new IllegalArgumentException("Assertion failed, found unknown recipe book type in Bukkit");
        }
        EnumHelper.addEnums(PlayerRecipeBookSettingsChangeEvent.RecipeBookType.class, newTypes);
    }

    private static void loadEndDragonPhase() {
        var max = EnderDragonPhase.getCount();
        var newTypes = new ArrayList<EnderDragon.Phase>();
        for (var id = EnderDragon.Phase.values().length; id < max; id++) {
            var name = "MOD_PHASE_" + id;
            var newPhase = EnumHelper.makeEnum(EnderDragon.Phase.class, name, id, List.of(), List.of());
            newTypes.add(newPhase);
            ArclightServer.LOGGER.debug("Registered {} as ender dragon phase {}", name, newPhase);
        }
        EnumHelper.addEnums(EnderDragon.Phase.class, newTypes);
    }

    private static void loadSpawnCategory() {
        var id = SpawnCategory.values().length;
        var newTypes = new ArrayList<SpawnCategory>();
        for (var category : MobCategory.values()) {
            try {
                CraftSpawnCategory.toBukkit(category);
            } catch (Exception e) {
                var name = category.name();
                var spawnCategory = EnumHelper.makeEnum(SpawnCategory.class, name, id++, List.of(), List.of());
                newTypes.add(spawnCategory);
                ArclightServer.LOGGER.debug("Registered {} as spawn category {}", name, spawnCategory);
            }
        }
        EnumHelper.addEnums(SpawnCategory.class, newTypes);
    }

    private static void loadStats() {
        int i = Statistic.values().length;
        List<Statistic> newTypes = new ArrayList<>();
        Field key = Arrays.stream(Statistic.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(key);
        for (StatType<?> statType : BuiltInRegistries.STAT_TYPE) {
            if (statType == Stats.CUSTOM) continue;
            var location = BuiltInRegistries.STAT_TYPE.getKey(statType);
            Statistic statistic = STATS.get(location);
            if (statistic == null) {
                String standardName = ResourceLocationUtil.standardize(location);
                Statistic.Type type;
                if (statType.getRegistry() == BuiltInRegistries.ENTITY_TYPE) {
                    type = Statistic.Type.ENTITY;
                } else if (statType.getRegistry() == BuiltInRegistries.BLOCK) {
                    type = Statistic.Type.BLOCK;
                } else if (statType.getRegistry() == BuiltInRegistries.ITEM) {
                    type = Statistic.Type.ITEM;
                } else {
                    type = Statistic.Type.UNTYPED;
                }
                statistic = EnumHelper.makeEnum(Statistic.class, standardName, i, ImmutableList.of(Statistic.Type.class), ImmutableList.of(type));
                Unsafe.putObject(statistic, keyOffset, location);
                newTypes.add(statistic);
                STATS.put(location, statistic);
                ArclightServer.LOGGER.debug("Registered {} as stats {}", location, statistic);
                i++;
            }
        }
        for (ResourceLocation location : BuiltInRegistries.CUSTOM_STAT) {
            Statistic statistic = STATS.get(location);
            if (statistic == null) {
                String standardName = ResourceLocationUtil.standardize(location);
                statistic = EnumHelper.makeEnum(Statistic.class, standardName, i, ImmutableList.of(), ImmutableList.of());
                Unsafe.putObject(statistic, keyOffset, location);
                newTypes.add(statistic);
                STATS.put(location, statistic);
                ArclightServer.LOGGER.debug("Registered {} as custom stats {}", location, statistic);
                i++;
            }
        }
        EnumHelper.addEnums(Statistic.class, newTypes);
        putStatic(CraftStatistic.class, "statistics", STATS);
    }

    private static void loadArts(DedicatedServer console) {
        int i = Art.values().length;
        List<Art> newTypes = new ArrayList<>();
        Field key = Arrays.stream(Art.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(key);
        var reg = console.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT);
        for (var paintingType : reg) {
            var location = reg.getKey(paintingType);
            String lookupName = location.getPath().toLowerCase(Locale.ROOT);
            Art bukkit = Art.getByName(lookupName);
            if (bukkit == null) {
                String standardName = ResourceLocationUtil.standardize(location);
                bukkit = EnumHelper.makeEnum(Art.class, standardName, i, ImmutableList.of(int.class, int.class, int.class), ImmutableList.of(i, paintingType.width(), paintingType.height()));
                newTypes.add(bukkit);
                Unsafe.putObject(bukkit, keyOffset, CraftNamespacedKey.fromMinecraft(location));
                ART_BY_ID.put(i, bukkit);
                ART_BY_NAME.put(lookupName, bukkit);
                ArclightServer.LOGGER.debug("Registered {} as art {}", location, bukkit);
                i++;
            }
        }
        EnumHelper.addEnums(Art.class, newTypes);
    }

    private static void loadBiomes(DedicatedServer console) {
        int i = Biome.values().length;
        List<Biome> newTypes = new ArrayList<>();
        Field key = Arrays.stream(Biome.class.getDeclaredFields()).filter(it -> it.getName().equals("key")).findAny().orElse(null);
        long keyOffset = Unsafe.objectFieldOffset(key);
        var registry = console.registryAccess().registryOrThrow(Registries.BIOME);
        for (net.minecraft.world.level.biome.Biome biome : registry) {
            var location = registry.getKey(biome);
            String name = ResourceLocationUtil.standardize(location);
            Biome bukkit;
            try {
                bukkit = Biome.valueOf(name);
            } catch (Throwable t) {
                bukkit = null;
            }
            if (bukkit == null) {
                bukkit = EnumHelper.makeEnum(Biome.class, name, i++, ImmutableList.of(), ImmutableList.of());
                newTypes.add(bukkit);
                Unsafe.putObject(bukkit, keyOffset, CraftNamespacedKey.fromMinecraft(location));
                ArclightServer.LOGGER.debug("Registered {} as biome {}", location, bukkit);
            }
        }
        EnumHelper.addEnums(Biome.class, newTypes);
        ArclightServer.LOGGER.info("registry.biome", newTypes.size());
    }

    public static void registerEnvironments(Registry<LevelStem> registry) {
        int i = World.Environment.values().length;
        List<World.Environment> newTypes = new ArrayList<>();
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            World.Environment environment = DIM_MAP.get(key);
            if (environment == null) {
                String name = ResourceLocationUtil.standardize(key.location());
                environment = EnumHelper.makeEnum(World.Environment.class, name, i, ENV_CTOR, ImmutableList.of(i - 1));
                newTypes.add(environment);
                ENVIRONMENT_MAP.put(i - 1, environment);
                DIM_MAP.put(key, environment);
                ArclightServer.LOGGER.debug("Registered {} as environment {}", key.location(), environment);
                i++;
            }
        }
        EnumHelper.addEnums(World.Environment.class, newTypes);
        ArclightServer.LOGGER.info("registry.environment", newTypes.size());
    }

    private static void loadEntities() {
        int origin = EntityType.values().length;
        int i = origin;
        List<EntityType> newTypes = new ArrayList<>(BuiltInRegistries.ENTITY_TYPE.entrySet().size() - origin + 1); // UNKNOWN
        for (net.minecraft.world.entity.EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation location = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            EntityType entityType = null;
            boolean found = false;
            if (location.getNamespace().equals(NamespacedKey.MINECRAFT)) {
                entityType = EntityType.fromName(location.getPath());
                if (entityType != null) {
                    found = true;
                    ((EntityTypeBridge) (Object) entityType).bridge$setHandle(type);
                }
                else ArclightServer.LOGGER.warn("Not found {} in {}", location, EntityType.class);
            }
            if (!found) {
                String name = ResourceLocationUtil.standardize(location);
                entityType = EnumHelper.makeEnum(EntityType.class, name, i++, ENTITY_CTOR, ImmutableList.of(location.getPath(), Entity.class, -1));
                ((EntityTypeBridge) (Object) entityType).bridge$setup(location, type, entitySpec(location));
                newTypes.add(entityType);
                ArclightServer.LOGGER.debug("Registered {} as entity {}", location, entityType);
            }
            ENTITY_NAME_MAP.put(location.toString(), entityType);
        }
        EnumHelper.addEnums(EntityType.class, newTypes);
        EntityClassLookup.init();
        ArclightServer.LOGGER.info("registry.entity-type", newTypes.size());
    }

    private static void loadEnchantmentTargets() {
        int origin = EnchantmentTarget.values().length;
        // TODO
    }

    private static void loadPotions() {
        int typeId = PotionType.values().length;
        List<PotionType> newTypes = new ArrayList<>();
        for (var potion : BuiltInRegistries.POTION) {
            var location = BuiltInRegistries.POTION.getKey(potion);
            String name = ResourceLocationUtil.standardize(location);
            try {
                PotionType.valueOf(name);
            } catch (Exception e) {
                PotionType potionType = EnumHelper.makeEnum(PotionType.class, name, typeId++,
                    List.of(String.class),
                    List.of(location.toString()));
                newTypes.add(potionType);
                ArclightServer.LOGGER.debug("Registered {} as potion type {}", location, potionType);
            }
        }
        EnumHelper.addEnums(PotionType.class, newTypes);
    }

    private static void loadMaterials() {
        int blocks = 0, items = 0;
        int i = Material.values().length;
        int origin = i;
        List<Material> list = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);
            String name = ResourceLocationUtil.standardize(location);
            Material material = BY_NAME.get(name);
            if (material == null) {
                material = EnumHelper.makeEnum(Material.class, name, i, MAT_CTOR, ImmutableList.of(i));
                ((MaterialBridge) (Object) material).bridge$setupBlock(location, matSpec(location));
                BY_NAME.put(name, material);
                i++;
                blocks++;
                ArclightServer.LOGGER.debug("Registered {} as block {}", location, material);
                list.add(material);
            } else {
                ((MaterialBridge) (Object) material).bridge$setupVanillaBlock(matSpec(location));
            }
            BLOCK_MATERIAL.put(block, material);
            MATERIAL_BLOCK.put(material, block);
            Item value = BuiltInRegistries.ITEM.get(location);
            if (value != null && value != Items.AIR) {
                ((MaterialBridge) (Object) material).bridge$setItem();
                ITEM_MATERIAL.put(value, material);
                MATERIAL_ITEM.put(material, value);
            }
        }
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation location = BuiltInRegistries.ITEM.getKey(item);
            String name = ResourceLocationUtil.standardize(location);
            Material material = BY_NAME.get(name);
            if (material == null) {
                material = EnumHelper.makeEnum(Material.class, name, i, MAT_CTOR, ImmutableList.of(i));
                ((MaterialBridge) (Object) material).bridge$setupItem(location, matSpec(location));
                BY_NAME.put(name, material);
                i++;
                items++;
                ArclightServer.LOGGER.debug("Registered {} as item {}", location, material);
                list.add(material);
            }
            ITEM_MATERIAL.put(item, material);
            MATERIAL_ITEM.put(material, item);
            Block value = BuiltInRegistries.BLOCK.get(location);
            if (value != null && value != Blocks.AIR) {
                ((MaterialBridge) (Object) material).bridge$setBlock();
                BLOCK_MATERIAL.put(value, material);
                MATERIAL_BLOCK.put(material, value);
            }
        }
        EnumHelper.addEnums(Material.class, list);
        ArclightServer.LOGGER.info("registry.material", i - origin, blocks, items);
    }

    private static MaterialPropertySpec matSpec(ResourceLocation location) {
        return ArclightConfig.spec().getCompat().getMaterial(location.toString()).orElse(MaterialPropertySpec.EMPTY);
    }

    private static EntityPropertySpec entitySpec(ResourceLocation location) {
        return ArclightConfig.spec().getCompat().getEntity(location.toString()).orElse(EntityPropertySpec.EMPTY);
    }

    public static Pose toBukkitPose(net.minecraft.world.entity.Pose nms) {
        if (Pose.values().length <= nms.ordinal()) {
            var newTypes = new ArrayList<Pose>();
            var forgeCount = net.minecraft.world.entity.Pose.values().length;
            for (var id = Pose.values().length; id < forgeCount; id++) {
                var name = net.minecraft.world.entity.Pose.values()[id].name();
                var newPhase = EnumHelper.makeEnum(Pose.class, name, id, List.of(), List.of());
                newTypes.add(newPhase);
                ArclightServer.LOGGER.debug("Registered {} as pose {}", name, newPhase);
            }
            EnumHelper.addEnums(Pose.class, newTypes);
        }
        return org.bukkit.entity.Pose.values()[nms.ordinal()];
    }

    private static void putStatic(Class<?> cl, String name, Object o) {
        try {
            Unsafe.ensureClassInitialized(cl);
            Field field = cl.getDeclaredField(name);
            Object materialByNameBase = Unsafe.staticFieldBase(field);
            long materialByNameOffset = Unsafe.staticFieldOffset(field);
            Unsafe.putObject(materialByNameBase, materialByNameOffset, o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void putBool(Class<?> cl, String name, boolean b) {
        try {
            Unsafe.ensureClassInitialized(cl);
            Field field = cl.getDeclaredField(name);
            Object materialByNameBase = Unsafe.staticFieldBase(field);
            long materialByNameOffset = Unsafe.staticFieldOffset(field);
            Unsafe.putBoolean(materialByNameBase, materialByNameOffset, b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
