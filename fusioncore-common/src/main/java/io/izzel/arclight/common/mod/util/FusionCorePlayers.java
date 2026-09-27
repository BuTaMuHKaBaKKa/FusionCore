package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.SkinParts;
import io.izzel.arclight.common.bridge.core.server.level.ServerLevelBridge;
import io.izzel.arclight.common.mixin.core.world.entity.monster.warden.WardenSpawnTrackerAccessor;
import io.izzel.arclight.common.mixin.core.world.entity.player.PlayerPaperAccessor;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.InventoryView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Fusion Core: bodies of Paper's smaller Player/HumanEntity/OfflinePlayer API. */
public final class FusionCorePlayers {

    private FusionCorePlayers() {
    }

    public static ServerPlayer handle(Object player) {
        return ((CraftPlayer) player).getHandle();
    }

    private static net.minecraft.world.entity.player.Player human(Object human) {
        return ((CraftHumanEntity) human).getHandle();
    }

    // HumanEntity

    /** Paper patch 0716. */
    public static org.bukkit.entity.FishHook fishHook(HumanEntity human) {
        var hook = human(human).fishing;
        return hook == null ? null : (org.bukkit.entity.FishHook) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) hook).bridge$getBukkitEntity();
    }

    /** Paper patch 0408: opens the block's menu at the location; without force the block there must be that block. */
    public static InventoryView openBlockMenu(HumanEntity human, Location location, boolean force, Material material) {
        org.spigotmc.AsyncCatcher.catchOp("open" + material);
        if (location == null) {
            location = human.getLocation();
        }
        if (!force && location.getBlock().getType() != material) {
            return null;
        }
        Block block = switch (material) {
            case ANVIL -> Blocks.ANVIL;
            case CARTOGRAPHY_TABLE -> Blocks.CARTOGRAPHY_TABLE;
            case GRINDSTONE -> Blocks.GRINDSTONE;
            case LOOM -> Blocks.LOOM;
            case SMITHING_TABLE -> Blocks.SMITHING_TABLE;
            case STONECUTTER -> Blocks.STONECUTTER;
            default -> throw new IllegalArgumentException("Unsupported inventory type: " + material);
        };
        var handle = human(human);
        var menuBefore = handle.containerMenu;
        handle.openMenu(block.defaultBlockState().getMenuProvider(handle.level(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
        if (handle.containerMenu == menuBefore) {
            return null;
        }
        ((io.izzel.arclight.common.bridge.core.world.inventory.AbstractContainerMenuBridge) handle.containerMenu).fusioncore$setCheckReachable(!force);
        return ((io.izzel.arclight.common.bridge.core.world.inventory.AbstractContainerMenuBridge) handle.containerMenu).bridge$getBukkitView();
    }

    /**
     * Paper patch 0138: puts the shoulder entity back into the world, as the vanilla dismount does (spawn reason
     * SHOULDER_ENTITY); the shoulder is cleared only when the entity was added.
     */
    public static org.bukkit.entity.Entity releaseShoulder(HumanEntity human, boolean left) {
        var handle = human(human);
        CompoundTag tag = left ? handle.getShoulderEntityLeft() : handle.getShoulderEntityRight();
        if (tag.isEmpty() || handle.level().isClientSide) {
            return null;
        }
        Optional<net.minecraft.world.entity.Entity> created = EntityType.create(tag, handle.level());
        if (created.isEmpty()) {
            return null;
        }
        net.minecraft.world.entity.Entity entity = created.get();
        if (entity instanceof TamableAnimal tamable) {
            tamable.setOwnerUUID(handle.getUUID());
        }
        entity.setPos(handle.getX(), handle.getY() + 0.699999988079071, handle.getZ());
        if (!((ServerLevelBridge) handle.level()).bridge$addEntitySerialized(entity, CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY)) {
            return null;
        }
        if (left) {
            handle.setShoulderEntityLeft(new CompoundTag());
        } else {
            handle.setShoulderEntityRight(new CompoundTag());
        }
        return ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) entity).bridge$getBukkitEntity();
    }

    public static void setHurtDirection(HumanEntity human, float hurtDirection) {
        ((PlayerPaperAccessor) human(human)).fusioncore$setHurtDir(hurtDirection);
    }

    // Player

    /** Paper patch 0163: the experience left after mending, firing PlayerItemMendEvent as an orb would. */
    public static int applyMending(Player player, int amount) {
        ServerPlayer handle = handle(player);
        var stackEntry = net.minecraft.world.item.enchantment.EnchantmentHelper.getRandomItemWith(
            net.minecraft.world.item.enchantment.EnchantmentEffectComponents.REPAIR_WITH_XP, handle, net.minecraft.world.item.ItemStack::isDamaged);
        net.minecraft.world.item.ItemStack itemstack = stackEntry.map(net.minecraft.world.item.enchantment.EnchantedItemInUse::itemStack)
            .orElse(net.minecraft.world.item.ItemStack.EMPTY);
        if (!itemstack.isEmpty() && itemstack.has(net.minecraft.core.component.DataComponents.MAX_DAMAGE)) {
            net.minecraft.world.entity.ExperienceOrb orb = EntityType.EXPERIENCE_ORB.create(handle.level());
            orb.value = amount;
            orb.setPosRaw(handle.getX(), handle.getY(), handle.getZ());
            int possible = net.minecraft.world.item.enchantment.EnchantmentHelper.modifyDurabilityToRepairFromXp(handle.serverLevel(), itemstack, amount);
            int repair = Math.min(possible, itemstack.getDamageValue());
            // Paper's consumed experience (patch 0815)
            org.bukkit.event.player.PlayerItemMendEvent event = FusionCoreEvents.callPlayerItemMend(handle, orb, itemstack, stackEntry.get().inSlot(), repair,
                possible > 0 ? repair * amount / possible : 0);
            repair = event.getRepairAmount();
            orb.discard();
            if (!event.isCancelled() && possible > 0) {
                amount -= repair * amount / possible;
                itemstack.setDamageValue(itemstack.getDamageValue() - repair);
            }
        }
        return amount;
    }

    /** Paper patch 0340. */
    @SuppressWarnings("unchecked")
    public static <T> T clientOption(Player player, ClientOption<T> type) {
        ServerPlayer handle = handle(player);
        Object value;
        if (ClientOption.SKIN_PARTS == type) {
            value = new SkinPartsImpl(handle.getEntityData().get(PlayerPaperAccessor.fusioncore$modeCustomisation()));
        } else if (ClientOption.CHAT_COLORS_ENABLED == type) {
            value = handle.canChatInColor();
        } else if (ClientOption.CHAT_VISIBILITY == type) {
            value = handle.getChatVisibility() == null ? ClientOption.ChatVisibility.UNKNOWN : ClientOption.ChatVisibility.valueOf(handle.getChatVisibility().name());
        } else if (ClientOption.LOCALE == type) {
            value = player.getLocale();
        } else if (ClientOption.MAIN_HAND == type) {
            value = player.getMainHand();
        } else if (ClientOption.VIEW_DISTANCE == type) {
            value = player.getClientViewDistance();
        } else if (ClientOption.ALLOW_SERVER_LISTINGS == type) {
            value = handle.allowsListing();
        } else if (ClientOption.TEXT_FILTERING_ENABLED == type) {
            value = handle.isTextFilteringEnabled();
        } else {
            throw new RuntimeException("Unknown settings type");
        }
        return type.getType().cast(value);
    }

    /**
     * Paper's PlayerClientOptionsChangeEvent (patch 0340), fired before the new options are applied so that its
     * {@code has…Changed} methods compare against the old ones. Not fired for the options a player joins with.
     */
    public static void clientOptionsChanged(ServerPlayer handle, net.minecraft.server.level.ClientInformation info) {
        if (!FusionCorePaperMobEvents.listened(com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent.getHandlerList())) {
            return;
        }
        java.util.Map<ClientOption<?>, Object> options = new java.util.HashMap<>();
        options.put(ClientOption.LOCALE, info.language());
        options.put(ClientOption.VIEW_DISTANCE, info.viewDistance());
        options.put(ClientOption.CHAT_VISIBILITY, ClientOption.ChatVisibility.valueOf(info.chatVisibility().name()));
        options.put(ClientOption.CHAT_COLORS_ENABLED, info.chatColors());
        options.put(ClientOption.SKIN_PARTS, new SkinPartsImpl(info.modelCustomisation()));
        options.put(ClientOption.MAIN_HAND, info.mainHand() == net.minecraft.world.entity.HumanoidArm.LEFT ? org.bukkit.inventory.MainHand.LEFT : org.bukkit.inventory.MainHand.RIGHT);
        options.put(ClientOption.ALLOW_SERVER_LISTINGS, info.allowsListing());
        options.put(ClientOption.TEXT_FILTERING_ENABLED, info.textFilteringEnabled());
        org.bukkit.Bukkit.getPluginManager().callEvent(new com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent(
            (Player) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) handle).bridge$getBukkitEntity(), options));
    }

    /** Paper's {@code PaperSkinParts}: the skin-part bits the client sent. */
    private record SkinPartsImpl(int raw) implements SkinParts {
        @Override public boolean hasCapeEnabled() { return (raw & 1) == 1; }
        @Override public boolean hasJacketEnabled() { return (raw >> 1 & 1) == 1; }
        @Override public boolean hasLeftSleeveEnabled() { return (raw >> 2 & 1) == 1; }
        @Override public boolean hasRightSleeveEnabled() { return (raw >> 3 & 1) == 1; }
        @Override public boolean hasLeftPantsEnabled() { return (raw >> 4 & 1) == 1; }
        @Override public boolean hasRightPantsEnabled() { return (raw >> 5 & 1) == 1; }
        @Override public boolean hasHatsEnabled() { return (raw >> 6 & 1) == 1; }
        @Override public int getRaw() { return raw; }
    }

    /** Paper patch 0470: the op level the client believes it has; permissions and commands are not recalculated. */
    public static void sendOpLevel(Player player, byte level) {
        com.google.common.base.Preconditions.checkArgument(level >= 0 && level <= 4, "Level must be within [0, 4]");
        ServerPlayer handle = handle(player);
        if (handle.connection != null) {
            handle.connection.send(new ClientboundEntityEventPacket(handle, (byte) (24 + level)));
        }
    }

    /** Paper patch 0653: one section packet per touched section. */
    public static void sendMultiBlockChange(Player player, Map<? extends io.papermc.paper.math.Position, BlockData> blockChanges) {
        ServerPlayer handle = handle(player);
        if (handle.connection == null) {
            return;
        }
        Map<SectionPos, Short2ObjectMap<BlockState>> sections = new HashMap<>();
        for (Map.Entry<? extends io.papermc.paper.math.Position, BlockData> entry : blockChanges.entrySet()) {
            io.papermc.paper.math.Position pos = entry.getKey();
            BlockPos blockPos = new BlockPos(pos.blockX(), pos.blockY(), pos.blockZ());
            sections.computeIfAbsent(SectionPos.of(blockPos), k -> new Short2ObjectArrayMap<>())
                .put(SectionPos.sectionRelativePos(blockPos), ((CraftBlockData) entry.getValue()).getState());
        }
        for (Map.Entry<SectionPos, Short2ObjectMap<BlockState>> entry : sections.entrySet()) {
            // vanilla builds this packet from a chunk section only; write Paper's contents in the wire format instead
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeLong(entry.getKey().asLong());
            buf.writeVarInt(entry.getValue().size());
            for (Short2ObjectMap.Entry<BlockState> block : entry.getValue().short2ObjectEntrySet()) {
                buf.writeVarLong((long) Block.getId(block.getValue()) << 12 | block.getShortKey());
            }
            handle.connection.send(ClientboundSectionBlocksUpdatePacket.STREAM_CODEC.decode(buf));
        }
    }

    public static void chatCompletions(Player player, Collection<String> completions, boolean add) {
        ServerPlayer handle = handle(player);
        if (handle.connection != null) {
            handle.connection.send(new net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket(
                add ? net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket.Action.ADD
                    : net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket.Action.REMOVE,
                new ArrayList<>(completions)));
        }
    }

    public static void gameEvent(Player player, ClientboundGameEventPacket.Type event, float param) {
        ServerPlayer handle = handle(player);
        if (handle.connection != null) {
            handle.connection.send(new ClientboundGameEventPacket(event, param));
        }
    }

    /** Paper patch 0763. The tracker exists on every player; mods could in theory take it away. */
    public static WardenSpawnTracker warden(Player player) {
        return handle(player).getWardenSpawnTracker().orElseThrow();
    }

    public static WardenSpawnTrackerAccessor wardenAccess(Player player) {
        return (WardenSpawnTrackerAccessor) warden(player);
    }

    /** Paper patch 0862: the tab-list "listed" flag of another player, sent as the vanilla UPDATE_LISTED action. */
    public static void sendListed(Player viewer, UUID other, boolean listed) {
        ServerPlayer handle = handle(viewer);
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), handle.registryAccess());
        buf.writeEnumSet(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED), ClientboundPlayerInfoUpdatePacket.Action.class);
        buf.writeVarInt(1);
        buf.writeUUID(other);
        buf.writeBoolean(listed);
        handle.connection.send(ClientboundPlayerInfoUpdatePacket.STREAM_CODEC.decode(buf));
    }

    /** Info packets to {@code viewer}: entries of players it unlisted go out unlisted (a copy; broadcasts share the packet). */
    public static ClientboundPlayerInfoUpdatePacket keepUnlisted(ServerPlayer viewer, ClientboundPlayerInfoUpdatePacket packet) {
        if (!packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED)) {
            return packet;
        }
        java.util.Set<UUID> unlisted = ((io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge)
            ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) viewer).bridge$getBukkitEntity()).fusioncore$unlisted();
        if (unlisted.isEmpty()) {
            return packet;
        }
        java.util.List<ClientboundPlayerInfoUpdatePacket.Entry> entries = null;
        for (int i = 0; i < packet.entries().size(); i++) {
            ClientboundPlayerInfoUpdatePacket.Entry entry = packet.entries().get(i);
            if (entry.listed() && unlisted.contains(entry.profileId())) {
                if (entries == null) {
                    entries = new ArrayList<>(packet.entries());
                }
                entries.set(i, new ClientboundPlayerInfoUpdatePacket.Entry(entry.profileId(), entry.profile(), false, entry.latency(),
                    entry.gameMode(), entry.displayName(), entry.chatSession()));
            }
        }
        if (entries == null) {
            return packet;
        }
        ClientboundPlayerInfoUpdatePacket copy = new ClientboundPlayerInfoUpdatePacket(packet.actions(), java.util.List.of());
        ((io.izzel.arclight.common.mixin.core.network.protocol.game.ClientboundPlayerInfoUpdatePacketAccessor) copy).fusioncore$setEntries(entries);
        return copy;
    }

    /** Paper patch 0915. See https://minecraft.wiki/w/Experience#Leveling_up */
    public static int totalExperiencePoints(int level) {
        if (level <= 16) {
            return (int) (Math.pow(level, 2) + 6 * level);
        } else if (level <= 31) {
            return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360.0);
        } else {
            return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220.0);
        }
    }

    public static int levelsForExperiencePoints(int points) {
        if (points <= 352) {
            return (int) Math.floor(Math.sqrt(points + 9) - 3);
        } else if (points <= 1507) {
            return (int) Math.floor(8.1 + Math.sqrt(0.4 * (points - (7839.0 / 40.0))));
        } else {
            return (int) Math.floor((325.0 / 18.0) + Math.sqrt((2.0 / 9.0) * (points - (54215.0 / 72.0))));
        }
    }

    public static void setExperienceLevelAndProgress(Player player, int totalExperience) {
        com.google.common.base.Preconditions.checkArgument(totalExperience >= 0, "Total experience points must not be negative (%s)", totalExperience);
        ServerPlayer handle = handle(player);
        int level = levelsForExperiencePoints(totalExperience);
        int remaining = totalExperience - totalExperiencePoints(level);
        handle.experienceLevel = level;
        handle.experienceProgress = (float) remaining / handle.getXpNeededForNextLevel();
        handle.lastSentExp = -1;
    }

    /** Paper patch 1060. */
    public static void sendEntityEffect(Player player, org.bukkit.EntityEffect effect, org.bukkit.entity.Entity target) {
        ServerPlayer handle = handle(player);
        if (handle.connection == null || !effect.isApplicableTo(target)) {
            return;
        }
        handle.connection.send(new ClientboundEntityEventPacket(((CraftEntity) target).getHandle(), effect.getData()));
    }

    /** Paper patch 0119. */
    public static void sendActionBar(Player player, net.minecraft.network.chat.Component message) {
        ServerPlayer handle = handle(player);
        if (handle.connection != null && message != null) {
            handle.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(message));
        }
    }

    /** Paper's resource pack overloads with the SHA-1 as a hex string, through Spigot's byte[] overload. */
    public static void setResourcePack(Player player, UUID id, String url, String hash, net.kyori.adventure.text.Component prompt, boolean required) {
        byte[] bytes = hash == null || hash.isEmpty() ? null : java.util.HexFormat.of().parseHex(hash);
        player.setResourcePack(id, url, bytes, prompt == null ? null : FusionCoreAdventure.toLegacyRgb(prompt), required);
    }

    // Player.Spigot#getPing: the player is the enclosing CraftPlayer of Spigot's anonymous Spigot class

    private static final ClassValue<Field> OUTER = new ClassValue<>() {
        @Override
        protected Field computeValue(Class<?> type) {
            for (Field field : type.getDeclaredFields()) {
                if (Player.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return field;
                }
            }
            return null;
        }
    };

    public static int spigotPing(Object spigot) {
        Field outer = OUTER.get(spigot.getClass());
        if (outer == null) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        try {
            return ((Player) outer.get(spigot)).getPing();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    // OfflinePlayer bans (Paper's deprecated defaults)

    @SuppressWarnings("deprecation")
    public static BanEntry<?> banName(OfflinePlayer player, String reason, Date expires, String source, boolean kick) {
        BanEntry<?> entry = Bukkit.getServer().getBanList(BanList.Type.NAME).addBan(player.getName(), reason, expires, source);
        if (kick && player.isOnline()) {
            player.getPlayer().kickPlayer(reason);
        }
        return entry;
    }

    @SuppressWarnings("deprecation")
    public static BanEntry<?> banIp(Player player, String reason, Date expires, String source, boolean kick) {
        BanEntry<?> entry = Bukkit.getServer().getBanList(BanList.Type.IP).addBan(player.getAddress().getAddress().getHostAddress(), reason, expires, source);
        if (kick && player.isOnline()) {
            player.kickPlayer(reason);
        }
        return entry;
    }

    // Paper's Player API that needs core hooks; the state is on the CraftPlayer (CraftPlayerPaperBridge)

    public static io.izzel.arclight.common.bridge.bukkit.CraftPlayerPaperBridge state(Object player) {
        return (io.izzel.arclight.common.bridge.bukkit.CraftPlayerPaperBridge) player;
    }

    public static io.izzel.arclight.common.bridge.bukkit.CraftPlayerPaperBridge state(net.minecraft.world.entity.player.Player player) {
        return (io.izzel.arclight.common.bridge.bukkit.CraftPlayerPaperBridge) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) player).bridge$getBukkitEntity();
    }

    /** Paper's {@code EntitySelector.PLAYER_AFFECTS_SPAWNING} (Paper patch 0034). */
    public static boolean affectsSpawning(net.minecraft.world.entity.Entity entity) {
        return !entity.isSpectator() && entity.isAlive() && entity instanceof net.minecraft.world.entity.player.Player player && state(player).fusioncore$affectsSpawning();
    }

    /** Paper's {@code EntityGetter#hasNearbyAlivePlayerThatAffectsSpawning}. */
    public static boolean hasNearbyAlivePlayerThatAffectsSpawning(net.minecraft.world.level.EntityGetter level, double x, double y, double z, double range) {
        for (net.minecraft.world.entity.player.Player player : level.players()) {
            if (affectsSpawning(player)) {
                double distanceSqr = player.distanceToSqr(x, y, z);
                if (range < 0.0D || distanceSqr < range * range) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The world's view distance (Spigot's per-world setting), which a player's own view distance can only lower. */
    public static int worldViewDistance(Player player) {
        return handle(player).serverLevel().getChunkSource().chunkMap.serverViewDistance;
    }

    /** Paper: 2..32, or -1 for the world's; applied by ChunkMap on the player's next move. */
    public static void setViewDistance(Player player, int viewDistance) {
        if (viewDistance != -1 && (viewDistance < 2 || viewDistance > 32)) {
            throw new IllegalArgumentException("View distance " + viewDistance + " is out of range of [2, 32]");
        }
        state(player).fusioncore$setViewDistance(viewDistance);
        ServerPlayer handle = handle(player);
        if (handle.connection != null) {
            int effective = viewDistance == -1 ? worldViewDistance(player) : Math.min(viewDistance, worldViewDistance(player));
            handle.connection.send(new net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket(effective));
        }
    }

    public static int sendViewDistance(Player player) {
        int world = worldViewDistance(player);
        int own = state(player).fusioncore$viewDistance();
        return own == -1 ? world : Math.min(own, world);
    }

    public static boolean isChunkSent(Player player, long key) {
        org.spigotmc.AsyncCatcher.catchOp("accessing sent chunks");
        return state(player).fusioncore$sentChunks().contains(key);
    }

    public static java.util.Set<Long> sentChunkKeys(Player player) {
        org.spigotmc.AsyncCatcher.catchOp("accessing sent chunks");
        return it.unimi.dsi.fastutil.longs.LongSets.unmodifiable(state(player).fusioncore$sentChunks().clone());
    }

    public static java.util.Set<org.bukkit.Chunk> sentChunks(Player player) {
        org.spigotmc.AsyncCatcher.catchOp("accessing sent chunks");
        it.unimi.dsi.fastutil.longs.LongOpenHashSet keys = state(player).fusioncore$sentChunks();
        java.util.Set<org.bukkit.Chunk> chunks = new java.util.HashSet<>(keys.size());
        org.bukkit.World world = player.getWorld();
        it.unimi.dsi.fastutil.longs.LongIterator it = keys.iterator();
        while (it.hasNext()) {
            long key = it.nextLong();
            chunks.add(world.getChunkAt(net.minecraft.world.level.ChunkPos.getX(key), net.minecraft.world.level.ChunkPos.getZ(key), false));
        }
        return java.util.Collections.unmodifiableSet(chunks);
    }
}
