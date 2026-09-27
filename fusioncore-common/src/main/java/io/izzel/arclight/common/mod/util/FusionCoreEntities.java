package io.izzel.arclight.common.mod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;

/** Fusion Core: bodies of Paper's entity additions that need vanilla internals. */
public final class FusionCoreEntities {

    private FusionCoreEntities() {
    }

    /** Paper's {@code CraftHumanEntity#getPotentialBedLocation()}. */
    public static Location potentialBedLocation(org.bukkit.entity.HumanEntity human) {
        if (!(((CraftHumanEntity) human).getHandle() instanceof ServerPlayer handle)) {
            return null;
        }
        BlockPos bed = handle.getRespawnPosition();
        if (bed == null) {
            return null;
        }
        ServerLevel level = handle.server.getLevel(handle.getRespawnDimension());
        if (level == null) {
            return null;
        }
        return new Location(((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) level).bridge$getWorld(), bed.getX(), bed.getY(), bed.getZ());
    }

    /** Paper's {@code CraftAttributeInstance#addTransientModifier}. */
    public static void addTransientModifier(AttributeInstance instance, AttributeModifier modifier) {
        com.google.common.base.Preconditions.checkArgument(modifier != null, "modifier");
        ((io.izzel.arclight.common.mixin.bukkit.CraftAttributeInstanceAccessor) instance).fusioncore$handle().addTransientModifier(CraftAttributeInstance.convert(modifier));
    }

    public static net.minecraft.world.entity.Entity handle(Object bukkitEntity) {
        return ((org.bukkit.craftbukkit.v.entity.CraftEntity) bukkitEntity).getHandle();
    }

    /** The vanilla entity behind a Bukkit one, cast to what the caller expects (Paper's {@code getHandle()} of the Craft subclass). */
    @SuppressWarnings("unchecked")
    public static <T extends net.minecraft.world.entity.Entity> T nms(Object bukkitEntity) {
        return (T) handle(bukkitEntity);
    }

    /** Paper's {@code MCUtil.toLocation(level, pos)}: the block position in the entity's world, {@code null} for {@code null}. */
    public static org.bukkit.Location location(Object bukkitEntity, @org.jetbrains.annotations.Nullable net.minecraft.core.BlockPos pos) {
        return pos == null ? null : new org.bukkit.Location(((org.bukkit.entity.Entity) bukkitEntity).getWorld(), pos.getX(), pos.getY(), pos.getZ());
    }

    /** Paper's {@code MCUtil.toBlockPosition(location)}. */
    public static net.minecraft.core.BlockPos blockPos(org.bukkit.Location location) {
        return net.minecraft.core.BlockPos.containing(location.getX(), location.getY(), location.getZ());
    }

    /** Paper's {@code Entity#getOrigin} (patch 0036): where the entity first entered a world. */
    public static org.bukkit.Location origin(Object bukkitEntity) {
        io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge paper = paper(bukkitEntity);
        org.bukkit.util.Vector origin = paper.fusioncore$originVector();
        if (origin == null) {
            return null;
        }
        org.bukkit.World world = ((org.bukkit.entity.Entity) bukkitEntity).getWorld();
        if (paper.fusioncore$originWorld() != null) {
            world = org.bukkit.Bukkit.getWorld(paper.fusioncore$originWorld());
        }
        return origin.toLocation(world);
    }

    public static net.minecraft.world.entity.LivingEntity living(Object bukkitEntity) {
        return (net.minecraft.world.entity.LivingEntity) handle(bukkitEntity);
    }

    /** Vanilla's private {@code Entity#isInRain}, which Paper exposes. */
    public static boolean isInRain(net.minecraft.world.entity.Entity entity) {
        BlockPos pos = entity.blockPosition();
        return entity.level().isRainingAt(pos) || entity.level().isRainingAt(BlockPos.containing(pos.getX(), entity.getBoundingBox().maxY, pos.getZ()));
    }

    /** Vanilla's private {@code Entity#isInBubbleColumn}, which Paper exposes. */
    public static boolean isInBubbleColumn(net.minecraft.world.entity.Entity entity) {
        return entity.level().getBlockState(entity.blockPosition()).is(net.minecraft.world.level.block.Blocks.BUBBLE_COLUMN);
    }

