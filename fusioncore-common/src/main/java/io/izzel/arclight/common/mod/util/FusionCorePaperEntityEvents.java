package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.advancements.AdvancementHolderBridge;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's entity and player events fired from the core. Events nobody listens to are not built. */
public final class FusionCorePaperEntityEvents {

    private FusionCorePaperEntityEvents() {
    }

    private static boolean listened(HandlerList handlers) {
        return handlers.getRegisteredListeners().length > 0;
    }

    /** Calls the event; true when it was not cancelled. */
    private static <T extends Event & Cancellable> boolean call(T event) {
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private static org.bukkit.entity.Entity bukkit(Entity entity) {
        return ((EntityBridge) entity).bridge$getBukkitEntity();
    }

    /**
     * CreeperIgniteEvent for a change of the creeper's ignited flag: the value to store, or null when nothing changes
     * (already in that state, or a listener cancelled).
     */
    public static @Nullable Boolean creeperIgnite(Creeper creeper, boolean ignited) {
        if (creeper.isIgnited() == ignited) {
            return null;
        }
        if (creeper.level().isClientSide || !listened(com.destroystokyo.paper.event.entity.CreeperIgniteEvent.getHandlerList())) {
            return ignited;
        }
        com.destroystokyo.paper.event.entity.CreeperIgniteEvent event = new com.destroystokyo.paper.event.entity.CreeperIgniteEvent((org.bukkit.entity.Creeper) bukkit(creeper), ignited);
        return call(event) ? event.isIgnited() : null;
    }

    /** EndermanAttackPlayerEvent, pre-cancelled when vanilla would not attack; the listeners' verdict. */
    public static boolean endermanAttack(EnderMan enderman, Player player, boolean shouldAttack) {
        if (enderman.level().isClientSide || !listened(com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent.getHandlerList())) {
            return shouldAttack;
        }
        com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent event = new com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent(
            (org.bukkit.entity.Enderman) bukkit(enderman), (org.bukkit.entity.Player) bukkit(player));
        event.setCancelled(!shouldAttack);
        return call(event);
    }

    /** PlayerPickupExperienceEvent: false when a listener cancelled the pickup. */
    public static boolean pickupExperience(Player player, ExperienceOrb orb) {
        if (!(player instanceof ServerPlayer) || !listened(com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent.getHandlerList())) {
            return true;
        }
        return call(new com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent((org.bukkit.entity.Player) bukkit(player), (org.bukkit.entity.ExperienceOrb) bukkit(orb)));
    }

    /** ExperienceOrbMergeEvent: false when a listener cancelled the merge of {@code source} into {@code target}. */
    public static boolean orbMerge(ExperienceOrb target, ExperienceOrb source) {
        if (target.level().isClientSide || !listened(com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent.getHandlerList())) {
            return true;
        }
        return call(new com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent((org.bukkit.entity.ExperienceOrb) bukkit(target), (org.bukkit.entity.ExperienceOrb) bukkit(source)));
    }

    public static void addedToWorld(Entity entity, ServerLevel level) {
        if (listened(com.destroystokyo.paper.event.entity.EntityAddToWorldEvent.getHandlerList())) {
            Bukkit.getPluginManager().callEvent(new com.destroystokyo.paper.event.entity.EntityAddToWorldEvent(bukkit(entity), ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) level).bridge$getWorld()));
        }
    }

    public static void removedFromWorld(Entity entity, ServerLevel level) {
        if (listened(com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent.getHandlerList())) {
            Bukkit.getPluginManager().callEvent(new com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent(bukkit(entity), ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) level).bridge$getWorld()));
        }
    }

    /** PlayerAdvancementCriterionGrantEvent: false when a listener cancelled the grant. */
    public static boolean criterionGrant(ServerPlayer player, AdvancementHolder advancement, String criterion) {
        if (!listened(com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent.getHandlerList())) {
            return true;
        }
        return call(new com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent((org.bukkit.entity.Player) bukkit(player),
            ((AdvancementHolderBridge) (Object) advancement).bridge$getBukkit(), criterion));
    }

    /** PlayerReadyArrowEvent (Paper patch 0201): false when a listener refused this projectile for the weapon. */
    public static boolean readyArrow(Player player, net.minecraft.world.item.ItemStack weapon, net.minecraft.world.item.ItemStack arrow) {
        if (!(player instanceof ServerPlayer) || !listened(com.destroystokyo.paper.event.player.PlayerReadyArrowEvent.getHandlerList())) {
            return true;
        }
        return call(new com.destroystokyo.paper.event.player.PlayerReadyArrowEvent((org.bukkit.entity.Player) bukkit(player),
            org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(weapon), org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(arrow)));
    }

    /** PrePlayerAttackEntityEvent (Paper patch 0761): false when a listener cancelled the attack. */
    public static boolean preAttack(Player player, Entity target, boolean willAttack) {
        return call(new io.papermc.paper.event.player.PrePlayerAttackEntityEvent((org.bukkit.entity.Player) bukkit(player), bukkit(target), willAttack));
    }

    // EntityPathfindEvent (Paper patch 0080): the entity the current createPath(Entity, int) paths to
    private static final ThreadLocal<Entity> PATH_TARGET = new ThreadLocal<>();

    public static void pathTarget(@Nullable Entity target) {
        if (target == null) {
            PATH_TARGET.remove();
        } else {
            PATH_TARGET.set(target);
        }
    }

    /** EntityPathfindEvent for each target: the targets no listener refused (the same set when nothing was refused). */
    public static java.util.Set<net.minecraft.core.BlockPos> pathfind(net.minecraft.world.entity.Mob mob, java.util.Set<net.minecraft.core.BlockPos> targets) {
        if (mob.level().isClientSide || !listened(com.destroystokyo.paper.event.entity.EntityPathfindEvent.getHandlerList())) {
            return targets;
        }
        Entity target = PATH_TARGET.get();
        org.bukkit.World world = ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) mob.level()).bridge$getWorld();
        java.util.Set<net.minecraft.core.BlockPos> allowed = targets;
        for (net.minecraft.core.BlockPos pos : targets) {
            com.destroystokyo.paper.event.entity.EntityPathfindEvent event = new com.destroystokyo.paper.event.entity.EntityPathfindEvent(bukkit(mob),
                new org.bukkit.Location(world, pos.getX(), pos.getY(), pos.getZ()), target == null ? null : bukkit(target));
            if (!call(event)) {
                if (allowed == targets) {
                    allowed = new java.util.HashSet<>(targets);
                }
                allowed.remove(pos);
            }
        }
        return allowed;
    }

    // Slime pathfinder events (Paper patch 0232); all of them also need Slime#canWander

    private static boolean canWander(net.minecraft.world.entity.monster.Slime slime) {
        return ((io.izzel.arclight.common.bridge.core.world.entity.monster.SlimeBridge) slime).bridge$canWander();
    }

    public static boolean slimeSwim(net.minecraft.world.entity.monster.Slime slime) {
        return canWander(slime) && (!listened(com.destroystokyo.paper.event.entity.SlimeSwimEvent.getHandlerList())
            || call(new com.destroystokyo.paper.event.entity.SlimeSwimEvent((org.bukkit.entity.Slime) bukkit(slime))));
    }

    public static boolean slimeWander(net.minecraft.world.entity.monster.Slime slime) {
        return canWander(slime) && (!listened(com.destroystokyo.paper.event.entity.SlimeWanderEvent.getHandlerList())
            || call(new com.destroystokyo.paper.event.entity.SlimeWanderEvent((org.bukkit.entity.Slime) bukkit(slime))));
    }

    public static boolean slimeTarget(net.minecraft.world.entity.monster.Slime slime, @Nullable net.minecraft.world.entity.LivingEntity target) {
        if (target == null || !target.isAlive() || !canWander(slime)) {
            return false;
        }
        return !listened(com.destroystokyo.paper.event.entity.SlimeTargetLivingEntityEvent.getHandlerList())
            || call(new com.destroystokyo.paper.event.entity.SlimeTargetLivingEntityEvent((org.bukkit.entity.Slime) bukkit(slime), (org.bukkit.entity.LivingEntity) bukkit(target)));
    }

    /** SlimeChangeDirectionEvent: the new direction, or null when the slime keeps its course (cancelled, or it may not wander). */
    public static @Nullable Float slimeDirection(net.minecraft.world.entity.monster.Slime slime, float yaw) {
        if (!canWander(slime)) {
            return null;
        }
        if (!listened(com.destroystokyo.paper.event.entity.SlimeChangeDirectionEvent.getHandlerList())) {
            return yaw;
        }
        com.destroystokyo.paper.event.entity.SlimeChangeDirectionEvent event = new com.destroystokyo.paper.event.entity.SlimeChangeDirectionEvent((org.bukkit.entity.Slime) bukkit(slime), yaw);
        return call(event) ? event.getNewYaw() : null;
    }

    /** PlayerDeepSleepEvent: false when a listener cancelled it. */
    public static boolean deepSleep(ServerPlayer player) {
        if (!listened(io.papermc.paper.event.player.PlayerDeepSleepEvent.getHandlerList())) {
            return true;
        }
        return call(new io.papermc.paper.event.player.PlayerDeepSleepEvent((org.bukkit.entity.Player) bukkit(player)));
    }

    // Witch potion events (Paper patches 0190 "WitchConsumePotionEvent", 0191 "WitchThrowPotionEvent", 0192 "WitchReadyPotionEvent")

    /** WitchConsumePotionEvent for a drunk potion: the contents to apply, null when cancelled. */
    public static @Nullable net.minecraft.world.item.alchemy.PotionContents witchConsume(net.minecraft.world.entity.monster.Witch witch, net.minecraft.world.item.ItemStack stack, @Nullable net.minecraft.world.item.alchemy.PotionContents contents) {
        if (!stack.is(net.minecraft.world.item.Items.POTION) || !listened(com.destroystokyo.paper.event.entity.WitchConsumePotionEvent.getHandlerList())) {
            return contents;
        }
        com.destroystokyo.paper.event.entity.WitchConsumePotionEvent event = new com.destroystokyo.paper.event.entity.WitchConsumePotionEvent((org.bukkit.entity.Witch) bukkit(witch), org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(stack));
        if (!call(event) || event.getPotion() == null) {
            return null;
        }
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(event.getPotion()).get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
    }

    /** WitchReadyPotionEvent: the potion the witch starts drinking, empty when cancelled or cleared. */
    public static net.minecraft.world.item.ItemStack witchReady(net.minecraft.world.entity.monster.Witch witch, net.minecraft.world.item.ItemStack potion) {
        if (!listened(com.destroystokyo.paper.event.entity.WitchReadyPotionEvent.getHandlerList())) {
            return potion;
        }
        com.destroystokyo.paper.event.entity.WitchReadyPotionEvent event = new com.destroystokyo.paper.event.entity.WitchReadyPotionEvent((org.bukkit.entity.Witch) bukkit(witch), org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(potion));
        if (!call(event) || event.getPotion() == null) {
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(event.getPotion());
    }

    /** WitchThrowPotionEvent: the potion to throw, null when cancelled (nothing is thrown). */
    public static @Nullable net.minecraft.world.item.ItemStack witchThrow(net.minecraft.world.entity.monster.Witch witch, net.minecraft.world.entity.LivingEntity target, net.minecraft.world.item.ItemStack potion) {
        if (!listened(com.destroystokyo.paper.event.entity.WitchThrowPotionEvent.getHandlerList())) {
            return potion;
        }
        com.destroystokyo.paper.event.entity.WitchThrowPotionEvent event = new com.destroystokyo.paper.event.entity.WitchThrowPotionEvent((org.bukkit.entity.Witch) bukkit(witch), (org.bukkit.entity.LivingEntity) bukkit(target), org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(potion));
        if (!call(event)) {
            return null;
        }
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(event.getPotion());
    }

    /**
     * EntityDyeEvent for a wolf's or cat's collar dyed by a player: the colour to apply, or null when a listener cancelled
     * (the interaction then fails, as on Paper).
     */
    public static net.minecraft.world.item.DyeColor collarDye(Entity entity, net.minecraft.world.item.DyeColor color, Player player) {
        if (entity.level().isClientSide || !(player instanceof ServerPlayer) || !listened(io.papermc.paper.event.entity.EntityDyeEvent.getHandlerList())) {
            return color;
        }
        io.papermc.paper.event.entity.EntityDyeEvent event = new io.papermc.paper.event.entity.EntityDyeEvent(bukkit(entity),
            org.bukkit.DyeColor.getByWoolData((byte) color.getId()), (org.bukkit.entity.Player) bukkit(player));
        return call(event) ? net.minecraft.world.item.DyeColor.byId(event.getColor().getWoolData()) : null;
    }
}
