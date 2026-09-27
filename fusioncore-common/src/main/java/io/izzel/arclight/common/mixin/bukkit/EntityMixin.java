package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.CraftEntitySchedulerBridge;
import io.izzel.arclight.common.mod.util.FusionCoreTeleport;
import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;

/**
 * Fusion Core: Paper's {@code teleportAsync} and teleport flags on the Bukkit {@link Entity} (Paper patch 0721
 * "More Teleport API"); the bodies are in {@link io.izzel.arclight.common.mod.util.FusionCoreTeleport}.
 */
@Mixin(value = Entity.class, remap = false)
public interface EntityMixin extends net.kyori.adventure.text.event.HoverEventSource<net.kyori.adventure.text.event.HoverEvent.ShowEntity>, net.kyori.adventure.sound.Sound.Emitter {

    default CompletableFuture<Boolean> teleportAsync(Location loc) {
        return this.teleportAsync(loc, TeleportCause.PLUGIN);
    }

    default CompletableFuture<Boolean> teleportAsync(Location loc, TeleportCause cause) {
        return FusionCoreTeleport.teleportAsync((Entity) this, loc, cause);
    }

    default CompletableFuture<Boolean> teleportAsync(Location location, TeleportCause cause, TeleportFlag... teleportFlags) {
        return FusionCoreTeleport.teleportAsync((Entity) this, location, cause, teleportFlags);
    }

    default boolean teleport(Location location, TeleportFlag... teleportFlags) {
        return FusionCoreTeleport.teleport((Entity) this, location, TeleportCause.PLUGIN, teleportFlags);
    }

    default boolean teleport(Location location, TeleportCause cause, TeleportFlag... teleportFlags) {
        return FusionCoreTeleport.teleport((Entity) this, location, cause, teleportFlags);
    }

    /** Fusion Core: Paper's Folia entity scheduler (patch 0843); see {@code PaperSchedulers}. */
    default EntityScheduler getScheduler() {
        return ((CraftEntitySchedulerBridge) this).fusioncore$apiScheduler();
    }

    // Fusion Core: Paper's Adventure API on entities (Paper patch 0006/0010 "Adventure"): an entity is a hover
    // source and a sound emitter; its names are the vanilla components.

    @Override
    default net.kyori.adventure.text.event.HoverEvent<net.kyori.adventure.text.event.HoverEvent.ShowEntity> asHoverEvent(
        java.util.function.UnaryOperator<net.kyori.adventure.text.event.HoverEvent.ShowEntity> op) {
        Entity self = (Entity) this;
        return net.kyori.adventure.text.event.HoverEvent.showEntity(op.apply(net.kyori.adventure.text.event.HoverEvent.ShowEntity.showEntity(
            net.kyori.adventure.key.Key.key(self.getType().getKey().toString()), self.getUniqueId(), this.customName())));
    }

    default net.kyori.adventure.text.Component customName() {
        net.minecraft.network.chat.Component name = ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle().getCustomName();
        return name == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.asAdventure(name);
    }

    default void customName(net.kyori.adventure.text.Component customName) {
        ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle().setCustomName(
            customName == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.asVanilla(customName));
    }

    default net.kyori.adventure.text.Component name() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.asAdventure(((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle().getName());
    }

    default net.kyori.adventure.text.Component teamDisplayName() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.asAdventure(((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle().getDisplayName());
    }

    // Fusion Core: Paper's simple Entity accessors, straight from the vanilla entity

    default double getX() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).getX();
    }

