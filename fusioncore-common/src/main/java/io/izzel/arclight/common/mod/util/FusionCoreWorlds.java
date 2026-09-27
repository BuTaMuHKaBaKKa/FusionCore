package io.izzel.arclight.common.mod.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.CraftParticle;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fusion Core: bodies of Paper's World additions that other Fusion Core code (copied Paper API classes, the Location
 * mixin) needs to call; javac does not see the methods {@code WorldMixin} adds to {@code World}.
 */
public final class FusionCoreWorlds {

    private FusionCoreWorlds() {
    }

    /**
     * Paper's {@code Effect#isApplicable}: whether {@code data} fits the effect. Paper lets an effect take several data
     * types; the only one with more than one in 1.21.1 is {@code STEP_SOUND} ({@code Material} or {@code BlockData}).
     */
    public static boolean effectApplicable(org.bukkit.Effect effect, Object data) {
        Class<?> type = effect.getData();
        if (type == null) {
            return false;
        }
        return type.isAssignableFrom(data.getClass())
            || (effect == org.bukkit.Effect.STEP_SOUND && data instanceof org.bukkit.block.data.BlockData);
    }

    /** Paper's {@code World#getNearbyEntitiesByType(Class, Location, double, double, double, Predicate)}. */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> Collection<T> getNearbyEntitiesByType(World world, Class<? extends Entity> clazz, Location loc,
                                                                          double xRadius, double yRadius, double zRadius, Predicate<? super T> predicate) {
        if (clazz == null) {
            clazz = Entity.class;
        }
        final List<T> nearby = new ArrayList<>();
        for (final Entity bukkitEntity : world.getNearbyEntities(loc, xRadius, yRadius, zRadius)) {
            if (clazz.isAssignableFrom(bukkitEntity.getClass()) && (predicate == null || predicate.test((T) bukkitEntity))) {
                nearby.add((T) bukkitEntity);
            }
        }
        return nearby;
    }

    public static Collection<Player> getNearbyPlayers(World world, Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(world, Player.class, loc, xRadius, yRadius, zRadius, null);
    }

