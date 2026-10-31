package io.izzel.arclight.neoforge.mod.machines;

import com.mojang.authlib.GameProfile;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.mod.util.DistValidate;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.MachinesSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fusion Core: machines of mods that change blocks without a player act as the player who placed them. The owner is kept
 * in the machine's persistent data; its block changes fire {@link BlockBreakEvent}/{@link BlockPlaceEvent} as a fake
 * player with the owner's profile. Server thread only.
 */
public final class MachineOwners {

    public static final String KEY = "fusioncore:owner";
    /** The key written by builds released as VitaCore; read, and replaced on the next write */
    private static final String LEGACY_KEY = "vitacore:owner";

    public record Owner(UUID id, String name) {
    }

    private record Context(Level level, BlockPos pos, String mod, Owner owner, Context previous) {
    }

    private static final Map<BlockEntityType<?>, String> BLOCK_ENTITY_MODS = new IdentityHashMap<>();
    private static final Map<EntityType<?>, String> ENTITY_MODS = new IdentityHashMap<>();
    private static final String VANILLA = "";

    private static Thread thread;
    private static Context context;
    private static Level allowedLevel;
    private static BlockPos allowedPos;

    private MachineOwners() {
    }

    private static MachinesSpec spec() {
        return ArclightConfig.spec().getMachines();
    }

    public static boolean enabled() {
        return spec().isOwnerEvents();
    }

    // ---- owners

    public static Owner read(CompoundTag data) {
        if (data == null) {
            return null;
        }
        String key = data.contains(KEY, 10) ? KEY : LEGACY_KEY;
        if (!data.contains(key, 10)) {
            return null;
        }
        CompoundTag tag = data.getCompound(key);
        if (!tag.hasUUID("id")) {
            return null;
        }
        return new Owner(tag.getUUID("id"), tag.getString("name"));
    }

