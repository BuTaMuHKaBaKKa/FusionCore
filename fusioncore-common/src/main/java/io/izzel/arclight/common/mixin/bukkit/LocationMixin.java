package io.izzel.arclight.common.mixin.bukkit;

import io.papermc.paper.math.FinePosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: {@link Location} is a {@link FinePosition}, as in Paper. */
@Mixin(value = Location.class, remap = false)
public abstract class LocationMixin implements FinePosition {

    @Shadow public abstract double getX();

    @Shadow public abstract double getY();

    @Shadow public abstract double getZ();

    @Shadow public abstract float getYaw();

    @Shadow public abstract float getPitch();

    @Override
    public double x() {
        return this.getX();
    }

    @Override
    public double y() {
        return this.getY();
    }

    @Override
    public double z() {
        return this.getZ();
    }

    @Override
    public boolean isFinite() {
        return Double.isFinite(this.x()) && Double.isFinite(this.y()) && Double.isFinite(this.z())
            && Float.isFinite(this.getYaw()) && Float.isFinite(this.getPitch());
    }

    @Override
    public @NotNull Location toLocation(@NotNull World world) {
        return new Location(world, this.x(), this.y(), this.z(), this.getYaw(), this.getPitch());
    }

    // Fusion Core: the rest of Paper's Location additions (Paper patches "expand location manipulation API",
    // "expand Location API", "isGenerated API", "Add heightmap api", "Expand Explosions API",
    // "additional getNearbyEntities API"), Paper's bodies

    @Shadow private double x;
    @Shadow private double y;
    @Shadow private double z;

    @Shadow public abstract World getWorld();

    @Shadow public abstract int getBlockX();

    @Shadow public abstract int getBlockY();

    @Shadow public abstract int getBlockZ();

    @Shadow public abstract Location clone();

    private Location fusioncore$self() {
        return (Location) (Object) this;
    }

    public boolean isChunkLoaded() {
        return this.getWorld().isChunkLoaded(Location.locToBlock(this.x) >> 4, Location.locToBlock(this.z) >> 4);
    }

    public boolean isGenerated() {
        World world = this.getWorld();
        com.google.common.base.Preconditions.checkNotNull(world, "Location has no world!");
        return world.isChunkGenerated(Location.locToBlock(this.x) >> 4, Location.locToBlock(this.z) >> 4);
    }

