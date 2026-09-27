package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.CraftRegionAccessor;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

/**
 * Fusion Core: Paper's {@code RegionAccessor} API (computed biome, fluid data, world key, moon phase, collisions,
 * line of sight, spawn reasons).
 */
@Mixin(value = org.bukkit.RegionAccessor.class, remap = false)
public interface RegionAccessorMixin extends io.papermc.paper.world.flag.FeatureFlagSetHolder { // FeatureFlagSetHolder: Paper

    default org.bukkit.block.Biome getComputedBiome(int x, int y, int z) {
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.computedBiome((org.bukkit.RegionAccessor) this, x, y, z);
    }

    default io.papermc.paper.block.fluid.FluidData getFluidData(int x, int y, int z) {
        return io.papermc.paper.block.fluid.PaperFluidData.createData(((CraftRegionAccessor) (Object) this).getHandle().getFluidState(new net.minecraft.core.BlockPos(x, y, z)));
    }

    default io.papermc.paper.block.fluid.FluidData getFluidData(io.papermc.paper.math.Position position) {
        return this.getFluidData(position.blockX(), position.blockY(), position.blockZ());
    }

    default io.papermc.paper.block.fluid.FluidData getFluidData(org.bukkit.Location location) {
        return this.getFluidData(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    default org.bukkit.NamespacedKey getKey() {
        return org.bukkit.craftbukkit.v.util.CraftNamespacedKey.fromMinecraft(((CraftRegionAccessor) (Object) this).getHandle().getLevel().dimension().location());
    }

    default io.papermc.paper.world.MoonPhase getMoonPhase() {
        return io.papermc.paper.world.MoonPhase.getPhase(((CraftRegionAccessor) (Object) this).getHandle().dayTime() / 24000L);
    }

    default boolean hasCollisionsIn(org.bukkit.util.BoundingBox boundingBox) {
        net.minecraft.world.phys.AABB aabb = new net.minecraft.world.phys.AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
            boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        return !((CraftRegionAccessor) (Object) this).getHandle().noCollision(aabb);
    }

    default boolean lineOfSightExists(org.bukkit.Location from, org.bukkit.Location to) {
        com.google.common.base.Preconditions.checkArgument(from != null, "from parameter in lineOfSightExists cannot be null");
        com.google.common.base.Preconditions.checkArgument(to != null, "to parameter in lineOfSightExists cannot be null");
        if (from.getWorld() != to.getWorld()) {
            return false;
        }
        net.minecraft.world.phys.Vec3 start = new net.minecraft.world.phys.Vec3(from.getX(), from.getY(), from.getZ());
        net.minecraft.world.phys.Vec3 end = new net.minecraft.world.phys.Vec3(to.getX(), to.getY(), to.getZ());
        if (end.distanceToSqr(start) > 128D * 128D) {
            return false;
        }
        return ((CraftRegionAccessor) (Object) this).getHandle().clip(new net.minecraft.world.level.ClipContext(start, end,
            net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE,
            net.minecraft.world.phys.shapes.CollisionContext.empty())).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    // spawning with a reason; CraftRegionAccessor declares spawn(Location, Class, Consumer, SpawnReason) itself

    default <T extends Entity> T spawn(org.bukkit.Location location, Class<T> clazz, Consumer<? super T> function, CreatureSpawnEvent.SpawnReason reason) {
        throw new UnsupportedOperationException("Not a CraftRegionAccessor: " + this.getClass());
    }

    default <T extends Entity> T spawn(org.bukkit.Location location, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason) {
        return this.spawn(location, clazz, reason, null);
    }

    default <T extends Entity> T spawn(org.bukkit.Location location, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason, Consumer<? super T> function) {
        return this.spawn(location, clazz, function, reason);
    }

    default Entity spawnEntity(org.bukkit.Location loc, org.bukkit.entity.EntityType type, CreatureSpawnEvent.SpawnReason reason) {
        com.google.common.base.Preconditions.checkArgument(type.getEntityClass() != null, "%s is not a valid EntityType, must have an entity class", type);
        return this.spawn(loc, type.getEntityClass(), reason, null);
    }

    default Entity spawnEntity(org.bukkit.Location loc, org.bukkit.entity.EntityType type, CreatureSpawnEvent.SpawnReason reason, Consumer<? super Entity> function) {
        com.google.common.base.Preconditions.checkArgument(type.getEntityClass() != null, "%s is not a valid EntityType, must have an entity class", type);
        return this.spawn(loc, type.getEntityClass(), reason, function);
    }
}
