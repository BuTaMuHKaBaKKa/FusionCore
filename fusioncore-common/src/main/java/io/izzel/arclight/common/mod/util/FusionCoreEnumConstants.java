package io.izzel.arclight.common.mod.util;

/**
 * Fusion Core: enum constants Paper adds to Bukkit enums; {@code PaperCallTransformer} rewrites plugin reads to these methods.
 */
public final class FusionCoreEnumConstants {

    private FusionCoreEnumConstants() {
    }

    /** Paper's {@code Fluid.EMPTY} ({@code minecraft:empty}). */
    public static org.bukkit.Fluid Fluid$EMPTY() {
        return org.bukkit.Fluid.valueOf("EMPTY");
    }

    // Paper's Effect constants (Paper patch "Add missing effects"), registered by BukkitRegistry#loadEffects

    public static org.bukkit.Effect Effect$WITHER_SPAWNED() {
        return org.bukkit.Effect.valueOf("WITHER_SPAWNED");
    }

    public static org.bukkit.Effect Effect$ENDER_DRAGON_DEATH() {
        return org.bukkit.Effect.valueOf("ENDER_DRAGON_DEATH");
    }

    public static org.bukkit.Effect Effect$END_PORTAL_CREATED_IN_OVERWORLD() {
        return org.bukkit.Effect.valueOf("END_PORTAL_CREATED_IN_OVERWORLD");
    }

    public static org.bukkit.Effect Effect$SOUND_STOP_JUKEBOX_SONG() {
        return org.bukkit.Effect.valueOf("SOUND_STOP_JUKEBOX_SONG");
    }

    public static org.bukkit.Effect Effect$CRAFTER_CRAFT() {
        return org.bukkit.Effect.valueOf("CRAFTER_CRAFT");
    }

    public static org.bukkit.Effect Effect$CRAFTER_FAIL() {
        return org.bukkit.Effect.valueOf("CRAFTER_FAIL");
    }

    public static org.bukkit.Effect Effect$SHOOT_WHITE_SMOKE() {
        return org.bukkit.Effect.valueOf("SHOOT_WHITE_SMOKE");
    }

    public static org.bukkit.Effect Effect$BEE_GROWTH() {
        return org.bukkit.Effect.valueOf("BEE_GROWTH");
    }

    public static org.bukkit.Effect Effect$TURTLE_EGG_PLACEMENT() {
        return org.bukkit.Effect.valueOf("TURTLE_EGG_PLACEMENT");
    }

    public static org.bukkit.Effect Effect$SMASH_ATTACK() {
        return org.bukkit.Effect.valueOf("SMASH_ATTACK");
    }

    public static org.bukkit.Effect Effect$PARTICLES_SCULK_CHARGE() {
        return org.bukkit.Effect.valueOf("PARTICLES_SCULK_CHARGE");
    }

    public static org.bukkit.Effect Effect$PARTICLES_SCULK_SHRIEK() {
        return org.bukkit.Effect.valueOf("PARTICLES_SCULK_SHRIEK");
    }

    public static org.bukkit.Effect Effect$PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE() {
        return org.bukkit.Effect.valueOf("PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE");
    }

    public static org.bukkit.Effect Effect$PARTICLES_EGG_CRACK() {
        return org.bukkit.Effect.valueOf("PARTICLES_EGG_CRACK");
    }

    public static org.bukkit.Effect Effect$GUST_DUST() {
        return org.bukkit.Effect.valueOf("GUST_DUST");
    }

    public static org.bukkit.Effect Effect$TRIAL_SPAWNER_SPAWN() {
        return org.bukkit.Effect.valueOf("TRIAL_SPAWNER_SPAWN");
    }

    public static org.bukkit.Effect Effect$TRIAL_SPAWNER_SPAWN_MOB_AT() {
        return org.bukkit.Effect.valueOf("TRIAL_SPAWNER_SPAWN_MOB_AT");
    }

    public static org.bukkit.Effect Effect$TRIAL_SPAWNER_DETECT_PLAYER() {
        return org.bukkit.Effect.valueOf("TRIAL_SPAWNER_DETECT_PLAYER");
    }

    public static org.bukkit.Effect Effect$TRIAL_SPAWNER_EJECT_ITEM() {
        return org.bukkit.Effect.valueOf("TRIAL_SPAWNER_EJECT_ITEM");
    }

