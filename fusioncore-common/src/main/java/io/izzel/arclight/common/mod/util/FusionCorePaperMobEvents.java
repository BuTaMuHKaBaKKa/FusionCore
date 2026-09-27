package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: more of Paper's entity events fired from the core. Events nobody listens to are not built. */
public final class FusionCorePaperMobEvents {

    private FusionCorePaperMobEvents() {
    }

    public static boolean listened(HandlerList handlers) {
        return handlers.getRegisteredListeners().length > 0;
    }

    /** Calls the event; true when it was not cancelled. */
    private static <T extends Event & Cancellable> boolean call(T event) {
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static org.bukkit.entity.Entity bukkit(Entity entity) {
        return ((EntityBridge) entity).bridge$getBukkitEntity();
    }

    /** ElderGuardianAppearanceEvent: false when the guardian is not to appear to (curse) this player. */
    public static boolean elderGuardianAppearance(Entity guardian, ServerPlayer player) {
        if (!listened(io.papermc.paper.event.entity.ElderGuardianAppearanceEvent.getHandlerList())
            || !(bukkit(guardian) instanceof org.bukkit.entity.ElderGuardian elder)) {
            return true;
        }
        return call(new io.papermc.paper.event.entity.ElderGuardianAppearanceEvent(elder, (org.bukkit.entity.Player) bukkit(player)));
    }

    public static boolean dragonFlame(Entity dragon, Entity flame) {
        if (!listened(com.destroystokyo.paper.event.entity.EnderDragonFlameEvent.getHandlerList())) {
            return true;
        }
        return call(new com.destroystokyo.paper.event.entity.EnderDragonFlameEvent((org.bukkit.entity.EnderDragon) bukkit(dragon),
            (org.bukkit.entity.AreaEffectCloud) bukkit(flame)));
    }

    public static boolean dragonShootFireball(Entity dragon, Entity fireball) {
        if (!listened(com.destroystokyo.paper.event.entity.EnderDragonShootFireballEvent.getHandlerList())) {
            return true;
        }
        return call(new com.destroystokyo.paper.event.entity.EnderDragonShootFireballEvent((org.bukkit.entity.EnderDragon) bukkit(dragon),
            (org.bukkit.entity.DragonFireball) bukkit(fireball)));
    }

    public static boolean dragonFireballHit(Entity fireball, java.util.List<? extends LivingEntity> targets, Entity cloud) {
        if (!listened(com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent.getHandlerList())) {
            return true;
        }
        java.util.List<org.bukkit.entity.LivingEntity> living = new java.util.ArrayList<>(targets.size());
        for (LivingEntity target : targets) {
            living.add((org.bukkit.entity.LivingEntity) bukkit(target));
        }
        return call(new com.destroystokyo.paper.event.entity.EnderDragonFireballHitEvent((org.bukkit.entity.DragonFireball) bukkit(fireball), living,
            (org.bukkit.entity.AreaEffectCloud) bukkit(cloud)));
    }

    /** EndermanEscapeEvent: false when the enderman is not to run away (teleport) for this reason. */
    public static boolean endermanEscape(Entity enderman, String reason) {
        if (!listened(com.destroystokyo.paper.event.entity.EndermanEscapeEvent.getHandlerList())) {
            return true;
        }
        return call(new com.destroystokyo.paper.event.entity.EndermanEscapeEvent((org.bukkit.entity.Enderman) bukkit(enderman),
            com.destroystokyo.paper.event.entity.EndermanEscapeEvent.Reason.valueOf(reason)));
    }

    /** EntityDamageItemEvent for a non-player: the damage to apply, or -1 when cancelled. */
    public static int entityDamageItem(LivingEntity entity, net.minecraft.world.item.ItemStack stack, int damage) {
        if (!listened(io.papermc.paper.event.entity.EntityDamageItemEvent.getHandlerList())) {
            return damage;
        }
        io.papermc.paper.event.entity.EntityDamageItemEvent event = new io.papermc.paper.event.entity.EntityDamageItemEvent(bukkit(entity),
            org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(stack), damage);
        return call(event) ? event.getDamage() : -1;
    }

    private static @Nullable Integer breedExperience;

    /**
     * EntityFertilizeEggEvent (a frog, sniffer or turtle about to lay eggs): the experience to drop, or null when cancelled
     * (both animals' love is reset then) or when nobody listens (vanilla draws its own).
     */
    public static @Nullable Integer fertilizeEgg(Animal breeding, Animal other, @Nullable net.minecraft.world.item.ItemStack bredWith, boolean[] cancelled) {
        cancelled[0] = false;
        if (!listened(io.papermc.paper.event.entity.EntityFertilizeEggEvent.getHandlerList())) {
            return null;
        }
        ServerPlayer player = breeding.getLoveCause();
        if (player == null) {
            player = other.getLoveCause();
        }
        int experience = breeding.getRandom().nextInt(7) + 1;
        io.papermc.paper.event.entity.EntityFertilizeEggEvent event = new io.papermc.paper.event.entity.EntityFertilizeEggEvent(
            (org.bukkit.entity.LivingEntity) bukkit(breeding), (org.bukkit.entity.LivingEntity) bukkit(other),
            player == null ? null : (org.bukkit.entity.Player) bukkit(player),
            bredWith == null ? null : org.bukkit.craftbukkit.v.inventory.CraftItemStack.asBukkitCopy(bredWith), experience);
        if (!call(event)) {
            breeding.resetLove();
            other.resetLove(); // stops the breeding goal from trying again every tick
            cancelled[0] = true;
            return null;
        }
        return event.getExperience();
    }

    /** Sets (or, with null, clears) the experience the next {@code finalizeSpawnChildFromBreeding} drops. */
    public static void setBreedExperience(@Nullable Integer experience) {
        breedExperience = experience;
    }

    /** The experience set by {@link #setBreedExperience}, taken once. */
    public static @Nullable Integer takeBreedExperience() {
        Integer experience = breedExperience;
        breedExperience = null;
        return experience;
    }

    private static boolean crossbowNoConsume;

    /**
     * EntityLoadCrossbowEvent: null when nobody listens, otherwise {cancelled, consume}.
     */
    public static boolean @Nullable [] loadCrossbow(LivingEntity entity, net.minecraft.world.item.ItemStack crossbow) {
        if (!listened(io.papermc.paper.event.entity.EntityLoadCrossbowEvent.getHandlerList())) {
            return null;
        }
        io.papermc.paper.event.entity.EntityLoadCrossbowEvent event = new io.papermc.paper.event.entity.EntityLoadCrossbowEvent(
            (org.bukkit.entity.LivingEntity) bukkit(entity), org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(crossbow),
            org.bukkit.craftbukkit.v.CraftEquipmentSlot.getHand(entity.getUsedItemHand()));
        boolean passed = call(event);
        return new boolean[] {!passed, event.shouldConsumeItem()};
    }

    /** Runs {@code action} (loading a crossbow) with ammo taken as if the shooter had infinite arrows. */
    public static boolean withoutAmmoUse(java.util.function.BooleanSupplier action) {
        boolean previous = crossbowNoConsume;
        crossbowNoConsume = true;
        try {
            return action.getAsBoolean();
        } finally {
            crossbowNoConsume = previous;
        }
    }

    public static boolean crossbowNoConsume() {
        return crossbowNoConsume;
    }

    /**
     * EntityPortalReadyEvent for a nether portal: the world the entity is to travel to (a listener may change it or set none),
     * or {@code from} unchanged when nobody listens. When cancelled, the entity's portal process is dropped and null returned.
     */
    public static @Nullable ServerLevel portalReady(Entity entity, @Nullable ServerLevel target, boolean[] cancelled) {
        cancelled[0] = false;
        if (!listened(io.papermc.paper.event.entity.EntityPortalReadyEvent.getHandlerList())) {
            return target;
        }
        io.papermc.paper.event.entity.EntityPortalReadyEvent event = new io.papermc.paper.event.entity.EntityPortalReadyEvent(bukkit(entity),
            target == null ? null : ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) target).bridge$getWorld(), org.bukkit.PortalType.NETHER);
        if (!call(event)) {
            entity.portalProcess = null;
            cancelled[0] = true;
            return null;
        }
        return event.getTargetWorld() == null ? null : ((org.bukkit.craftbukkit.v.CraftWorld) event.getTargetWorld()).getHandle();
    }

    /**
     * The teleport event for an entity changing dimension: Paper's EntityTeleportEndGatewayEvent when it goes through an end
     * gateway (its portal process is at a gateway block entity), Bukkit's EntityTeleportEvent otherwise.
     */
    public static org.bukkit.event.entity.EntityTeleportEvent teleportEvent(Entity entity, org.bukkit.Location to) {
        net.minecraft.world.entity.PortalProcessor process = entity.portalProcess;
        if (process != null && process.isSamePortal((net.minecraft.world.level.block.Portal) net.minecraft.world.level.block.Blocks.END_GATEWAY)
            && entity.level().getBlockEntity(process.getEntryPosition()) instanceof net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity gateway) {
            org.bukkit.entity.Entity bukkit = bukkit(entity);
            com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent event = new com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent(
                bukkit, bukkit.getLocation(), to, new org.bukkit.craftbukkit.v.block.CraftEndGateway(to.getWorld(), gateway));
            Bukkit.getPluginManager().callEvent(event);
            return event;
        }
        return org.bukkit.craftbukkit.v.event.CraftEventFactory.callEntityTeleportEvent(entity, to);
    }

    private static boolean sitWithoutEvent;

    /** Runs {@code action} (a sitting state loaded from NBT) without EntityToggleSitEvent, as Paper does. */
    public static void withoutSitEvent(Runnable action) {
        boolean previous = sitWithoutEvent;
        sitWithoutEvent = true;
        try {
            action.run();
        } finally {
            sitWithoutEvent = previous;
        }
    }

    /** EntityToggleSitEvent: false when the entity is not to sit down / stand up. */
    public static boolean toggleSit(Entity entity, boolean sitting) {
        if (sitWithoutEvent || !listened(io.papermc.paper.event.entity.EntityToggleSitEvent.getHandlerList())) {
            return true;
        }
        return call(new io.papermc.paper.event.entity.EntityToggleSitEvent(bukkit(entity), sitting));
    }

    /** PufferFishStateChangeEvent: false when the puffer fish is not to change to {@code newState}. */
    public static boolean pufferFishState(Entity fish, int newState) {
        if (!listened(io.papermc.paper.event.entity.PufferFishStateChangeEvent.getHandlerList())) {
            return true;
        }
        return call(new io.papermc.paper.event.entity.PufferFishStateChangeEvent((org.bukkit.entity.PufferFish) bukkit(fish), newState));
    }

    public static boolean shulkerDuplicate(Entity child, Entity parent) {
        if (!listened(io.papermc.paper.event.entity.ShulkerDuplicateEvent.getHandlerList())) {
            return true;
        }
        return call(new io.papermc.paper.event.entity.ShulkerDuplicateEvent((org.bukkit.entity.Shulker) bukkit(child), (org.bukkit.entity.Shulker) bukkit(parent)));
    }

    /** TameableDeathMessageEvent: the message to send the owner, or null when cancelled. */
    public static net.minecraft.network.chat.@Nullable Component tameableDeathMessage(Entity tameable, net.minecraft.network.chat.Component message) {
        if (!listened(io.papermc.paper.event.entity.TameableDeathMessageEvent.getHandlerList())
            || !(bukkit(tameable) instanceof org.bukkit.entity.Tameable bukkitTameable)) {
            return message;
        }
        io.papermc.paper.event.entity.TameableDeathMessageEvent event = new io.papermc.paper.event.entity.TameableDeathMessageEvent(bukkitTameable,
            FusionCoreAdventure.asAdventure(message));
        return call(event) ? FusionCoreAdventure.asVanilla(event.deathMessage()) : null;
    }

    /** ThrownEggHatchEvent; returns the event (always fired: it decides the chickens). */
    public static com.destroystokyo.paper.event.entity.ThrownEggHatchEvent thrownEggHatch(Entity egg, boolean hatching, byte hatches, org.bukkit.entity.EntityType type) {
        com.destroystokyo.paper.event.entity.ThrownEggHatchEvent event = new com.destroystokyo.paper.event.entity.ThrownEggHatchEvent(
            (org.bukkit.entity.Egg) bukkit(egg), hatching, hatches, type);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean turtleStartDigging(Entity turtle, net.minecraft.core.BlockPos pos) {
        if (!listened(com.destroystokyo.paper.event.entity.TurtleStartDiggingEvent.getHandlerList())) {
            return true;
        }
        return call(new com.destroystokyo.paper.event.entity.TurtleStartDiggingEvent((org.bukkit.entity.Turtle) bukkit(turtle),
            org.bukkit.craftbukkit.v.util.CraftLocation.toBukkit(pos, ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) turtle.level()).bridge$getWorld())));
    }

    /** TurtleLayEggEvent: the number of eggs to lay, or 0 when cancelled. */
    public static int turtleLayEgg(Entity turtle, net.minecraft.core.BlockPos pos, int eggs) {
        if (!listened(com.destroystokyo.paper.event.entity.TurtleLayEggEvent.getHandlerList())) {
            return eggs;
        }
        com.destroystokyo.paper.event.entity.TurtleLayEggEvent event = new com.destroystokyo.paper.event.entity.TurtleLayEggEvent((org.bukkit.entity.Turtle) bukkit(turtle),
            org.bukkit.craftbukkit.v.util.CraftLocation.toBukkit(pos, ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) turtle.level()).bridge$getWorld()), eggs);
        return call(event) ? event.getEggCount() : 0;
    }

    public static boolean turtleGoHome(Entity turtle) {
        if (!listened(com.destroystokyo.paper.event.entity.TurtleGoHomeEvent.getHandlerList())) {
            return true;
        }
        return call(new com.destroystokyo.paper.event.entity.TurtleGoHomeEvent((org.bukkit.entity.Turtle) bukkit(turtle)));
    }

    /** WardenAngerChangeEvent: the amount of anger to add, or null when cancelled. */
    public static @Nullable Integer wardenAnger(Entity warden, Entity target, int activeAnger, int amount) {
        if (!listened(io.papermc.paper.event.entity.WardenAngerChangeEvent.getHandlerList())) {
            return amount;
        }
        io.papermc.paper.event.entity.WardenAngerChangeEvent event = new io.papermc.paper.event.entity.WardenAngerChangeEvent((org.bukkit.entity.Warden) bukkit(warden),
            bukkit(target), activeAnger, Math.min(150, activeAnger + amount));
        return call(event) ? event.getNewAnger() - activeAnger : null;
    }
}