    /**
     * Paper's {@code CraftWorld#spawnParticle} with receivers, source and force: every receiver (or everyone in the
     * world) who can see the source gets the particle packet.
     */
    public static <T> void spawnParticle(World world, Particle particle, List<Player> receivers, Player source,
                                         double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ,
                                         double extra, T data, boolean force) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        ParticleOptions options = CraftParticle.createParticleParam(particle, data);
        List<ServerPlayer> targets = new ArrayList<>();
        if (receivers == null) {
            targets.addAll(level.players());
        } else {
            for (Player receiver : receivers) {
                targets.add(((CraftPlayer) receiver).getHandle());
            }
        }
        for (ServerPlayer receiver : targets) {
            if (source != null && !((Player) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) receiver).bridge$getBukkitEntity()).canSee(source)) {
                continue;
            }
            level.sendParticles(receiver, options, force, x, y, z, count, offsetX, offsetY, offsetZ, extra);
        }
    }

    /** Paper's callback form of getChunkAtAsync: loads (or generates) the chunk, then calls back on the main thread. */
    public static void getChunkAtAsync(World world, int x, int z, java.util.function.Consumer<org.bukkit.Chunk> callback) {
        io.izzel.arclight.common.mod.server.ArclightServer.executeOnMainThread(() -> callback.accept(world.getChunkAt(x, z)));
    }

    /** Paper's {@code CraftMagicNumbers#getBiomeKey}. */
    public static org.bukkit.NamespacedKey biomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z) {
        net.minecraft.world.level.LevelAccessor level = ((org.bukkit.craftbukkit.v.CraftRegionAccessor) accessor).getHandle();
        return level.getBiome(new net.minecraft.core.BlockPos(x, y, z)).unwrapKey()
            .map(key -> org.bukkit.craftbukkit.v.util.CraftNamespacedKey.fromMinecraft(key.location())).orElse(null);
    }

    // Fusion Core: bodies of Paper's World and RegionAccessor API

    /** The level behind a Bukkit world (a {@code CraftWorld}). */
    public static net.minecraft.server.level.ServerLevel level(Object world) {
        return ((org.bukkit.craftbukkit.v.CraftWorld) world).getHandle();
    }

    /** Paper patch 0123: loaded entities. */
    public static int entityCount(net.minecraft.server.level.ServerLevel level) {
        int count = 0;
        for (net.minecraft.world.entity.Entity ignored : level.getAllEntities()) {
            count++;
        }
        return count;
    }

    /** Paper patch 0123: block entities in ticking chunks. */
    public static int tileEntityCount(net.minecraft.server.level.ServerLevel level) {
        int count = 0;
        for (net.minecraft.server.level.ChunkHolder holder : ((io.izzel.arclight.common.mixin.core.server.level.ChunkMapPaperAccessor) level.getChunkSource().chunkMap).fusioncore$chunks()) {
            net.minecraft.world.level.chunk.LevelChunk chunk = holder.getTickingChunk();
            if (chunk != null) {
                count += chunk.getBlockEntities().size();
            }
        }
        return count;
    }

    /** Paper patch 0123: ticking chunks. */
    public static int chunkCount(net.minecraft.server.level.ServerLevel level) {
        int count = 0;
        for (net.minecraft.server.level.ChunkHolder holder : ((io.izzel.arclight.common.mixin.core.server.level.ChunkMapPaperAccessor) level.getChunkSource().chunkMap).fusioncore$chunks()) {
            if (holder.getTickingChunk() != null) {
                count++;
            }
        }
        return count;
    }

    /** Paper patch 0516: blocks that burn forever in this dimension. */
    public static java.util.Collection<org.bukkit.Material> infiniburn(net.minecraft.server.level.ServerLevel level) {
        java.util.Set<org.bukkit.Material> materials = new java.util.HashSet<>();
        for (net.minecraft.core.Holder<net.minecraft.world.level.block.Block> block
            : net.minecraft.core.registries.BuiltInRegistries.BLOCK.getTagOrEmpty(level.dimensionType().infiniburn())) {
            materials.add(org.bukkit.craftbukkit.v.util.CraftMagicNumbers.getMaterial(block.value()));
        }
        return materials;
    }

    /**
     * Paper patch 0583: vanilla's {@code ServerLevel#findLightningTargetAround}, but null where vanilla falls back to
     * the ground position (no lightning rod and no living entity under the sky nearby).
     */
    public static net.minecraft.core.BlockPos lightningTarget(net.minecraft.server.level.ServerLevel level, net.minecraft.core.BlockPos pos) {
        net.minecraft.core.BlockPos ground = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos);
        java.util.Optional<net.minecraft.core.BlockPos> rod = ((io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge) level).fusioncore$findLightningRod(ground);
        if (rod.isPresent()) {
            return rod.get();
        }
        net.minecraft.world.phys.AABB box = net.minecraft.world.phys.AABB.encapsulatingFullBlocks(ground, new net.minecraft.core.BlockPos(ground.atY(level.getMaxBuildHeight()))).inflate(3.0D);
        java.util.List<net.minecraft.world.entity.LivingEntity> entities = level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, box,
            entity -> entity != null && entity.isAlive() && level.canSeeSky(entity.blockPosition()));
        return entities.isEmpty() ? null : entities.get(level.random.nextInt(entities.size())).blockPosition();
    }

    /** Paper patch 0677: the biome as the generator computes it (with the biome noise), not the stored one. */
    public static org.bukkit.block.Biome computedBiome(org.bukkit.RegionAccessor accessor, int x, int y, int z) {
        return org.bukkit.craftbukkit.v.block.CraftBiome.minecraftHolderToBukkit(
            ((org.bukkit.craftbukkit.v.CraftRegionAccessor) accessor).getHandle().getBiome(new net.minecraft.core.BlockPos(x, y, z)));
    }

    public static Location location(World world, io.papermc.paper.math.Position position) {
        return position instanceof Location location ? location : new Location(world, position.x(), position.y(), position.z());
    }

    /** Paper patch 0898: {@code CraftWorld#rayTraceBlocks} from a position, with a filter of the blocks that can be hit. */
    public static org.bukkit.util.RayTraceResult rayTraceBlocks(World world, io.papermc.paper.math.Position start, org.bukkit.util.Vector direction, double maxDistance,
                                                                org.bukkit.FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks,
                                                                java.util.function.Predicate<? super org.bukkit.block.Block> canCollide) {
        com.google.common.base.Preconditions.checkArgument(start != null, "Location start cannot be null");
        com.google.common.base.Preconditions.checkArgument(!(start instanceof Location location) || world.equals(location.getWorld()), "Location start cannot be in a different world");
        com.google.common.base.Preconditions.checkArgument(start.isFinite(), "Location start is not finite");
        com.google.common.base.Preconditions.checkArgument(direction != null, "Vector direction cannot be null");
        direction.checkFinite();
        com.google.common.base.Preconditions.checkArgument(direction.lengthSquared() > 0, "Direction's magnitude (%s) need to be greater than 0", direction.lengthSquared());
        com.google.common.base.Preconditions.checkArgument(fluidCollisionMode != null, "FluidCollisionMode cannot be null");
        if (maxDistance < 0.0D) {
            return null;
        }
        org.bukkit.util.Vector dir = direction.clone().normalize().multiply(maxDistance);
        net.minecraft.world.phys.Vec3 from = new net.minecraft.world.phys.Vec3(start.x(), start.y(), start.z());
        net.minecraft.world.phys.Vec3 to = from.add(dir.getX(), dir.getY(), dir.getZ());
        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(from, to,
            ignorePassableBlocks ? net.minecraft.world.level.ClipContext.Block.COLLIDER : net.minecraft.world.level.ClipContext.Block.OUTLINE,
            org.bukkit.craftbukkit.v.CraftFluidCollisionMode.toNMS(fluidCollisionMode), net.minecraft.world.phys.shapes.CollisionContext.empty());
        net.minecraft.server.level.ServerLevel level = level(world);
        net.minecraft.world.phys.HitResult hit = canCollide == null ? level.clip(context) : clip(level, context, canCollide);
        return org.bukkit.craftbukkit.v.util.CraftRayTraceResult.fromNMS(world, hit);
    }

    /** Vanilla's {@code BlockGetter#clip} that skips the blocks the filter rejects (Paper patch 0898). */
    private static net.minecraft.world.phys.BlockHitResult clip(net.minecraft.server.level.ServerLevel level, net.minecraft.world.level.ClipContext context,
                                                                java.util.function.Predicate<? super org.bukkit.block.Block> canCollide) {
        return net.minecraft.world.level.BlockGetter.traverseBlocks(context.getFrom(), context.getTo(), context, (ctx, pos) -> {
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
            if (state.isAir() || !canCollide.test(org.bukkit.craftbukkit.v.block.CraftBlock.at(level, pos))) {
                return null;
            }
            net.minecraft.world.level.material.FluidState fluid = level.getFluidState(pos);
            net.minecraft.world.phys.Vec3 from = ctx.getFrom();
            net.minecraft.world.phys.Vec3 to = ctx.getTo();
            net.minecraft.world.phys.BlockHitResult blockHit = level.clipWithInteractionOverride(from, to, pos, ctx.getBlockShape(state, level, pos), state);
            net.minecraft.world.phys.BlockHitResult fluidHit = ctx.getFluidShape(fluid, level, pos).clip(from, to, pos);
            double blockDistance = blockHit == null ? Double.MAX_VALUE : from.distanceToSqr(blockHit.getLocation());
            double fluidDistance = fluidHit == null ? Double.MAX_VALUE : from.distanceToSqr(fluidHit.getLocation());
            return blockDistance <= fluidDistance ? blockHit : fluidHit;
        }, ctx -> {
            net.minecraft.world.phys.Vec3 back = ctx.getFrom().subtract(ctx.getTo());
            return net.minecraft.world.phys.BlockHitResult.miss(ctx.getTo(), net.minecraft.core.Direction.getNearest(back.x, back.y, back.z),
                net.minecraft.core.BlockPos.containing(ctx.getTo()));
        });
    }
}