    public Location set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this.fusioncore$self();
    }

    public Location add(Location base, double x, double y, double z) {
        return this.set(base.getX() + x, base.getY() + y, base.getZ() + z);
    }

    public Location subtract(Location base, double x, double y, double z) {
        return this.set(base.getX() - x, base.getY() - y, base.getZ() - z);
    }

    public Location toBlockLocation() {
        Location blockLoc = this.clone();
        blockLoc.setX(this.getBlockX());
        blockLoc.setY(this.getBlockY());
        blockLoc.setZ(this.getBlockZ());
        return blockLoc;
    }

    /** Paper's {@code Block.getBlockKey} packing. */
    public long toBlockKey() {
        return ((long) this.getBlockX() & 0x7FFFFFF) | (((long) this.getBlockZ() & 0x7FFFFFF) << 27) | ((long) this.getBlockY() << 54);
    }

    public Location toCenterLocation() {
        Location centerLoc = this.clone();
        centerLoc.setX(this.getBlockX() + 0.5);
        centerLoc.setY(this.getBlockY() + 0.5);
        centerLoc.setZ(this.getBlockZ() + 0.5);
        return centerLoc;
    }

    public Location toHighestLocation() {
        return this.toHighestLocation(org.bukkit.HeightMap.WORLD_SURFACE);
    }

    public Location toHighestLocation(final org.bukkit.HeightMap heightMap) {
        final Location ret = this.clone();
        ret.setY(this.getWorld().getHighestBlockYAt(this.fusioncore$self(), heightMap));
        return ret;
    }

    public boolean createExplosion(float power) {
        return this.getWorld().createExplosion(this.fusioncore$self(), power);
    }

    public boolean createExplosion(float power, boolean setFire) {
        return this.getWorld().createExplosion(this.fusioncore$self(), power, setFire);
    }

    public boolean createExplosion(float power, boolean setFire, boolean breakBlocks) {
        return this.getWorld().createExplosion(this.fusioncore$self(), power, setFire, breakBlocks);
    }

    public boolean createExplosion(org.bukkit.entity.Entity source, float power) {
        return this.getWorld().createExplosion(this.fusioncore$self(), power, true, true, source);
    }

    public boolean createExplosion(org.bukkit.entity.Entity source, float power, boolean setFire) {
        return this.getWorld().createExplosion(this.fusioncore$self(), power, setFire, true, source);
    }

    public boolean createExplosion(org.bukkit.entity.Entity source, float power, boolean setFire, boolean breakBlocks) {
        return this.getWorld().createExplosion(this.fusioncore$self(), power, setFire, breakBlocks, source);
    }

    public java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(double radius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, radius, radius, radius, null);
    }

    public java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(double radius, java.util.function.Predicate<? super org.bukkit.entity.LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, radius, radius, radius, predicate);
    }

    public java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(double xzRadius, double yRadius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, xzRadius, yRadius, xzRadius, null);
    }

    public java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(double xzRadius, double yRadius, java.util.function.Predicate<? super org.bukkit.entity.LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, xzRadius, yRadius, xzRadius, predicate);
    }

    public java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(double xRadius, double yRadius, double zRadius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, xRadius, yRadius, zRadius, null);
    }

    public java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(double xRadius, double yRadius, double zRadius, java.util.function.Predicate<? super org.bukkit.entity.LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, xRadius, yRadius, zRadius, predicate);
    }

    public java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(double radius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, radius, radius, radius, null);
    }

    public java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(double radius, java.util.function.Predicate<? super org.bukkit.entity.Player> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, radius, radius, radius, predicate);
    }

    public java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(double xzRadius, double yRadius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, xzRadius, yRadius, xzRadius, null);
    }

    public java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(double xzRadius, double yRadius, java.util.function.Predicate<? super org.bukkit.entity.Player> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, xzRadius, yRadius, xzRadius, predicate);
    }

    public java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(double xRadius, double yRadius, double zRadius) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, xRadius, yRadius, zRadius, null);
    }

    public java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(double xRadius, double yRadius, double zRadius, java.util.function.Predicate<? super org.bukkit.entity.Player> predicate) {
        return this.getNearbyEntitiesByType(org.bukkit.entity.Player.class, xRadius, yRadius, zRadius, predicate);
    }

    public <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double radius) {
        return this.getNearbyEntitiesByType(clazz, radius, radius, radius, null);
    }

    public <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double radius, java.util.function.Predicate<? super T> predicate) {
        return this.getNearbyEntitiesByType(clazz, radius, radius, radius, predicate);
    }

    public <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double xzRadius, double yRadius) {
        return this.getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, null);
    }

    public <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double xzRadius, double yRadius, java.util.function.Predicate<? super T> predicate) {
        return this.getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, predicate);
    }

    public <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double xRadius, double yRadius, double zRadius) {
        return this.getNearbyEntitiesByType(clazz, xRadius, yRadius, zRadius, null);
    }

    public <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends org.bukkit.entity.Entity> clazz, double xRadius, double yRadius, double zRadius, java.util.function.Predicate<? super T> predicate) {
        final org.bukkit.World world = this.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.getNearbyEntitiesByType(world, clazz, (org.bukkit.Location) (Object) this, xRadius, yRadius, zRadius, predicate);
    }

    public java.util.Collection<org.bukkit.entity.Entity> getNearbyEntities(double x, double y, double z) {
        final org.bukkit.World world = this.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        return world.getNearbyEntities((org.bukkit.Location) (Object) this, x, y, z);
    }
}