    /**
     * Paper's EntityJumpEvent (Paper patch 0316 "Entity Jump API"): true when the jump may happen. Cancelled, the entity
     * stops wanting to jump ({@code setJumping(false)}), which ends a jump loop, as in Paper.
     */
    public static boolean jumpEvent(net.minecraft.world.entity.LivingEntity entity) {
        if (com.destroystokyo.paper.event.entity.EntityJumpEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return true;
        }
        com.destroystokyo.paper.event.entity.EntityJumpEvent event = new com.destroystokyo.paper.event.entity.EntityJumpEvent(
            (org.bukkit.entity.LivingEntity) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) entity).bridge$getBukkitEntity());
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            entity.setJumping(false);
            return false;
        }
        return true;
    }

    /** Paper's EntityPushedByEntityAttackEvent (patch 0202). Runs the push with the event's acceleration, or not at all. */
    public static void pushedBy(net.minecraft.world.entity.Entity target, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.Entity pusher,
                                double x, double y, double z, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> push) {
        if (pusher == null || target instanceof net.minecraft.world.entity.decoration.BlockAttachedEntity
            || target instanceof net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge
            || io.papermc.paper.event.entity.EntityKnockbackEvent.getHandlerList().getRegisteredListeners().length == 0) {
            push.call(target, x, y, z);
            return;
        }
        io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent event = new io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent(
            ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) target).bridge$getBukkitEntity(), io.papermc.paper.event.entity.EntityKnockbackEvent.Cause.PUSH,
            ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) pusher).bridge$getBukkitEntity(), new org.bukkit.util.Vector(x, y, z));
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            org.bukkit.util.Vector push2 = event.getAcceleration();
            push.call(target, push2.getX(), push2.getY(), push2.getZ());
        }
    }

    /** Paper's knockback events (patch 0202), written back into Spigot's event. */
    public static void paperKnockback(org.bukkit.event.entity.EntityKnockbackEvent spigot, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.Entity attacker, double force) {
        if (io.papermc.paper.event.entity.EntityKnockbackEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }
        org.bukkit.entity.LivingEntity entity = (org.bukkit.entity.LivingEntity) spigot.getEntity();
        org.bukkit.util.Vector velocity = entity.getVelocity();
        io.papermc.paper.event.entity.EntityKnockbackEvent.Cause cause;
        try {
            cause = io.papermc.paper.event.entity.EntityKnockbackEvent.Cause.valueOf(spigot.getCause().name());
        } catch (IllegalArgumentException e) {
            cause = io.papermc.paper.event.entity.EntityKnockbackEvent.Cause.UNKNOWN;
        }
        org.bukkit.util.Vector knockback = spigot.getFinalKnockback().clone().subtract(velocity);
        io.papermc.paper.event.entity.EntityKnockbackEvent event = attacker != null
            ? new com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent(entity, ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) attacker).bridge$getBukkitEntity(), cause, (float) force, knockback)
            : new io.papermc.paper.event.entity.EntityKnockbackEvent(entity, cause, knockback);
        event.setCancelled(spigot.isCancelled());
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        spigot.setCancelled(event.isCancelled());
        spigot.setFinalKnockback(velocity.clone().add(event.getKnockback()));
    }

    /** Paper patch 0036: an entity that enters a world without an origin gets its current location as origin. */
    public static void initOrigin(net.minecraft.world.entity.Entity entity, ServerLevel level) {
        io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge paper = (io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge) entity;
        org.bukkit.World world = ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) level).bridge$getWorld();
        if (paper.fusioncore$originVector() == null) {
            paper.fusioncore$setOrigin(new Location(world, entity.getX(), entity.getY(), entity.getZ()));
        }
        if (paper.fusioncore$originWorld() == null) {
            paper.fusioncore$setOrigin(paper.fusioncore$originVector().toLocation(world));
        }
    }

    /** Paper's per-entity state on the vanilla entity behind a Bukkit entity. */
    public static io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge paper(Object bukkitEntity) {
        return (io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge) handle(bukkitEntity);
    }

    /** Paper's CraftArmorStand#fromNMSRotations (patch 0219). */
    public static io.papermc.paper.math.Rotations rotations(net.minecraft.core.Rotations rotations) {
        return io.papermc.paper.math.Rotations.ofDegrees(rotations.getX(), rotations.getY(), rotations.getZ());
    }

    /** Paper's CraftArmorStand#toNMSRotations (patch 0219). */
    public static net.minecraft.core.Rotations rotations(io.papermc.paper.math.Rotations rotations) {
        return new net.minecraft.core.Rotations((float) rotations.x(), (float) rotations.y(), (float) rotations.z());
    }

    /** Paper's {@code getTargetEntity} (patch 0252). */
    public static net.minecraft.world.phys.EntityHitResult targetEntity(net.minecraft.world.entity.LivingEntity living, int maxDistance, boolean ignoreBlocks) {
        if (maxDistance < 1 || maxDistance > 120) {
            throw new IllegalArgumentException("maxDistance must be between 1-120");
        }
        net.minecraft.world.phys.Vec3 start = living.getEyePosition(1.0F);
        net.minecraft.world.phys.Vec3 direction = living.getLookAngle();
        net.minecraft.world.phys.Vec3 end = start.add(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance);
        java.util.List<net.minecraft.world.entity.Entity> entities = living.level().getEntities(living,
            living.getBoundingBox().expandTowards(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance).inflate(1.0D, 1.0D, 1.0D),
            net.minecraft.world.entity.EntitySelector.NO_SPECTATORS.and(net.minecraft.world.entity.Entity::isPickable));
        double distance = 0.0D;
        net.minecraft.world.phys.EntityHitResult result = null;
        for (net.minecraft.world.entity.Entity entity : entities) {
            double inflation = entity.getPickRadius();
            java.util.Optional<net.minecraft.world.phys.Vec3> hit = entity.getBoundingBox().inflate(inflation, inflation, inflation).clip(start, end);
            if (hit.isPresent()) {
                double distanceTo = start.distanceToSqr(hit.get());
                if (distanceTo < distance || distance == 0.0D) {
                    result = new net.minecraft.world.phys.EntityHitResult(entity, hit.get());
                    distance = distanceTo;
                }
            }
        }
        if (result == null || ignoreBlocks) {
            return result;
        }
        // Paper's LivingEntity#getRayTrace(maxDistance, Fluid.NONE)
        net.minecraft.world.phys.HitResult blocks = living.level().clip(new net.minecraft.world.level.ClipContext(start, end,
            net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, living));
        if (blocks != null && blocks.getType() != net.minecraft.world.phys.HitResult.Type.MISS
            && start.distanceToSqr(blocks.getLocation()) <= start.distanceToSqr(result.getLocation())) {
            return null;
        }
        return result;
    }

    /**
     * Paper's {@code CraftLivingEntity#damageItemStack(ItemStack, int)}, also behind {@code ItemStack#damage}: a CraftItemStack
     * is damaged in place, a plain stack is mirrored first and the damaged mirror returned.
     */
    public static org.bukkit.inventory.ItemStack damageItemStack(Object bukkitLiving, org.bukkit.inventory.ItemStack stack, int amount) {
        net.minecraft.world.item.ItemStack nmsStack;
        if (stack instanceof org.bukkit.craftbukkit.v.inventory.CraftItemStack craft) {
            nmsStack = ((io.izzel.arclight.common.mixin.bukkit.CraftItemStackAccessor) (Object) craft).fusioncore$handle();
            if (nmsStack == null || nmsStack.isEmpty()) {
                return stack;
            }
        } else {
            nmsStack = org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(stack);
            stack = org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(nmsStack); // mirror to capture changes in hurt logic and events
        }
        damageItemStack(living(bukkitLiving), nmsStack, amount, null);
        return stack;
    }

    /** Paper's {@code damageItemStack0} (patch 0768). */
    public static void damageItemStack(net.minecraft.world.entity.LivingEntity living, net.minecraft.world.item.ItemStack stack, int amount,
                                       @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        if (!(living.level() instanceof ServerLevel level)) {
            return;
        }
        if (slot != null && !living.hasInfiniteMaterials()) {
            stack.hurtAndBreak(amount, living, slot);
            return;
        }
        ServerPlayer player = living instanceof ServerPlayer serverPlayer && !serverPlayer.hasInfiniteMaterials() ? serverPlayer : null;
        stack.hurtAndBreak(amount, level, player, item -> {
            if (slot != null) {
                living.onEquippedItemBroken(item, slot);
            }
        });
    }

    // --- Paper's entity capability interfaces ---

    /** Paper's {@code PaperBucketable} (patch 0643). */
    public static net.minecraft.world.entity.animal.Bucketable bucketable(Object bukkitEntity) {
        return (net.minecraft.world.entity.animal.Bucketable) handle(bukkitEntity);
    }

    public static org.bukkit.inventory.ItemStack baseBucketItem(Object bukkitEntity) {
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(bucketable(bukkitEntity).getBucketItemStack());
    }

    public static org.bukkit.Sound bucketPickupSound(Object bukkitEntity) {
        return org.bukkit.craftbukkit.v.CraftSound.minecraftToBukkit(bucketable(bukkitEntity).getPickupSound());
    }

    /** Paper's {@code PaperShearable} (patch 0802). */
    public static net.minecraft.world.entity.Shearable shearable(Object bukkitEntity) {
        return (net.minecraft.world.entity.Shearable) handle(bukkitEntity);
    }

    public static void shear(Object bukkitEntity, net.kyori.adventure.sound.Sound.Source source) {
        shearable(bukkitEntity).shear(soundSource(source));
    }

    /** Paper's {@code PaperAdventure#asVanilla(Sound.Source)}. */
    public static net.minecraft.sounds.SoundSource soundSource(net.kyori.adventure.sound.Sound.Source source) {
        return switch (source) {
            case MASTER -> net.minecraft.sounds.SoundSource.MASTER;
            case MUSIC -> net.minecraft.sounds.SoundSource.MUSIC;
            case RECORD -> net.minecraft.sounds.SoundSource.RECORDS;
            case WEATHER -> net.minecraft.sounds.SoundSource.WEATHER;
            case BLOCK -> net.minecraft.sounds.SoundSource.BLOCKS;
            case HOSTILE -> net.minecraft.sounds.SoundSource.HOSTILE;
            case NEUTRAL -> net.minecraft.sounds.SoundSource.NEUTRAL;
            case PLAYER -> net.minecraft.sounds.SoundSource.PLAYERS;
            case AMBIENT -> net.minecraft.sounds.SoundSource.AMBIENT;
            case VOICE -> net.minecraft.sounds.SoundSource.VOICE;
        };
    }

    /** Paper's {@code PaperLeashable} (patch 1042), for leashable entities that are not mobs (boats). */
    public static boolean isLeashed(Object bukkitEntity) {
        return ((net.minecraft.world.entity.Leashable) handle(bukkitEntity)).getLeashHolder() != null;
    }

    public static org.bukkit.entity.Entity leashHolder(Object bukkitEntity) {
        com.google.common.base.Preconditions.checkState(isLeashed(bukkitEntity), "Entity not leashed");
        net.minecraft.world.entity.Entity holder = ((net.minecraft.world.entity.Leashable) handle(bukkitEntity)).getLeashHolder();
        return ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) holder).bridge$getBukkitEntity();
    }

    /** Paper's {@code PaperLeashable#setLeashHolder}; Paper's world-generation check is left out (boats are not placed by world generation). */
    public static boolean setLeashHolder(Object bukkitEntity, @org.jetbrains.annotations.Nullable org.bukkit.entity.Entity holder) {
        net.minecraft.world.entity.Leashable leashable = (net.minecraft.world.entity.Leashable) handle(bukkitEntity);
        if (holder == null) {
            if (!isLeashed(bukkitEntity)) {
                return false;
            }
            leashable.dropLeash(true, false);
            return true;
        }
        if (holder.isDead()) {
            return false;
        }
        if (isLeashed(bukkitEntity)) {
            leashable.dropLeash(true, false);
        }
        leashable.setLeashedTo(handle(holder), true);
        return true;
    }

    /** Paper's {@code Frictional} (patch 0769) on living entities and items. */
    public static net.kyori.adventure.util.TriState frictionState(Object bukkitEntity) {
        return ((io.izzel.arclight.common.bridge.core.world.entity.api.FrictionApiBridge) handle(bukkitEntity)).fusioncore$frictionState();
    }

    public static void setFrictionState(Object bukkitEntity, net.kyori.adventure.util.TriState state) {
        java.util.Objects.requireNonNull(state, "state may not be null");
        ((io.izzel.arclight.common.bridge.core.world.entity.api.FrictionApiBridge) handle(bukkitEntity)).fusioncore$setFrictionState(state);
    }

    /** Paper's {@code CraftMagicNumbers#hasDefaultEntityAttributes} (patch 0584); mods' attribute suppliers count. */
    public static boolean hasDefaultEntityAttributes(org.bukkit.NamespacedKey key) {
        return net.minecraft.world.entity.ai.attributes.DefaultAttributes.hasSupplier(
            net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(org.bukkit.craftbukkit.v.util.CraftNamespacedKey.toMinecraft(key)));
    }

    /** Paper's {@code CraftMagicNumbers#getDefaultEntityAttributes} (patch 0584): a read-only view. */
    @SuppressWarnings("unchecked")
    public static org.bukkit.attribute.Attributable defaultEntityAttributes(org.bukkit.NamespacedKey key) {
        com.google.common.base.Preconditions.checkArgument(hasDefaultEntityAttributes(key), key + " doesn't have default attributes");
        var supplier = net.minecraft.world.entity.ai.attributes.DefaultAttributes.getSupplier(
            (net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity>) net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(org.bukkit.craftbukkit.v.util.CraftNamespacedKey.toMinecraft(key)));
        return new io.papermc.paper.attribute.UnmodifiableAttributeMap(supplier);
    }

    /** Paper's {@code GossipContainer.EntityGossips#getPaperReputation} (patch 0354). */
    public static com.destroystokyo.paper.entity.villager.Reputation paperReputation(net.minecraft.world.entity.ai.gossip.GossipContainer.EntityGossips gossips) {
        java.util.Map<com.destroystokyo.paper.entity.villager.ReputationType, Integer> map = new java.util.EnumMap<>(com.destroystokyo.paper.entity.villager.ReputationType.class);
        for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<net.minecraft.world.entity.ai.gossip.GossipType> type : gossips.entries.object2IntEntrySet()) {
            com.destroystokyo.paper.entity.villager.ReputationType api = reputationType(type.getKey());
            if (api != null) {
                map.put(api, type.getIntValue());
            }
        }
        return new com.destroystokyo.paper.entity.villager.Reputation(map);
    }

    /** Paper's {@code GossipContainer.EntityGossips#assignFromPaperReputation} (patch 0354). */
    public static void assignReputation(net.minecraft.world.entity.ai.gossip.GossipContainer.EntityGossips gossips, com.destroystokyo.paper.entity.villager.Reputation rep) {
        for (net.minecraft.world.entity.ai.gossip.GossipType type : net.minecraft.world.entity.ai.gossip.GossipType.values()) {
            com.destroystokyo.paper.entity.villager.ReputationType api = reputationType(type);
            if (api != null && rep.hasReputationSet(api)) {
                int reputation = rep.getReputation(api);
                if (reputation == 0) {
                    gossips.entries.removeInt(type);
                } else {
                    gossips.entries.put(type, reputation);
                }
            }
        }
    }

    private static com.destroystokyo.paper.entity.villager.ReputationType reputationType(net.minecraft.world.entity.ai.gossip.GossipType type) {
        return switch (type) {
            case MAJOR_NEGATIVE -> com.destroystokyo.paper.entity.villager.ReputationType.MAJOR_NEGATIVE;
            case MINOR_NEGATIVE -> com.destroystokyo.paper.entity.villager.ReputationType.MINOR_NEGATIVE;
            case MINOR_POSITIVE -> com.destroystokyo.paper.entity.villager.ReputationType.MINOR_POSITIVE;
            case MAJOR_POSITIVE -> com.destroystokyo.paper.entity.villager.ReputationType.MAJOR_POSITIVE;
            case TRADING -> com.destroystokyo.paper.entity.villager.ReputationType.TRADING;
            default -> null; // Fusion Core: a gossip type added by a mod has no Paper counterpart
        };
    }

    // Fusion Core: heal, tame owner, attribute registration, projectile consumer

    /** Paper patch 0557: heals the living entity (a dragon part heals the dragon), firing EntityRegainHealthEvent. */
    public static void heal(org.bukkit.entity.Damageable damageable, double amount, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason reason) {
        com.google.common.base.Preconditions.checkArgument(amount > 0, "amount must be positive");
        com.google.common.base.Preconditions.checkArgument(reason != null, "reason must not be null");
        if (damageable instanceof org.bukkit.entity.ComplexEntityPart part) {
            heal(part.getParent(), amount, reason);
            return;
        }
        net.minecraft.world.entity.Entity handle = ((org.bukkit.craftbukkit.v.entity.CraftEntity) damageable).getHandle();
        ((io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge) handle).bridge$heal((float) amount, reason);
    }

    /** Paper patch 0171: the owner's UUID without loading the owner. */
    public static java.util.UUID ownerUniqueId(org.bukkit.entity.Tameable tameable) {
        net.minecraft.world.entity.Entity handle = ((org.bukkit.craftbukkit.v.entity.CraftEntity) tameable).getHandle();
        if (handle instanceof net.minecraft.world.entity.TamableAnimal animal) {
            return animal.getOwnerUUID();
        } else if (handle instanceof net.minecraft.world.entity.animal.horse.AbstractHorse horse) {
            return horse.getOwnerUUID();
        } else if (handle instanceof net.minecraft.world.entity.OwnableEntity ownable) {
            return ownable.getOwnerUUID();
        }
        org.bukkit.entity.AnimalTamer owner = tameable.getOwner();
        return owner == null ? null : owner.getUniqueId();
    }

    /**
     * Paper patch 0489: adds an attribute the entity type does not have by default (base value from the attribute's
     * default); the instance is not synced to clients until it changes, as in Paper.
     */
    public static void registerAttribute(org.bukkit.attribute.Attributable attributable, org.bukkit.attribute.Attribute attribute) {
        com.google.common.base.Preconditions.checkArgument(attribute != null, "attribute");
        com.google.common.base.Preconditions.checkArgument(attributable instanceof org.bukkit.craftbukkit.v.entity.CraftLivingEntity,
            "only living entities can register attributes");
        net.minecraft.world.entity.LivingEntity handle = ((org.bukkit.craftbukkit.v.entity.CraftLivingEntity) attributable).getHandle();
        net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> holder = org.bukkit.craftbukkit.v.attribute.CraftAttribute.bukkitToMinecraftHolder(attribute);
        handle.getAttributes().attributes.put(holder, new net.minecraft.world.entity.ai.attributes.AttributeInstance(holder, instance -> {
        }));
    }

    private static final ThreadLocal<java.util.function.Consumer<Object>> LAUNCH_CONSUMER = new ThreadLocal<>();

    /**
     * Paper patch 0674: {@code launchProjectile(type, velocity, consumer)}. The consumer runs on the projectile before it
     * is added to the world (so launch events see its changes), through the hook on the Craft launchers' addFreshEntity.
     */
    @SuppressWarnings("unchecked")
    public static <T extends org.bukkit.entity.Projectile> T launchProjectile(org.bukkit.projectiles.ProjectileSource source, Class<? extends T> projectile,
                                                                         org.bukkit.util.Vector velocity, java.util.function.Consumer<? super T> function) {
        java.util.function.Consumer<Object> previous = LAUNCH_CONSUMER.get();
        LAUNCH_CONSUMER.set(function == null ? null : entity -> function.accept((T) entity));
        try {
            return source.launchProjectile(projectile, velocity);
        } finally {
            LAUNCH_CONSUMER.set(previous);
        }
    }

    /** Called by the launch hooks right before the projectile is added to the world. */
    public static void beforeLaunchAdded(net.minecraft.world.entity.Entity projectile) {
        java.util.function.Consumer<Object> consumer = LAUNCH_CONSUMER.get();
        if (consumer != null) {
            LAUNCH_CONSUMER.remove();
            consumer.accept(((io.izzel.arclight.common.bridge.core.entity.EntityBridge) projectile).bridge$getBukkitEntity());
        }
    }
}