    default double getY() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).getY();
    }

    default double getZ() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).getZ();
    }

    default float getPitch() {
        return ((Entity) this).getLocation().getPitch();
    }

    default float getYaw() {
        return ((Entity) this).getLocation().getYaw();
    }

    default org.bukkit.Chunk getChunk() {
        return ((Entity) this).getLocation().getChunk();
    }

    default boolean isInLava() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).isInLava();
    }

    default boolean isInRain() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.isInRain(io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this));
    }

    default boolean isInBubbleColumn() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.isInBubbleColumn(io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this));
    }

    default boolean isInWaterOrRain() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).isInWaterOrRain();
    }

    default boolean isInWaterOrBubbleColumn() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).isInWaterOrBubble();
    }

    default boolean isInWaterOrRainOrBubbleColumn() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).isInWaterRainOrBubble();
    }

    default boolean isUnderWater() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).isUnderWater();
    }

    default boolean isInPowderedSnow() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).isInPowderSnow;
    }

    default java.util.Set<org.bukkit.entity.Player> getTrackedPlayers() {
        return ((Entity) this).getTrackedBy();
    }

    default boolean isSneaking() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).isShiftKeyDown();
    }

    default void setSneaking(boolean sneak) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).setShiftKeyDown(sneak);
    }

    default boolean isInvisible() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).isInvisible();
    }

    default void setInvisible(boolean invisible) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).setInvisible(invisible);
    }

    default boolean hasNoPhysics() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).noPhysics;
    }

    default void setNoPhysics(boolean noPhysics) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).noPhysics = noPhysics;
    }

    default String getScoreboardEntryName() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this).getScoreboardName();
    }

    /** Paper: whether the entity's box, moved to that location, would hit blocks or other solid entities. */
    default boolean collidesAt(org.bukkit.Location location) {
        net.minecraft.world.entity.Entity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this);
        net.minecraft.world.phys.AABB box = handle.getBoundingBox().move(location.getX() - handle.getX(), location.getY() - handle.getY(), location.getZ() - handle.getZ());
        return !handle.level().noCollision(handle, box);
    }

    default boolean wouldCollideUsing(org.bukkit.util.BoundingBox boundingBox) {
        net.minecraft.world.entity.Entity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this);
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
            boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        return !handle.level().noCollision(handle, box);
    }

    /** Paper: display name, UUID and permissions for Adventure (Paper patch 0010). */
    default net.kyori.adventure.pointer.Pointers pointers() {
        return io.izzel.arclight.common.mod.util.FusionCoreAudiences.entityPointers((Entity) this);
    }

    // Fusion Core: Paper's entity state API; the state lives on the vanilla entity (EntityPaperBridge, EntityPaperMixin)

    /** Paper patch 0036: where the entity first entered a world. */
    default org.bukkit.Location getOrigin() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.origin(this);
    }

    /** Paper patch 0280. */
    default org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason getEntitySpawnReason() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.paper(this).fusioncore$spawnReason();
    }

    /** Paper patch 0141: spawned by a mob spawner or a trial spawner. */
    default boolean fromMobSpawner() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.paper(this).fusioncore$fromMobSpawner();
    }

    /** Paper patch 0655. */
    default boolean isFreezeTickingLocked() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.paper(this).fusioncore$freezeLocked();
    }

    default void lockFreezeTicks(boolean locked) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.paper(this).fusioncore$setFreezeLocked(locked);
    }

    // Paper patch 0866 "Expand Pose API"

    default void setPose(org.bukkit.entity.Pose pose) {
        this.setPose(pose, false);
    }

    default void setPose(org.bukkit.entity.Pose pose, boolean fixed) {
        com.google.common.base.Preconditions.checkNotNull(pose, "Pose cannot be null");
        net.minecraft.world.entity.Entity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this);
        io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge paper = (io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge) handle;
        paper.fusioncore$setFixedPose(false);
        handle.setPose(net.minecraft.world.entity.Pose.values()[pose.ordinal()]);
        paper.fusioncore$setFixedPose(fixed);
    }

    default boolean hasFixedPose() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.paper(this).fusioncore$fixedPose();
    }

    /** Paper patch 0413: whether the entity is in an entity-ticking chunk. */
    default boolean isTicking() {
        net.minecraft.world.entity.Entity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this);
        return handle.level() instanceof net.minecraft.server.level.ServerLevel level && level.isPositionEntityTicking(handle.blockPosition());
    }

    /** Paper patch 1060: the hurt animation of this entity, shown to the given players only. */
    default void broadcastHurtAnimation(java.util.Collection<org.bukkit.entity.Player> players) {
        com.google.common.base.Preconditions.checkArgument(!players.contains(this), "Cannot broadcast hurt animation to self without a yaw");
        // CraftPlayer#sendHurtAnimation(0): vanilla's yaw is shifted by 90 degrees
        net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket packet =
            new net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket(((Entity) this).getEntityId(), 90F);
        for (org.bukkit.entity.Player player : players) {
            net.minecraft.server.level.ServerPlayer handle = ((org.bukkit.craftbukkit.v.entity.CraftPlayer) player).getHandle();
            if (handle.connection != null) {
                handle.connection.send(packet);
            }
        }
    }

    // Paper patch 0593: puts an entity made from bytes (UnsafeValues#deserializeEntity) into a world

    default boolean spawnAt(org.bukkit.Location location) {
        return this.spawnAt(location, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.DEFAULT);
    }

    default boolean spawnAt(org.bukkit.Location location, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        com.google.common.base.Preconditions.checkNotNull(location, "location cannot be null");
        com.google.common.base.Preconditions.checkNotNull(reason, "reason cannot be null");
        net.minecraft.world.entity.Entity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(this);
        net.minecraft.server.level.ServerLevel level = ((org.bukkit.craftbukkit.v.CraftWorld) location.getWorld()).getHandle();
        ((io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge) handle).fusioncore$setLevel(level);
        handle.absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        return !((io.izzel.arclight.common.bridge.core.entity.EntityBridge) handle).bridge$isValid()
            && ((io.izzel.arclight.common.bridge.core.world.level.IWorldWriterBridge) level).bridge$addEntity(handle, reason);
    }
}