    public static org.bukkit.Effect Effect$VAULT_ACTIVATE() {
        return org.bukkit.Effect.valueOf("VAULT_ACTIVATE");
    }

    public static org.bukkit.Effect Effect$VAULT_DEACTIVATE() {
        return org.bukkit.Effect.valueOf("VAULT_DEACTIVATE");
    }

    public static org.bukkit.Effect Effect$VAULT_EJECT_ITEM() {
        return org.bukkit.Effect.valueOf("VAULT_EJECT_ITEM");
    }

    public static org.bukkit.Effect Effect$SPAWN_COBWEB() {
        return org.bukkit.Effect.valueOf("SPAWN_COBWEB");
    }

    public static org.bukkit.Effect Effect$TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS() {
        return org.bukkit.Effect.valueOf("TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS");
    }

    public static org.bukkit.Effect Effect$TRIAL_SPAWNER_BECOME_OMINOUS() {
        return org.bukkit.Effect.valueOf("TRIAL_SPAWNER_BECOME_OMINOUS");
    }

    public static org.bukkit.Effect Effect$TRIAL_SPAWNER_SPAWN_ITEM() {
        return org.bukkit.Effect.valueOf("TRIAL_SPAWNER_SPAWN_ITEM");
    }

    public static org.bukkit.Effect Effect$SOUND_WITH_CHARGE_SHOT() {
        return org.bukkit.Effect.valueOf("SOUND_WITH_CHARGE_SHOT");
    }

    /**
     * Marks a getter whose value is not of the owner's type (a static field such as {@code DisplaySlot.NAMES}): the owner
     * is named here, the method name keeps the {@code <SimpleName>$<NAME>} form.
     */
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
    public @interface Of {
        Class<?> value();
    }