    public static void write(CompoundTag data, Owner owner) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", owner.id());
        tag.putString("name", owner.name() == null ? "" : owner.name());
        data.put(KEY, tag);
        data.remove(LEGACY_KEY);
    }

    /** The mod id of a machine block entity, or {@code null} for vanilla and ignored mods */
    public static String modOf(BlockEntity blockEntity) {
        String mod = BLOCK_ENTITY_MODS.computeIfAbsent(blockEntity.getType(), type -> namespace(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type)));
        return mod == VANILLA || spec().isIgnoredMod(mod) ? null : mod;
    }

    public static String modOf(Entity entity) {
        String mod = ENTITY_MODS.computeIfAbsent(entity.getType(), type -> namespace(BuiltInRegistries.ENTITY_TYPE.getKey(type)));
        return mod == VANILLA || spec().isIgnoredMod(mod) ? null : mod;
    }

    private static String namespace(ResourceLocation key) {
        return key == null || key.getNamespace().equals("minecraft") ? VANILLA : key.getNamespace();
    }

    /** Records the player who placed a mod's block entity as its owner */
    public static void recordPlacement(BlockEntity blockEntity, GameProfile profile) {
        if (blockEntity == null || profile == null || profile.getId() == null || !enabled() || modOf(blockEntity) == null) {
            return;
        }
        write(blockEntity.getPersistentData(), new Owner(profile.getId(), profile.getName()));
        blockEntity.setChanged();
    }

    // ---- context

    public static boolean enter(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (!(level instanceof ServerLevel) || !enabled() || !DistValidate.isValid(level)) {
            return false;
        }
        String mod = modOf(blockEntity);
        if (mod == null) {
            return false;
        }
        return push(level, blockEntity.getBlockPos(), mod, read(blockEntity.getPersistentData()));
    }

    public static boolean enter(Entity entity) {
        Level level = entity.level();
        if (!(level instanceof ServerLevel) || !enabled() || !DistValidate.isValid(level)) {
            return false;
        }
        String mod = machineModOf(entity);
        if (mod == null) {
            return false;
        }
        return push(level, null, mod, read(entity.getPersistentData()));
    }

    /**
     * The mod id of an entity that works as a machine (a moving contraption), or {@code null}. Projectiles, mobs, items,
     * falling blocks and other entities with their own Bukkit events are never machines: a shell fired by a cannon must not
     * break blocks as the cannon's owner.
     */
    public static String machineModOf(Entity entity) {
        if (entity instanceof Projectile || entity instanceof TraceableEntity || entity instanceof LivingEntity
            || entity instanceof ItemEntity || entity instanceof FallingBlockEntity || entity instanceof ExperienceOrb
            || entity instanceof HangingEntity) {
            return null;
        }
        return modOf(entity);
    }

    private static boolean push(Level level, BlockPos pos, String mod, Owner owner) {
        if (owner == null) {
            String policy = spec().getUnknownOwner();
            if (policy.equals("mod")) {
                owner = modOwner(mod);
            } else if (!policy.equals("deny")) {
                return false;
            }
        }
        thread = Thread.currentThread();
        context = new Context(level, pos, mod, owner, context);
        return true;
    }

    public static void exit() {
        if (context != null) {
            context = context.previous();
        }
    }

    public static Owner modOwner(String mod) {
        String name = "[" + mod + "]";
        // the seed keeps the name the core had when these ids were first handed out, so protection plugins keep matching them
        return new Owner(UUID.nameUUIDFromBytes(("vitacore:machine:" + mod).getBytes(StandardCharsets.UTF_8)), name.length() > 16 ? name.substring(0, 16) : name);
    }

    public static boolean hasContext() {
        return context != null && thread == Thread.currentThread();
    }

    /** The owner of the machine running now, or {@code null} */
    public static Owner currentOwner() {
        Context ctx = context;
        return ctx != null && thread == Thread.currentThread() ? ctx.owner() : null;
    }

    /** What a machine creates (a block entity, an entity) inherits its player owner, not a {@code [mod]} one */
    public static void inherit(CompoundTag data) {
        Owner owner = currentOwner();
        if (owner != null && !owner.name().startsWith("[") && read(data) == null) {
            write(data, owner);
        }
    }

    // ---- guard

    private static int suspended;

    /**
     * Checks are off while a mod moves blocks it has already checked as a whole (Create's contraption assembly), and while
     * an explosion breaks blocks (it has its own Bukkit explode events)
     */
    public static void suspend() {
        suspended++;
    }

    public static void resume() {
        suspended--;
    }

    /** Whether the running machine may take the block at {@code pos} away; leaves no permission behind (bulk checks) */
    public static boolean mayRemove(Level level, BlockPos pos) {
        Context ctx = context;
        if (ctx == null || suspended > 0 || thread != Thread.currentThread() || ctx.level() != level || pos.equals(ctx.pos())) {
            return true;
        }
        return isEmpty(level.getBlockState(pos)) || fireBreak(ctx, level, pos);
    }

    /** {@link #checkSetBlock}, and on success the following {@code setBlock} at the same position is not asked again */
    public static boolean checkSetBlockOnce(Level level, BlockPos pos, BlockState state) {
        if (!hasContext()) {
            return true;
        }
        boolean allowed = checkSetBlock(level, pos, state);
        if (allowed) {
            allowedLevel = level;
            allowedPos = pos.immutable();
        }
        return allowed;
    }

    /** Whether the running machine may destroy the block at {@code pos}; called before any drops */
    public static boolean checkDestroy(Level level, BlockPos pos) {
        Context ctx = context;
        if (ctx == null || suspended > 0 || thread != Thread.currentThread() || ctx.level() != level || pos.equals(ctx.pos())) {
            return true;
        }
        if (isEmpty(level.getBlockState(pos))) {
            return true;
        }
        boolean allowed = fireBreak(ctx, level, pos);
        if (allowed) {
            // the destruction goes on through setBlock at the same position: do not ask twice
            allowedLevel = level;
            allowedPos = pos.immutable();
        }
        return allowed;
    }

    /** Whether the running machine may set {@code state} at {@code pos} */
    public static boolean checkSetBlock(Level level, BlockPos pos, BlockState state) {
        Context ctx = context;
        if (ctx == null || suspended > 0 || thread != Thread.currentThread() || ctx.level() != level || pos.equals(ctx.pos())) {
            return true;
        }
        if (allowedPos != null && allowedLevel == level && allowedPos.equals(pos)) {
            allowedPos = null;
            allowedLevel = null;
            return true;
        }
        BlockState old = level.getBlockState(pos);
        if (old == state || old.getBlock() == state.getBlock()) {
            return true;
        }
        boolean oldEmpty = isEmpty(old);
        if (oldEmpty && isEmpty(state)) {
            return true;
        }
        if (oldEmpty) {
            return firePlace(ctx, level, pos, state);
        }
        // one block replaced by another: it is broken, and the new one placed
        return fireBreak(ctx, level, pos) && (isEmpty(state) || firePlace(ctx, level, pos, state));
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.getBlock() instanceof LiquidBlock || state.canBeReplaced();
    }

    private static Player player(Owner owner, Level level) {
        var fake = FakePlayerFactory.get((ServerLevel) level, new GameProfile(owner.id(), owner.name()));
        return ((ServerPlayerBridge) fake).bridge$getBukkitEntity();
    }

    private static boolean fireBreak(Context ctx, Level level, BlockPos pos) {
        if (ctx.owner() == null) {
            return false;
        }
        Context saved = context;
        context = null;
        try {
            BlockBreakEvent event = new BlockBreakEvent(CraftBlock.at(level, pos), player(ctx.owner(), level));
            event.setExpToDrop(0);
            Bukkit.getPluginManager().callEvent(event);
            return !event.isCancelled();
        } finally {
            context = saved;
        }
    }

    private static boolean firePlace(Context ctx, Level level, BlockPos pos, BlockState state) {
        if (ctx.owner() == null) {
            return false;
        }
        Context saved = context;
        context = null;
        try {
            CraftBlock block = CraftBlock.at(level, pos);
            // the placed block shows the new state; the replaced state is what the world holds now
            CraftBlock placed = new PendingPlaceBlock(level, pos, state);
            var item = CraftItemStack.asBukkitCopy(new net.minecraft.world.item.ItemStack(state.getBlock().asItem()));
            BlockPlaceEvent event = new BlockPlaceEvent(placed, block.getState(), block, item, player(ctx.owner(), level), true, EquipmentSlot.HAND);
            Bukkit.getPluginManager().callEvent(event);
            return !event.isCancelled() && event.canBuild();
        } finally {
            context = saved;
        }
    }
}
