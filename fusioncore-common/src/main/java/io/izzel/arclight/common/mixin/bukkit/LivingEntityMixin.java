package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's LivingEntity API (active item, raised hand, jumping, line of sight, arrows, stingers, ray traces, item damage and more; Paper patch numbers at each group). */
@Mixin(value = org.bukkit.entity.LivingEntity.class, remap = false)
public interface LivingEntityMixin extends io.papermc.paper.entity.Frictional {


    default void clearActiveItem() {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).stopUsingItem();
    }

    default boolean isHandRaised() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).isUsingItem();
    }

    default boolean hasActiveItem() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).isUsingItem();
    }

    default org.bukkit.inventory.ItemStack getActiveItem() {
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).getUseItem());
    }

    default int getActiveItemRemainingTime() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).getUseItemRemainingTicks();
    }

    default org.bukkit.inventory.EquipmentSlot getActiveItemHand() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
            ? org.bukkit.inventory.EquipmentSlot.HAND : org.bukkit.inventory.EquipmentSlot.OFF_HAND;
    }

    default org.bukkit.inventory.EquipmentSlot getHandRaised() {
        return this.getActiveItemHand();
    }

    /** Paper: an unobstructed line from the eyes to the location, within 128 blocks. */
    default boolean hasLineOfSight(org.bukkit.Location loc) {
        net.minecraft.world.entity.LivingEntity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this);
        if (handle.level() != ((org.bukkit.craftbukkit.v.CraftWorld) loc.getWorld()).getHandle()) {
            return false;
        }
        net.minecraft.world.phys.Vec3 from = new net.minecraft.world.phys.Vec3(handle.getX(), handle.getEyeY(), handle.getZ());
        net.minecraft.world.phys.Vec3 to = new net.minecraft.world.phys.Vec3(loc.getX(), loc.getY(), loc.getZ());
        if (to.distanceToSqr(from) > 128D * 128D) {
            return false;
        }
        return handle.level().clip(new net.minecraft.world.level.ClipContext(from, to, net.minecraft.world.level.ClipContext.Block.COLLIDER,
            net.minecraft.world.level.ClipContext.Fluid.NONE, handle)).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    // Paper's launchProjectile(type, velocity, consumer) is on ProjectileSourceMixin

    // Fusion Core: Paper's hurt direction (Paper patch 0445); only players have one to set (HumanEntityMixin)

    default float getHurtDirection() {
        return ((org.bukkit.craftbukkit.v.entity.CraftLivingEntity) (Object) this).getHandle().getHurtDir();
    }

    default void setHurtDirection(float hurtDirection) {
        throw new UnsupportedOperationException("Cannot set the hurt direction on a non player");
    }

    /** Fusion Core: Paper's {@code clearActivePotionEffects} (Paper patch 0824): removes all effects with cause PLUGIN. */
    default boolean clearActivePotionEffects() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge) ((org.bukkit.craftbukkit.v.entity.CraftLivingEntity) (Object) this).getHandle())
            .bridge$removeAllEffects(org.bukkit.event.entity.EntityPotionEffectEvent.Cause.PLUGIN);
    }
    // Fusion Core: the rest of Paper's LivingEntity API (Paper patch numbers at each group)

    // patch 0204 "LivingEntity Active Item API"

    default void startUsingItem(org.bukkit.inventory.EquipmentSlot hand) {
        com.google.common.base.Preconditions.checkArgument(hand != null, "hand must not be null");
        switch (hand) {
            case HAND -> io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
            case OFF_HAND -> io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
            default -> throw new IllegalArgumentException("hand may only be HAND or OFF_HAND");
        }
    }

    default void completeUsingActiveItem() {
        ((io.izzel.arclight.common.mixin.core.world.entity.LivingEntityPaperAccessor) io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this)).fusioncore$completeUsingItem();
    }

    default void setActiveItemRemainingTime(int ticks) {
        net.minecraft.world.entity.LivingEntity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this);
        com.google.common.base.Preconditions.checkArgument(ticks >= 0, "ticks must be >= 0");
        com.google.common.base.Preconditions.checkArgument(ticks <= handle.getUseItem().getUseDuration(handle), "ticks must be <= item use duration");
        handle.useItemRemaining = ticks;
    }

    default int getActiveItemUsedTime() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).getTicksUsingItem();
    }

    default int getItemUseRemainingTime() {
        return this.getActiveItemRemainingTime();
    }

    default int getHandRaisedTime() {
        return this.getActiveItemUsedTime();
    }

    // patch 0064 "Add methods for working with arrows stuck in living entities"; fireEvent: Spigot's ArrowBodyCountChangeEvent

    default void setArrowsInBody(int count, boolean fireEvent) {
        com.google.common.base.Preconditions.checkArgument(count >= 0, "New arrow amount must be >= 0");
        if (fireEvent) {
            io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).setArrowCount(count);
        } else {
            io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).getEntityData().set(net.minecraft.world.entity.LivingEntity.DATA_ARROW_COUNT_ID, count);
        }
    }

    default int getArrowsStuck() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).getArrowCount();
    }

    default void setArrowsStuck(int arrows) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).setArrowCount(arrows);
    }

    default void setNextArrowRemoval(int ticks) {
        com.google.common.base.Preconditions.checkArgument(ticks >= 0, "New amount of ticks before next arrow removal must be >= 0");
        io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).removeArrowTime = ticks;
    }

    default int getNextArrowRemoval() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).removeArrowTime;
    }

    // patch 0566 "Stinger API"

    default int getBeeStingerCooldown() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).removeStingerTime;
    }

    default void setBeeStingerCooldown(int ticks) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).removeStingerTime = ticks;
    }

    default int getBeeStingersInBody() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).getStingerCount();
    }

    default void setBeeStingersInBody(int count) {
        com.google.common.base.Preconditions.checkArgument(count >= 0, "New bee stinger amount must be >= 0");
        io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).setStingerCount(count);
    }

    default void setNextBeeStingerRemoval(int ticks) {
        com.google.common.base.Preconditions.checkArgument(ticks >= 0, "New amount of ticks before next bee stinger removal must be >= 0");
        io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).removeStingerTime = ticks;
    }

    default int getNextBeeStingerRemoval() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).removeStingerTime;
    }

    // patch 0199 "Make shield blocking delay configurable"

    default int getShieldBlockingDelay() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.LivingEntityPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this)).fusioncore$shieldBlockingDelay();
    }

    default void setShieldBlockingDelay(int delay) {
        ((io.izzel.arclight.common.bridge.core.world.entity.LivingEntityPaperBridge) io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this)).fusioncore$setShieldBlockingDelay(delay);
    }

    // patch 0147 "LivingEntity#setKiller"

    default void setKiller(org.bukkit.entity.Player killer) {
        net.minecraft.world.entity.LivingEntity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this);
        net.minecraft.server.level.ServerPlayer player = killer == null ? null : ((org.bukkit.craftbukkit.v.entity.CraftPlayer) killer).getHandle();
        handle.lastHurtByPlayer = player;
        handle.lastHurtByMob = player;
        ((io.izzel.arclight.common.mixin.core.world.entity.LivingEntityPaperAccessor) handle).fusioncore$setLastHurtByPlayerTime(player == null ? 0 : 100);
    }

    // patch 0316 "Entity Jump API"

    default boolean isJumping() {
        return ((io.izzel.arclight.common.mixin.core.world.entity.LivingEntityPaperAccessor) io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this)).fusioncore$jumping();
    }

    default void setJumping(boolean jumping) {
        net.minecraft.world.entity.LivingEntity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this);
        handle.setJumping(jumping);
        if (jumping && handle instanceof net.minecraft.world.entity.Mob mob) {
            mob.getJumpControl().jump();
        }
    }

    // patch 0557 "Missing Entity API" (movement input) and patch 0793 "Add Entity Body Yaw API"

    default float getSidewaysMovement() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).xxa;
    }

    default float getUpwardsMovement() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).yya;
    }

    default float getForwardsMovement() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).zza;
    }

    default float getBodyYaw() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).getVisualRotationYInDegrees();
    }

    default void setBodyYaw(float bodyYaw) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).setYBodyRot(bodyYaw);
    }

    // patch 0240 "Add ray tracing methods to LivingEntity" and patch 0252 "Add LivingEntity#getTargetEntity"

    default org.bukkit.block.Block getTargetBlock(int maxDistance) {
        return this.getTargetBlock(maxDistance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode.NEVER);
    }

    default org.bukkit.block.Block getTargetBlock(int maxDistance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) {
        return ((org.bukkit.entity.LivingEntity) this).getTargetBlockExact(maxDistance, fluidMode.bukkit);
    }

    default org.bukkit.block.BlockFace getTargetBlockFace(int maxDistance) {
        return this.getTargetBlockFace(maxDistance, org.bukkit.FluidCollisionMode.NEVER);
    }

    default org.bukkit.block.BlockFace getTargetBlockFace(int maxDistance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) {
        return this.getTargetBlockFace(maxDistance, fluidMode.bukkit);
    }

    default org.bukkit.block.BlockFace getTargetBlockFace(int maxDistance, org.bukkit.FluidCollisionMode fluidMode) {
        org.bukkit.util.RayTraceResult result = ((org.bukkit.entity.LivingEntity) this).rayTraceBlocks(maxDistance, fluidMode);
        return result != null ? result.getHitBlockFace() : null;
    }

    default com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int maxDistance) {
        return this.getTargetBlockInfo(maxDistance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode.NEVER);
    }

    default com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int maxDistance, com.destroystokyo.paper.block.TargetBlockInfo.FluidMode fluidMode) {
        org.bukkit.util.RayTraceResult result = ((org.bukkit.entity.LivingEntity) this).rayTraceBlocks(maxDistance, fluidMode.bukkit);
        if (result != null && result.getHitBlock() != null && result.getHitBlockFace() != null) {
            return new com.destroystokyo.paper.block.TargetBlockInfo(result.getHitBlock(), result.getHitBlockFace());
        }
        return null;
    }

    default org.bukkit.entity.Entity getTargetEntity(int maxDistance) {
        return this.getTargetEntity(maxDistance, false);
    }

    default org.bukkit.entity.Entity getTargetEntity(int maxDistance, boolean ignoreBlocks) {
        net.minecraft.world.phys.EntityHitResult hit = io.izzel.arclight.common.mod.util.FusionCoreEntities.targetEntity(io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this), maxDistance, ignoreBlocks);
        return hit == null ? null : ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) hit.getEntity()).bridge$getBukkitEntity();
    }

    default com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int maxDistance) {
        return this.getTargetEntityInfo(maxDistance, false);
    }

    default com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int maxDistance, boolean ignoreBlocks) {
        net.minecraft.world.phys.EntityHitResult hit = io.izzel.arclight.common.mod.util.FusionCoreEntities.targetEntity(io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this), maxDistance, ignoreBlocks);
        return hit == null ? null : new com.destroystokyo.paper.entity.TargetEntityInfo(((io.izzel.arclight.common.bridge.core.entity.EntityBridge) hit.getEntity()).bridge$getBukkitEntity(),
            new org.bukkit.util.Vector(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z));
    }

    default org.bukkit.util.RayTraceResult rayTraceEntities(int maxDistance) {
        return this.rayTraceEntities(maxDistance, false);
    }

    default org.bukkit.util.RayTraceResult rayTraceEntities(int maxDistance, boolean ignoreBlocks) {
        net.minecraft.world.phys.EntityHitResult hit = io.izzel.arclight.common.mod.util.FusionCoreEntities.targetEntity(io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this), maxDistance, ignoreBlocks);
        return hit == null ? null : new org.bukkit.util.RayTraceResult(new org.bukkit.util.Vector(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z),
            ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) hit.getEntity()).bridge$getBukkitEntity());
    }

    // patch 0367 "Add entity knockback API"; the knockback goes through the knockback events

    default void knockback(double strength, double directionX, double directionZ) {
        com.google.common.base.Preconditions.checkArgument(strength > 0, "Knockback strength must be > 0");
        io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).knockback(strength, directionX, directionZ);
    }

    // patch 0393 "Add playPickupItemAnimation to LivingEntity"

    default void playPickupItemAnimation(org.bukkit.entity.Item item) {
        this.playPickupItemAnimation(item, item.getItemStack().getAmount());
    }

    default void playPickupItemAnimation(org.bukkit.entity.Item item, int quantity) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).take(io.izzel.arclight.common.mod.util.FusionCoreEntities.handle(item), quantity);
    }

    // patch 0366 "Add LivingEntity#swingHand(EquipmentSlot) convenience method"

    default void swingHand(org.bukkit.inventory.EquipmentSlot hand) {
        com.google.common.base.Preconditions.checkArgument(hand == org.bukkit.inventory.EquipmentSlot.HAND || hand == org.bukkit.inventory.EquipmentSlot.OFF_HAND,String.format("Expected a valid hand, got \"%s\" instead!", hand));
        if (hand == org.bukkit.inventory.EquipmentSlot.HAND) {
            ((org.bukkit.entity.LivingEntity) this).swingMainHand();
        } else {
            ((org.bukkit.entity.LivingEntity) this).swingOffHand();
        }
    }

    // patch 0768 "ItemStack damage API" and patch 0967 "Fix equipment slot and group API"

    default void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot slot) {
        net.minecraft.world.entity.LivingEntity handle = io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this);
        handle.level().broadcastEntityEvent(handle, io.izzel.arclight.common.mixin.core.world.entity.LivingEntityPaperAccessor.fusioncore$entityEventForEquipmentBreak(
            org.bukkit.craftbukkit.v.CraftEquipmentSlot.getNMS(slot)));
    }

    default void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot slot, java.util.Collection<org.bukkit.entity.Player> players) {
        if (players.isEmpty()) {
            return;
        }
        net.minecraft.network.protocol.game.ClientboundEntityEventPacket packet = new net.minecraft.network.protocol.game.ClientboundEntityEventPacket(io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this),
            io.izzel.arclight.common.mixin.core.world.entity.LivingEntityPaperAccessor.fusioncore$entityEventForEquipmentBreak(org.bukkit.craftbukkit.v.CraftEquipmentSlot.getNMS(slot)));
        for (org.bukkit.entity.Player player : players) {
            net.minecraft.server.level.ServerPlayer handle = ((org.bukkit.craftbukkit.v.entity.CraftPlayer) player).getHandle();
            if (handle.connection != null) {
                handle.connection.send(packet);
            }
        }
    }

    default org.bukkit.inventory.ItemStack damageItemStack(org.bukkit.inventory.ItemStack stack, int amount) {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.damageItemStack(this, stack, amount);
    }

    default void damageItemStack(org.bukkit.inventory.EquipmentSlot slot, int amount) {
        net.minecraft.world.entity.EquipmentSlot nmsSlot = org.bukkit.craftbukkit.v.CraftEquipmentSlot.getNMS(slot);
        io.izzel.arclight.common.mod.util.FusionCoreEntities.damageItemStack(io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this), io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).getItemBySlot(nmsSlot), amount, nmsSlot);
    }

    default boolean canUseEquipmentSlot(org.bukkit.inventory.EquipmentSlot slot) {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.living(this).canUseSlot(org.bukkit.craftbukkit.v.CraftEquipmentSlot.getNMS(slot));
    }

    @Override
    default net.kyori.adventure.util.TriState getFrictionState() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.frictionState(this);
    }

    @Override
    default void setFrictionState(net.kyori.adventure.util.TriState state) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.setFrictionState(this, state);
    }
}
