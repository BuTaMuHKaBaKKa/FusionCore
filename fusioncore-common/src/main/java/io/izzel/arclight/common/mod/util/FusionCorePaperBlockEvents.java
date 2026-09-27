package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's block events fired from the core. Events are built only when someone listens. */
public final class FusionCorePaperBlockEvents {

    private FusionCorePaperBlockEvents() {
    }

    /**
     * Paper's (deprecated) {@code com.destroystokyo.paper.event.block.TNTPrimeEvent}, fired after Spigot's
     * {@code TNTPrimeEvent} went through, for the causes Paper fires it for. False when a listener cancelled it.
     */
    public static boolean paperTntPrime(LevelAccessor level, BlockPos pos, org.bukkit.event.block.TNTPrimeEvent.PrimeCause cause, @Nullable Entity source) {
        com.destroystokyo.paper.event.block.TNTPrimeEvent.PrimeReason reason = switch (cause) {
            case FIRE -> com.destroystokyo.paper.event.block.TNTPrimeEvent.PrimeReason.FIRE;
            case REDSTONE -> com.destroystokyo.paper.event.block.TNTPrimeEvent.PrimeReason.REDSTONE;
            case EXPLOSION -> com.destroystokyo.paper.event.block.TNTPrimeEvent.PrimeReason.EXPLOSION;
            case PLAYER -> com.destroystokyo.paper.event.block.TNTPrimeEvent.PrimeReason.ITEM;
            case PROJECTILE -> com.destroystokyo.paper.event.block.TNTPrimeEvent.PrimeReason.PROJECTILE;
            default -> null; // BLOCK_BREAK, DISPENSER: Paper does not fire its event for these
        };
        if (reason == null || com.destroystokyo.paper.event.block.TNTPrimeEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return true;
        }
        org.bukkit.entity.Entity primer = source == null || reason == com.destroystokyo.paper.event.block.TNTPrimeEvent.PrimeReason.REDSTONE
            || reason == com.destroystokyo.paper.event.block.TNTPrimeEvent.PrimeReason.FIRE ? null : ((EntityBridge) source).bridge$getBukkitEntity();
        com.destroystokyo.paper.event.block.TNTPrimeEvent event = new com.destroystokyo.paper.event.block.TNTPrimeEvent(CraftBlock.at(level, pos), reason, primer);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /** Paper's BlockPreDispenseEvent: false when a listener cancelled the dispense of that slot. */
    public static boolean preDispense(LevelAccessor level, BlockPos pos, ItemStack stack, int slot) {
        if (io.papermc.paper.event.block.BlockPreDispenseEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return true;
        }
        io.papermc.paper.event.block.BlockPreDispenseEvent event = new io.papermc.paper.event.block.BlockPreDispenseEvent(CraftBlock.at(level, pos), CraftItemStack.asCraftMirror(stack), slot);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static void beaconActivated(LevelAccessor level, BlockPos pos) {
        Bukkit.getPluginManager().callEvent(new io.papermc.paper.event.block.BeaconActivatedEvent(CraftBlock.at(level, pos)));
    }

    public static void beaconDeactivated(LevelAccessor level, BlockPos pos) {
        Bukkit.getPluginManager().callEvent(new io.papermc.paper.event.block.BeaconDeactivatedEvent(CraftBlock.at(level, pos)));
    }

    /**
     * Paper's BeaconEffectEvent (patch 0049) for one player: the effect to give (a listener may replace it), or null
     * when a listener cancelled it.
     */
    public static @Nullable net.minecraft.world.effect.MobEffectInstance beaconEffect(net.minecraft.world.entity.player.Player player, net.minecraft.world.effect.MobEffectInstance effect,
                                                                                   LevelAccessor level, BlockPos pos, boolean primary) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer)
            || com.destroystokyo.paper.event.block.BeaconEffectEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return effect;
        }
        com.destroystokyo.paper.event.block.BeaconEffectEvent event = new com.destroystokyo.paper.event.block.BeaconEffectEvent(CraftBlock.at(level, pos),
            org.bukkit.craftbukkit.v.potion.CraftPotionUtil.toBukkit(effect), (org.bukkit.entity.Player) ((EntityBridge) player).bridge$getBukkitEntity(), primary);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled() ? null : org.bukkit.craftbukkit.v.potion.CraftPotionUtil.fromBukkit(event.getEffect());
    }

    /** Paper's AnvilDamagedEvent (patch 0220). True when the event decided the outcome. */
    public static boolean anvilDamaged(net.minecraft.world.entity.player.Player player, net.minecraft.world.level.Level level, BlockPos pos,
                                       net.minecraft.world.level.block.state.BlockState state) {
        if (com.destroystokyo.paper.event.block.AnvilDamagedEvent.getHandlerList().getRegisteredListeners().length == 0
            || !(player.containerMenu instanceof net.minecraft.world.inventory.AnvilMenu menu)) {
            return false;
        }
        net.minecraft.world.level.block.state.BlockState damaged = net.minecraft.world.level.block.AnvilBlock.damage(state);
        com.destroystokyo.paper.event.block.AnvilDamagedEvent event = new com.destroystokyo.paper.event.block.AnvilDamagedEvent(
            ((io.izzel.arclight.common.bridge.core.world.inventory.AbstractContainerMenuBridge) menu).bridge$getBukkitView(),
            damaged != null ? org.bukkit.craftbukkit.v.block.data.CraftBlockData.fromData(damaged) : null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return true;
        }
        net.minecraft.world.level.block.state.BlockState result;
        if (event.getDamageState() == com.destroystokyo.paper.event.block.AnvilDamagedEvent.DamageState.BROKEN) {
            result = null;
        } else {
            result = ((org.bukkit.craftbukkit.v.block.data.CraftBlockData) event.getDamageState().getMaterial().createBlockData()).getState()
                .setValue(net.minecraft.world.level.block.AnvilBlock.FACING, state.getValue(net.minecraft.world.level.block.AnvilBlock.FACING));
        }
        if (result == damaged) {
            return false;
        }
        if (result == null) {
            level.removeBlock(pos, false);
            level.levelEvent(1029, pos, 0);
        } else {
            level.setBlock(pos, result, 2);
            level.levelEvent(1030, pos, 0);
        }
        return true;
    }

    private static boolean listened(org.bukkit.event.HandlerList handlers) {
        return handlers.getRegisteredListeners().length > 0;
    }

    /** Paper's BellRevealRaiderEvent (patch 0548): false when a listener keeps the bell from making this raider glow. */
    public static boolean bellRevealRaider(LevelAccessor level, BlockPos pos, net.minecraft.world.entity.LivingEntity entity) {
        if (!listened(io.papermc.paper.event.block.BellRevealRaiderEvent.getHandlerList())
            || !(((EntityBridge) entity).bridge$getBukkitEntity() instanceof org.bukkit.entity.Raider raider)) {
            return true; // Paper casts blindly; a mod entity in the raiders tag that is not a Raider gets no event
        }
        io.papermc.paper.event.block.BellRevealRaiderEvent event = new io.papermc.paper.event.block.BellRevealRaiderEvent(CraftBlock.at(level, pos), raider);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private static @Nullable BlockPos breakSource;

    /**
     * Runs {@code action} (which drops a block's resources through {@code Block.dropResources}) with the block that
     * broke it, for Paper's BlockBreakBlockEvent (patch 0578): a piston, or the fluid flowing in.
     */
    public static void withBreakSource(BlockPos source, Runnable action) {
        BlockPos previous = breakSource;
        breakSource = source;
        try {
            action.run();
        } finally {
            breakSource = previous;
        }
    }

    /**
     * Paper's BlockBreakBlockEvent (patch 0578): the drops of a block broken by another block, as listeners left them.
     * Unchanged outside {@link #withBreakSource}. The drops still go through the loader's own drop handling.
     */
    public static java.util.List<ItemStack> breakBlockDrops(java.util.List<ItemStack> drops, LevelAccessor level, BlockPos pos) {
        BlockPos source = breakSource;
        if (source == null || !listened(io.papermc.paper.event.block.BlockBreakBlockEvent.getHandlerList())) {
            return drops;
        }
        breakSource = null; // a listener breaking other blocks does not inherit this source
        try {
            java.util.List<org.bukkit.inventory.ItemStack> items = new java.util.ArrayList<>(drops.size());
            for (ItemStack drop : drops) {
                items.add(CraftItemStack.asBukkitCopy(drop));
            }
            io.papermc.paper.event.block.BlockBreakBlockEvent event = new io.papermc.paper.event.block.BlockBreakBlockEvent(CraftBlock.at(level, pos), CraftBlock.at(level, source), items);
            Bukkit.getPluginManager().callEvent(event);
            java.util.List<ItemStack> result = new java.util.ArrayList<>(event.getDrops().size());
            for (org.bukkit.inventory.ItemStack drop : event.getDrops()) {
                ItemStack stack = CraftItemStack.asNMSCopy(drop);
                if (!stack.isEmpty()) {
                    result.add(stack);
                }
            }
            return result;
        } finally {
            breakSource = source;
        }
    }

    /** Paper's BlockBreakProgressUpdateEvent (patch 0933), for progress sent on behalf of a real entity. */
    public static void breakProgress(net.minecraft.server.level.ServerLevel level, int breakerId, BlockPos pos, int progress) {
        if (!listened(io.papermc.paper.event.block.BlockBreakProgressUpdateEvent.getHandlerList())) {
            return;
        }
        Entity breaker = level.getEntity(breakerId);
        if (breaker == null) {
            return; // a plugin sending progress for a client-side entity id: nothing happened on the server
        }
        float value = net.minecraft.util.Mth.clamp(progress, 0, 10) / 10.0F;
        Bukkit.getPluginManager().callEvent(new io.papermc.paper.event.block.BlockBreakProgressUpdateEvent(CraftBlock.at(level, pos), value,
            ((EntityBridge) breaker).bridge$getBukkitEntity()));
    }

    /** Paper's BlockFailedDispenseEvent (patch 0463): false when the "click" effect of an empty dispenser/dropper is not to be played. */
    public static boolean failedDispense(LevelAccessor level, BlockPos pos) {
        if (!listened(io.papermc.paper.event.block.BlockFailedDispenseEvent.getHandlerList())) {
            return true;
        }
        return new io.papermc.paper.event.block.BlockFailedDispenseEvent(CraftBlock.at(level, pos)).callEvent();
    }

    /**
     * Paper's BlockLockCheckEvent (patch 0778): whether the player may open the locked (or not) container, or null
     * when vanilla's check applies unchanged (nobody listens, not a player, the block entity is not in its world).
     */
    public static @Nullable Boolean lockCheck(net.minecraft.world.entity.player.Player player, net.minecraft.world.LockCode lock,
                                              net.minecraft.network.chat.Component containerName, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        if (!listened(io.papermc.paper.event.block.BlockLockCheckEvent.getHandlerList())
            || !(player instanceof net.minecraft.server.level.ServerPlayer)
            || blockEntity.getLevel() == null || blockEntity.getLevel().getBlockEntity(blockEntity.getBlockPos()) != blockEntity) {
            return null;
        }
        net.kyori.adventure.text.Component lockedMessage = net.kyori.adventure.text.Component.translatable("container.isLocked", FusionCoreAdventure.asAdventure(containerName));
        net.kyori.adventure.sound.Sound lockedSound = net.kyori.adventure.sound.Sound.sound(net.kyori.adventure.key.Key.key("block.chest.locked"),
            net.kyori.adventure.sound.Sound.Source.BLOCK, 1.0F, 1.0F);
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((EntityBridge) player).bridge$getBukkitEntity();
        io.papermc.paper.event.block.BlockLockCheckEvent event = new io.papermc.paper.event.block.BlockLockCheckEvent(
            CraftBlock.at(blockEntity.getLevel(), blockEntity.getBlockPos()), bukkitPlayer, lockedMessage, lockedSound);
        Bukkit.getPluginManager().callEvent(event);
        if (event.getResult() == org.bukkit.event.Event.Result.ALLOW) {
            return true;
        }
        if (event.getResult() == org.bukkit.event.Event.Result.DENY || (!player.isSpectator()
            && !lock.unlocksWith(event.isUsingCustomKeyItemStack() ? CraftItemStack.asNMSCopy(event.getKeyItem()) : player.getMainHandItem()))) {
            net.kyori.adventure.audience.Audience audience = (net.kyori.adventure.audience.Audience) (Object) event.getPlayer();
            if (event.getLockedMessage() != null) {
                audience.sendActionBar(event.getLockedMessage());
            }
            if (event.getLockedSound() != null) {
                audience.playSound(event.getLockedSound());
            }
            return false;
        }
        return true;
    }

    private static boolean compostCancelled;

    /** Paper's CompostItemEvent/EntityCompostItemEvent (patch 0797). Returns the chance vanilla continues with. */
    public static float compost(float chance, @Nullable Entity entity, net.minecraft.world.level.block.state.BlockState state, LevelAccessor level,
                                BlockPos pos, ItemStack stack) {
        compostCancelled = false;
        if (!listened(io.papermc.paper.event.block.CompostItemEvent.getHandlerList())
            && !listened(io.papermc.paper.event.entity.EntityCompostItemEvent.getHandlerList())) {
            return chance;
        }
        int fill = state.getValue(net.minecraft.world.level.block.ComposterBlock.LEVEL);
        boolean willRaiseLevel = !((fill != 0 || chance <= 0.0F) && level.getRandom().nextDouble() >= (double) chance);
        io.papermc.paper.event.block.CompostItemEvent event;
        if (entity == null) {
            event = new io.papermc.paper.event.block.CompostItemEvent(CraftBlock.at(level, pos), CraftItemStack.asCraftMirror(stack), willRaiseLevel);
        } else {
            event = new io.papermc.paper.event.entity.EntityCompostItemEvent(((EntityBridge) entity).bridge$getBukkitEntity(), CraftBlock.at(level, pos),
                CraftItemStack.asCraftMirror(stack), willRaiseLevel);
        }
        Bukkit.getPluginManager().callEvent(event);
        if (event instanceof org.bukkit.event.Cancellable cancellable && cancellable.isCancelled()) {
            compostCancelled = true; // hoppers cannot cancel: CompostItemEvent is not Cancellable
            return 0.0F;
        }
        return event.willRaiseLevel() ? 2.0F : 0.0F;
    }

    /** Whether the last {@code ComposterBlock.addItem} had its entity's EntityCompostItemEvent cancelled (Paper: addItem returned null). */
    public static boolean takeCompostCancelled() {
        boolean cancelled = compostCancelled;
        compostCancelled = false;
        return cancelled;
    }

    /**
     * Paper's DragonEggFormEvent (patch 0483), fired when the dragon dies; cancelled up front when vanilla would not place
     * the egg (the dragon was killed before). Places the egg state the listeners left.
     */
    public static void dragonEggForm(net.minecraft.world.level.dimension.end.EndDragonFight fight, net.minecraft.server.level.ServerLevel level, BlockPos origin,
                                     boolean previouslyKilled) {
        BlockPos eggPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
            net.minecraft.world.level.levelgen.feature.EndPodiumFeature.getLocation(origin));
        org.bukkit.craftbukkit.v.block.CraftBlockState eggState = org.bukkit.craftbukkit.v.block.CraftBlockStates.getBlockState(level, eggPos);
        eggState.setData(net.minecraft.world.level.block.Blocks.DRAGON_EGG.defaultBlockState());
        io.papermc.paper.event.block.DragonEggFormEvent event = new io.papermc.paper.event.block.DragonEggFormEvent(CraftBlock.at(level, eggPos), eggState,
            new org.bukkit.craftbukkit.v.boss.CraftDragonBattle(fight));
        event.setCancelled(previouslyKilled);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            event.getNewState().update(true);
        }
    }

    public static boolean dragonEggFormListened() {
        return listened(io.papermc.paper.event.block.DragonEggFormEvent.getHandlerList());
    }

    private static boolean targetHitCancelled;

    /**
     * Paper's TargetHitEvent (patch 0448): the redstone strength a projectile hitting a target block gives, as listeners
     * left it. When cancelled, {@link #takeTargetHitCancelled} is true and the target neither powers nor awards the hit.
     */
    public static int targetHit(int strength, LevelAccessor level, net.minecraft.world.phys.BlockHitResult hit, Entity entity) {
        targetHitCancelled = false;
        if (!(entity instanceof net.minecraft.world.entity.projectile.Projectile)
            || !listened(io.papermc.paper.event.block.TargetHitEvent.getHandlerList())) {
            return strength;
        }
        io.papermc.paper.event.block.TargetHitEvent event = new io.papermc.paper.event.block.TargetHitEvent(
            (org.bukkit.entity.Projectile) ((EntityBridge) entity).bridge$getBukkitEntity(), CraftBlock.at(level, hit.getBlockPos()),
            CraftBlock.notchToBlockFace(hit.getDirection()), strength);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            targetHitCancelled = true;
            return strength;
        }
        return event.getSignalStrength();
    }

    public static boolean targetHitCancelled() {
        return targetHitCancelled;
    }

    public static boolean takeTargetHitCancelled() {
        boolean cancelled = targetHitCancelled;
        targetHitCancelled = false;
        return cancelled;
    }

    /**
     * Paper's PlayerShearBlockEvent (patch 0440) for a player shearing a beehive or a pumpkin: the drops as listeners
     * left them, null when cancelled, or {@link #NOT_FIRED} when nobody listens (vanilla drops its own items).
     */
    public static final java.util.List<ItemStack> NOT_FIRED = java.util.List.of();

    public static @Nullable java.util.List<ItemStack> shearBlock(net.minecraft.world.entity.player.Player player, LevelAccessor level, BlockPos pos,
                                                                 ItemStack tool, net.minecraft.world.InteractionHand hand, ItemStack defaultDrop) {
        if (!listened(io.papermc.paper.event.block.PlayerShearBlockEvent.getHandlerList())) {
            return NOT_FIRED;
        }
        java.util.List<org.bukkit.inventory.ItemStack> drops = new java.util.ArrayList<>();
        drops.add(CraftItemStack.asCraftMirror(defaultDrop));
        io.papermc.paper.event.block.PlayerShearBlockEvent event = new io.papermc.paper.event.block.PlayerShearBlockEvent(
            (org.bukkit.entity.Player) ((EntityBridge) player).bridge$getBukkitEntity(), CraftBlock.at(level, pos), CraftItemStack.asCraftMirror(tool),
            org.bukkit.craftbukkit.v.CraftEquipmentSlot.getHand(hand), drops);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        java.util.List<ItemStack> result = new java.util.ArrayList<>(event.getDrops().size());
        for (org.bukkit.inventory.ItemStack drop : event.getDrops()) {
            ItemStack stack = CraftItemStack.asNMSCopy(drop);
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result;
    }
}