    // Paper renamed the team sidebar slots (SIDEBAR_BLACK -> SIDEBAR_TEAM_BLACK); the same constants here

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_BLACK() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_BLACK;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_DARK_BLUE() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_DARK_BLUE;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_DARK_GREEN() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_DARK_GREEN;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_DARK_AQUA() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_DARK_AQUA;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_DARK_RED() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_DARK_RED;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_DARK_PURPLE() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_DARK_PURPLE;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_GOLD() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_GOLD;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_GRAY() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_GRAY;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_DARK_GRAY() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_DARK_GRAY;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_BLUE() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_BLUE;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_GREEN() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_GREEN;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_AQUA() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_AQUA;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_RED() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_RED;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_LIGHT_PURPLE() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_LIGHT_PURPLE;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_YELLOW() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_YELLOW;
    }

    public static org.bukkit.scoreboard.DisplaySlot DisplaySlot$SIDEBAR_TEAM_WHITE() {
        return org.bukkit.scoreboard.DisplaySlot.SIDEBAR_WHITE;
    }

    private static net.kyori.adventure.util.Index<String, org.bukkit.scoreboard.DisplaySlot> displaySlotNames;

    /** Paper's {@code DisplaySlot.NAMES}: slots by their vanilla id ({@code DisplaySlot#getId}). */
    @Of(org.bukkit.scoreboard.DisplaySlot.class)
    public static synchronized net.kyori.adventure.util.Index<String, org.bukkit.scoreboard.DisplaySlot> DisplaySlot$NAMES() {
        if (displaySlotNames == null) {
            displaySlotNames = net.kyori.adventure.util.Index.create(org.bukkit.scoreboard.DisplaySlot.class, FusionCoreScoreboards::slotId);
        }
        return displaySlotNames;
    }

    // Paper's names for the taming effects, Spigot's WOLF_SMOKE/WOLF_HEARTS (entity events 6 and 7)

    public static org.bukkit.EntityEffect EntityEffect$TAMING_FAILED() {
        return org.bukkit.EntityEffect.WOLF_SMOKE;
    }

    public static org.bukkit.EntityEffect EntityEffect$TAMING_SUCCEEDED() {
        return org.bukkit.EntityEffect.WOLF_HEARTS;
    }

    /** Paper: {@code ItemFlag.HIDE_ITEM_SPECIFICS} is an alias of {@code HIDE_ADDITIONAL_TOOLTIP}. */
    public static org.bukkit.inventory.ItemFlag ItemFlag$HIDE_ITEM_SPECIFICS() {
        return org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
    }

    private static net.kyori.adventure.util.Index<String, org.bukkit.FireworkEffect.Type> fireworkTypeNames;

    /** Paper's {@code FireworkEffect.Type.NAMES}: shapes by vanilla's shape name ({@code small_ball}, {@code large_ball}, ...). */
    @Of(org.bukkit.FireworkEffect.Type.class)
    public static synchronized net.kyori.adventure.util.Index<String, org.bukkit.FireworkEffect.Type> Type$NAMES() {
        if (fireworkTypeNames == null) {
            fireworkTypeNames = net.kyori.adventure.util.Index.create(org.bukkit.FireworkEffect.Type.class, type -> switch (type.name()) {
                case "BALL" -> "small_ball";
                case "BALL_LARGE" -> "large_ball";
                default -> type.name().toLowerCase(java.util.Locale.ENGLISH);
            });
        }
        return fireworkTypeNames;
    }

    /** Paper's {@code EventExecutor.eventExecutorMap}: the generated executor classes of {@code EventExecutor.create}. */
    @Of(org.bukkit.plugin.EventExecutor.class)
    public static java.util.concurrent.ConcurrentMap<java.lang.reflect.Method, Class<? extends org.bukkit.plugin.EventExecutor>> EventExecutor$eventExecutorMap() {
        return FusionCoreEventExecutorStatics.EVENT_EXECUTOR_MAP;
    }

    // Paper's values of Bukkit enums, registered by BukkitRegistry#loadPaperEnumValues

    public static org.bukkit.entity.Boat.Status Status$NOT_IN_WORLD() {
        return org.bukkit.entity.Boat.Status.valueOf("NOT_IN_WORLD");
    }

    public static org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult BedEnterResult$OBSTRUCTED() {
        return org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.valueOf("OBSTRUCTED");
    }

    public static org.bukkit.event.player.PlayerFishEvent.State State$LURED() {
        return org.bukkit.event.player.PlayerFishEvent.State.valueOf("LURED");
    }

    public static org.bukkit.event.entity.EntityTargetEvent.TargetReason TargetReason$TARGET_OTHER_LEVEL() {
        return org.bukkit.event.entity.EntityTargetEvent.TargetReason.valueOf("TARGET_OTHER_LEVEL");
    }

    public static org.bukkit.event.entity.EntityTargetEvent.TargetReason TargetReason$TARGET_INVALID() {
        return org.bukkit.event.entity.EntityTargetEvent.TargetReason.valueOf("TARGET_INVALID");
    }

    public static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason SpawnReason$OMINOUS_ITEM_SPAWNER() {
        return org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.valueOf("OMINOUS_ITEM_SPAWNER");
    }

    public static org.bukkit.event.entity.EntityRemoveEvent.Cause Cause$DISCARD() {
        return org.bukkit.event.entity.EntityRemoveEvent.Cause.valueOf("DISCARD");
    }

    public static org.bukkit.event.inventory.InventoryType InventoryType$DECORATED_POT() {
        return org.bukkit.event.inventory.InventoryType.valueOf("DECORATED_POT");
    }

    public static org.bukkit.PortalType PortalType$END_GATEWAY() {
        return org.bukkit.PortalType.valueOf("END_GATEWAY");
    }

    public static org.bukkit.inventory.ItemFlag ItemFlag$HIDE_STORED_ENCHANTS() {
        return org.bukkit.inventory.ItemFlag.valueOf("HIDE_STORED_ENCHANTS");
    }

    public static org.bukkit.EntityEffect EntityEffect$ARMADILLO_PEEK() {
        return org.bukkit.EntityEffect.valueOf("ARMADILLO_PEEK");
    }

    public static org.bukkit.EntityEffect EntityEffect$BODY_BREAK() {
        return org.bukkit.EntityEffect.valueOf("BODY_BREAK");
    }

    public static org.bukkit.MinecraftExperimental.Requires Requires$BUNDLE() {
        return org.bukkit.MinecraftExperimental.Requires.valueOf("BUNDLE");
    }

    public static org.bukkit.MinecraftExperimental.Requires Requires$TRADE_REBALANCE() {
        return org.bukkit.MinecraftExperimental.Requires.valueOf("TRADE_REBALANCE");
    }

    /** Paper's {@code EquipmentSlotGroup.BODY}, registered by {@code EquipmentSlotGroupMixin}. */
    public static org.bukkit.inventory.EquipmentSlotGroup EquipmentSlotGroup$BODY() {
        return org.bukkit.inventory.EquipmentSlotGroup.getByName("body");
    }

    // Paper's game event tags (patch "Add GameEvent tags"), served by CraftServerTagsMixin

    private static org.bukkit.Tag<org.bukkit.GameEvent> gameEventVibrations;
    private static org.bukkit.Tag<org.bukkit.GameEvent> gameEventIgnoreVibrationsSneaking;
    private static org.bukkit.Tag<org.bukkit.GameEvent> gameEventAllayCanListen;

    private static org.bukkit.Tag<org.bukkit.GameEvent> gameEventTag(String name) {
        return org.bukkit.Bukkit.getTag("game_events", org.bukkit.NamespacedKey.minecraft(name), org.bukkit.GameEvent.class);
    }

    /** Paper's {@code Tag.REGISTRY_GAME_EVENTS}; a compile-time constant, so plugins normally inline it. */
    /** Paper's {@code Registry.POTION_EFFECT_TYPE}, an alias of {@code Registry.EFFECT}. */
    @Of(org.bukkit.Registry.class)
    public static org.bukkit.Registry<org.bukkit.potion.PotionEffectType> Registry$POTION_EFFECT_TYPE() {
        return org.bukkit.Registry.EFFECT;
    }

    @Of(org.bukkit.Tag.class)
    public static String Tag$REGISTRY_GAME_EVENTS() {
        return "game_events";
    }

    public static synchronized org.bukkit.Tag<org.bukkit.GameEvent> Tag$GAME_EVENT_VIBRATIONS() {
        if (gameEventVibrations == null) {
            gameEventVibrations = gameEventTag("vibrations");
        }
        return gameEventVibrations;
    }

    public static synchronized org.bukkit.Tag<org.bukkit.GameEvent> Tag$GAME_EVENT_IGNORE_VIBRATIONS_SNEAKING() {
        if (gameEventIgnoreVibrationsSneaking == null) {
            gameEventIgnoreVibrationsSneaking = gameEventTag("ignore_vibrations_sneaking");
        }
        return gameEventIgnoreVibrationsSneaking;
    }

    public static synchronized org.bukkit.Tag<org.bukkit.GameEvent> Tag$GAME_EVENT_ALLAY_CAN_LISTEN() {
        if (gameEventAllayCanListen == null) {
            gameEventAllayCanListen = gameEventTag("allay_can_listen");
        }
        return gameEventAllayCanListen;
    }

    private static net.kyori.adventure.util.Index<net.kyori.adventure.key.Key, org.bukkit.FeatureFlag> allFeatureFlags;

    /** Paper's {@code FeatureFlag.ALL_FLAGS}, taken from vanilla's flag registry so mod flags are included. */
    @Of(org.bukkit.FeatureFlag.class)
    public static synchronized net.kyori.adventure.util.Index<net.kyori.adventure.key.Key, org.bukkit.FeatureFlag> FeatureFlag$ALL_FLAGS() {
        if (allFeatureFlags == null) {
            java.util.Map<org.bukkit.NamespacedKey, org.bukkit.FeatureFlag> constants = new java.util.HashMap<>();
            for (java.lang.reflect.Field field : org.bukkit.FeatureFlag.class.getFields()) {
                try {
                    if (field.get(null) instanceof org.bukkit.FeatureFlag flag) {
                        constants.put(flag.getKey(), flag);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
            java.util.List<org.bukkit.FeatureFlag> flags = new java.util.ArrayList<>();
            net.minecraft.world.flag.FeatureFlagRegistry registry = net.minecraft.world.flag.FeatureFlags.REGISTRY;
            for (net.minecraft.resources.ResourceLocation name : registry.toNames(registry.allFlags())) {
                org.bukkit.NamespacedKey key = org.bukkit.craftbukkit.v.util.CraftNamespacedKey.fromMinecraft(name);
                org.bukkit.FeatureFlag flag = constants.get(key);
                flags.add(flag != null ? flag : org.bukkit.Bukkit.getUnsafe().getFeatureFlag(key));
            }
            allFeatureFlags = net.kyori.adventure.util.Index.create(flag -> (net.kyori.adventure.key.Key) (Object) flag.getKey(), flags);
        }
        return allFeatureFlags;
    }
}
