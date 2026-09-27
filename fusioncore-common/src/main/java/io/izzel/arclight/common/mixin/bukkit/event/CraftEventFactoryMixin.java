package io.izzel.arclight.common.mixin.bukkit.event;

import com.google.common.base.Function;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.bridge.core.world.damagesource.DamageSourceBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mod.server.event.ArclightEventFactory;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.common.mod.util.DistValidate;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.bukkit.craftbukkit.v.block.CraftBlockStates;
import org.bukkit.craftbukkit.v.block.CraftSign;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.damage.CraftDamageSource;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Mixin(value = CraftEventFactory.class, remap = false)
public abstract class CraftEventFactoryMixin {

    // @formatter:off
    @Shadow private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, EntityDamageEvent.DamageCause cause, org.bukkit.damage.DamageSource bukkitDamageSource, Map<EntityDamageEvent.DamageModifier, Double> modifiers, Map<EntityDamageEvent.DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) { return null; }
    // @formatter:on

    @ModifyVariable(method = "handleEntityDamageEvent*", at = @At("HEAD"), index = 1, argsOnly = true)
    private static DamageSource arclight$captureSource(DamageSource source, Entity entity) {
        Entity damageEventEntity = ArclightCaptures.getDamageEventEntity();
        BlockPos damageEventBlock = ArclightCaptures.getDamageEventBlock();
        if (damageEventEntity != null && ((DamageSourceBridge) source).bridge$getCausingEntity() == null) {
            if (source.is(DamageTypes.LIGHTNING_BOLT)) {
                source = ((DamageSourceBridge) source).bridge$customCausingEntity(damageEventEntity);
            }
        }
        if (damageEventBlock != null && ((DamageSourceBridge) source).bridge$directBlock() == null) {
            if (source.is(DamageTypes.CACTUS)
                    || source.is(DamageTypes.SWEET_BERRY_BUSH)
                    || source.is(DamageTypes.HOT_FLOOR)) {
                source = ((DamageSourceBridge) source).bridge$directBlock(CraftBlock.at(entity.getCommandSenderWorld(), damageEventBlock));
            }
        }
        return source;
    }

    // Fusion Core: Paper's critical damage (patch 0588). The source's flag reaches the event built a few calls down; plugins'
    // listeners (which may cause nested damage) only run after the event is built, so one field is enough.
    @org.spongepowered.asm.mixin.Unique private static boolean fusioncore$critical;

    @Inject(method = "handleEntityDamageEvent*", at = @At("HEAD"))
    private static void fusioncore$captureCritical(Entity entity, DamageSource source, Map<EntityDamageEvent.DamageModifier, Double> modifiers, Map<EntityDamageEvent.DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled, CallbackInfoReturnable<EntityDamageEvent> cir) {
        fusioncore$critical = ((DamageSourceBridge) source).bridge$isCritical();
    }

    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method = "callEntityDamageEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;Lorg/bukkit/damage/DamageSource;Ljava/util/Map;Ljava/util/Map;Z)Lorg/bukkit/event/entity/EntityDamageEvent;",
        at = @At(value = "NEW", target = "org/bukkit/event/entity/EntityDamageByEntityEvent"))
    private static org.bukkit.event.entity.EntityDamageByEntityEvent fusioncore$critical(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        ((io.izzel.arclight.common.bridge.bukkit.EntityDamageByEntityEventBridge) event).bridge$setCritical(fusioncore$critical);
        fusioncore$critical = false;
        return event;
    }

    @Inject(method = "handleEntityDamageEvent*", cancellable = true, at = @At(value = "NEW", target = "java/lang/IllegalStateException"))
    private static void arclight$unhandledDamage(Entity entity, DamageSource source, Map<EntityDamageEvent.DamageModifier, Double> modifiers, Map<EntityDamageEvent.DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled, CallbackInfoReturnable<EntityDamageEvent> cir) {
        // todo blockDamage is lost
        CraftDamageSource bukkitDamageSource = new CraftDamageSource(source);
        EntityDamageEvent event = callEntityDamageEvent(((DamageSourceBridge) source).bridge$getCausingEntity(), entity, EntityDamageEvent.DamageCause.CUSTOM, bukkitDamageSource, modifiers, modifierFunctions, cancelled);
        cir.setReturnValue(event);
    }

    @Decorate(method = "callPlayerInteractEvent*", at = @At(value = "INVOKE", target = "Lorg/bukkit/plugin/PluginManager;callEvent(Lorg/bukkit/event/Event;)V"))
    private static void arclight$cancelPlayerInteractIfNecessary(PluginManager instance, Event event) throws Throwable {
        if (ArclightCaptures.shouldCancelPlayerInteract()) {
            ((Cancellable) event).setCancelled(true);
        }
        DecorationOps.callsite().invoke(instance, event);
    }

    /**
     * @author InitAuther97
     * @reason
     */
    @Overwrite
    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.world.entity.LivingEntity victim, DamageSource damageSource, List<ItemStack> drops) {
        LivingEntityBridge living = (LivingEntityBridge) victim;
        CraftLivingEntity craft = living.bridge$getBukkitEntity();
        EntityDeathEvent event = ArclightEventFactory.callEntityDeathEvent(victim, damageSource, drops);
        // Fusion Core: Paper's cancellable death (patch 0242)
        if (io.izzel.arclight.common.mod.util.FusionCoreDeathEvents.isCancelled(event)) {
            return event;
        }
        io.izzel.arclight.common.mod.util.FusionCoreDeathEvents.playDeathSound(victim, event);

        CraftWorld world = (CraftWorld) craft.getWorld();
        living.bridge$setExpToDrop(event.getDroppedExp());
        for(org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack != null && stack.getType() != Material.AIR && stack.getAmount() != 0) {
                world.dropItem(craft.getLocation(), stack);
            }
        }
        return event;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public static boolean handleBlockSpreadEvent(LevelAccessor world, BlockPos source, BlockPos target, net.minecraft.world.level.block.state.BlockState block, int flag) {
        // Suppress during worldgen
        if (!(world instanceof Level) || !DistValidate.isValid(world)) {
            world.setBlock(target, block, flag);
            return true;
        }

        CraftBlockState state = CraftBlockStates.getBlockState(world, target, flag);
        state.setData(block);

        BlockSpreadEvent event = new BlockSpreadEvent(state.getBlock(), CraftBlock.at(world, source), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public static boolean handleBlockGrowEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newData, int flag) {
        // Suppress during worldgen
        if (!DistValidate.isValid(world)) {
            world.setBlock(pos, newData, flag);
            return true;
        }
        Block block = world.bridge$getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        CraftBlockState state = (CraftBlockState) block.getState();
        state.setData(newData);

        BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }

        return !event.isCancelled();
    }

    /**
     * @author IzzelAliz, InitAuther97
     * @reason IzzelAliz: suppress during world generation; InitAuther97: use extracted logic
     */
    @Overwrite
    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block, int flag, @Nullable Entity entity) {
        // Suppressed in callBlockFormEvent
        final var event = ArclightEventFactory.callBlockFormEvent(world, pos, block, flag, entity);
        if (event == null) {
            world.setBlock(pos, block, flag);
            return true;
        }

        if (!event.isCancelled()) {
            event.getNewState().update(true);
        }

        return !event.isCancelled();
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public static BlockFadeEvent callBlockFadeEvent(LevelAccessor world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newBlock) {
        // Suppress during worldgen
        if (!(world instanceof Level) || !DistValidate.isValid(world)) {
            return new BlockFadeEvent(CraftBlock.at(world, pos), null);
        }
        CraftBlockState state = CraftBlockStates.getBlockState(world, pos);
        state.setData(newBlock);

        BlockFadeEvent event = new BlockFadeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public static BlockPhysicsEvent callBlockPhysicsEvent(LevelAccessor world, BlockPos blockposition) {
        org.bukkit.block.Block block = CraftBlock.at(world, blockposition);
        BlockPhysicsEvent event = new BlockPhysicsEvent(block, block.getBlockData());
        // Suppress during worldgen
        if (world instanceof Level && DistValidate.isValid(world)) {
            Bukkit.getPluginManager().callEvent(event);
        }
        return event;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public static boolean callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.world.level.block.state.BlockState newBlock, boolean cancelled) {
        Block block = CraftBlock.at(entity.level(), position);
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity.bridge$getBukkitEntity(), block, CraftBlockData.fromData(newBlock));
        event.setCancelled(cancelled);
        // Suppress during worldgen
        if (DistValidate.isValid(entity.level())) {
            Bukkit.getPluginManager().callEvent(event);
        }
        return !event.isCancelled();
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public static BlockRedstoneEvent callRedstoneChange(Level world, BlockPos pos, int oldCurrent, int newCurrent) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(CraftBlock.at(world, pos), oldCurrent, newCurrent);
        // Suppress during worldgen
        if (DistValidate.isValid(world)) {
            Bukkit.getPluginManager().callEvent(event);
        }
        return event;
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public static NotePlayEvent callNotePlayEvent(Level world, BlockPos pos, NoteBlockInstrument instrument, int note) {
        NotePlayEvent event = new NotePlayEvent(CraftBlock.at(world, pos), org.bukkit.Instrument.getByType((byte) instrument.ordinal()), new org.bukkit.Note(note));
        // Suppress during worldgen
        if (DistValidate.isValid(world)) {
            Bukkit.getPluginManager().callEvent(event);
        }
        return event;
    }

    @Inject(method = "callItemSpawnEvent", cancellable = true, at = @At("HEAD"))
    private static void arclight$noAirDrops(ItemEntity itemEntity, CallbackInfoReturnable<ItemSpawnEvent> cir) {
        if (itemEntity.getItem().isEmpty()) {
            Item entity = (Item) itemEntity.bridge$getBukkitEntity();
            ItemSpawnEvent event = new ItemSpawnEvent(entity);
            event.setCancelled(true);
            cir.setReturnValue(event);
        }
    }

    /**
     * @author sj-hub9796
     * @reason
     */
    @Overwrite
    public static boolean callPlayerSignOpenEvent(net.minecraft.world.entity.player.Player player, SignBlockEntity tileEntitySign, boolean front, PlayerSignOpenEvent.Cause cause) {
        Block block = CraftBlock.at(tileEntitySign.getLevel(), tileEntitySign.getBlockPos());
        Sign sign;
        if (CraftBlockStates.getBlockState(block) instanceof Sign sign1) {
            sign = sign1;
        } else {
            sign = new CraftSign<>(((WorldBridge) tileEntitySign.getLevel()).bridge$getWorld(), tileEntitySign);
        }
        Side side = front ? Side.FRONT : Side.BACK;
        return callPlayerSignOpenEvent(((ServerPlayerBridge) player).bridge$getBukkitEntity(), sign, side, cause);
    }

    /**
     * @author sj-hub9796
     * @reason
     */
    @Overwrite
    public static boolean callPlayerSignOpenEvent(Player player, Sign sign, Side side, PlayerSignOpenEvent.Cause cause) {
        // Fusion Core: Paper's PlayerOpenSignEvent first, Spigot's only when someone listens (Paper patch 0826)
        io.papermc.paper.event.player.PlayerOpenSignEvent paperEvent = new io.papermc.paper.event.player.PlayerOpenSignEvent(player, sign, side, switch (cause) {
            case PLACE -> io.papermc.paper.event.player.PlayerOpenSignEvent.Cause.PLACE;
            case PLUGIN -> io.papermc.paper.event.player.PlayerOpenSignEvent.Cause.PLUGIN;
            case INTERACT -> io.papermc.paper.event.player.PlayerOpenSignEvent.Cause.INTERACT;
            default -> io.papermc.paper.event.player.PlayerOpenSignEvent.Cause.UNKNOWN;
        });
        Bukkit.getPluginManager().callEvent(paperEvent);
        if (paperEvent.isCancelled()) {
            return false;
        }
        if (PlayerSignOpenEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return true;
        }
        PlayerSignOpenEvent event = new PlayerSignOpenEvent(player, sign, side, cause);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    // Fusion Core: Paper's InventoryOpenEvent#titleOverride (patch 0874) goes onto the menu; the open-screen packet uses it
    @Inject(method = "callInventoryOpenEvent(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/inventory/AbstractContainerMenu;Z)Lnet/minecraft/world/inventory/AbstractContainerMenu;", at = @At("RETURN"))
    private static void fusioncore$titleOverride(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.inventory.AbstractContainerMenu container, boolean cancelled,
                                               org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.world.inventory.AbstractContainerMenu> cir,
                                               @com.llamalad7.mixinextras.sugar.Local org.bukkit.event.inventory.InventoryOpenEvent event) {
        if (cir.getReturnValue() != null) {
            net.kyori.adventure.text.Component title = io.izzel.arclight.common.mod.util.FusionCoreInventories.titleOverride(event);
            if (title != null) {
                ((io.izzel.arclight.common.bridge.core.world.inventory.AbstractContainerMenuBridge) cir.getReturnValue())
                    .fusioncore$setTitleOverride(io.izzel.arclight.common.mod.util.FusionCoreAdventure.asVanilla(title));
            }
        }
    }

    // Fusion Core: Paper's close reason (Paper patch 0208) for the container closed when another one opens
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "callInventoryOpenEvent(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/inventory/AbstractContainerMenu;Z)Lnet/minecraft/world/inventory/AbstractContainerMenu;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;handleContainerClose(Lnet/minecraft/network/protocol/game/ServerboundContainerClosePacket;)V"))
    private static void fusioncore$closeForNew(net.minecraft.server.network.ServerGamePacketListenerImpl connection, net.minecraft.network.protocol.game.ServerboundContainerClosePacket packet, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        io.izzel.arclight.common.mod.util.FusionCoreEvents.closeWith(org.bukkit.event.inventory.InventoryCloseEvent$Reason.OPEN_NEW, () -> original.call(connection, packet));
    }

    // Fusion Core: Paper's own TNTPrimeEvent (Paper patch 0221) once Spigot's went through
    @Inject(method = "callTNTPrimeEvent", cancellable = true, at = @At("RETURN"))
    private static void fusioncore$paperTntPrime(net.minecraft.world.level.Level world, BlockPos pos, org.bukkit.event.block.TNTPrimeEvent.PrimeCause cause,
                                               Entity causingEntity, BlockPos causePosition, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && !io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents.paperTntPrime(world, pos, cause, causingEntity)) {
            cir.setReturnValue(false);
        }
    }

    // Fusion Core: Paper's knockback events (Paper patch 0202) after Spigot's
    @Inject(method = "callEntityKnockbackEvent", at = @At("RETURN"))
    private static void fusioncore$paperKnockback(org.bukkit.craftbukkit.v.entity.CraftLivingEntity entity, Entity attacker, org.bukkit.event.entity.EntityKnockbackEvent.KnockbackCause cause,
                                                double force, net.minecraft.world.phys.Vec3 raw, double x, double y, double z,
                                                org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<org.bukkit.event.entity.EntityKnockbackEvent> cir) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.paperKnockback(cir.getReturnValue(), attacker, force);
    }

    // Fusion Core: Paper fires its deprecated io.papermc.paper.event.block.BellRingEvent, a subclass of Spigot's, so
    // listeners of either get it (Paper patch 0399)
    @SuppressWarnings("deprecation")
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "handleBellRingEvent", at = @At(value = "NEW", target = "org/bukkit/event/block/BellRingEvent"))
    private static org.bukkit.event.block.BellRingEvent fusioncore$paperBellRing(org.bukkit.block.Block block, org.bukkit.block.BlockFace direction, org.bukkit.entity.Entity entity,
                                                                               com.llamalad7.mixinextras.injector.wrapoperation.Operation<org.bukkit.event.block.BellRingEvent> original) {
        return new io.papermc.paper.event.block.BellRingEvent(block, direction, entity);

    }

    // Fusion Core: Spigot's exp merge on spawn (merge-radius.exp) with Paper's rules (Paper patch 0355): only orbs of the
    // same count merge, and ExperienceOrbMergeEvent may stop it
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "doEntityAddEventCalling", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;isRemoved()Z"))
    private static boolean fusioncore$orbMergeOnSpawn(net.minecraft.world.entity.ExperienceOrb orb, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original,
                                                    @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) Entity entity) {
        if (original.call(orb)) {
            return true;
        }
        if (entity instanceof net.minecraft.world.entity.ExperienceOrb xp) {
            return ((io.izzel.arclight.common.mixin.core.world.entity.ExperienceOrbAccessor) xp).fusioncore$getCount() != ((io.izzel.arclight.common.mixin.core.world.entity.ExperienceOrbAccessor) orb).fusioncore$getCount()
                || !io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents.orbMerge(xp, orb);
        }
        return false;
    }

    // Fusion Core: Paper's deprecated ProjectileCollideEvent (Paper patch 0113) before ProjectileHitEvent when an entity is
    // hit; its cancellation carries over to ProjectileHitEvent
    @SuppressWarnings("deprecation")
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "callProjectileHitEvent", at = @At(value = "NEW", target = "org/bukkit/event/entity/ProjectileHitEvent"))
    private static org.bukkit.event.entity.ProjectileHitEvent fusioncore$projectileCollide(org.bukkit.entity.Projectile projectile, org.bukkit.entity.Entity hitEntity, org.bukkit.block.Block hitBlock,
                                                                                         org.bukkit.block.BlockFace hitFace, com.llamalad7.mixinextras.injector.wrapoperation.Operation<org.bukkit.event.entity.ProjectileHitEvent> original) {
        org.bukkit.event.entity.ProjectileHitEvent event = original.call(projectile, hitEntity, hitBlock, hitFace);
        if (hitEntity != null && com.destroystokyo.paper.event.entity.ProjectileCollideEvent.getHandlerList().getRegisteredListeners().length > 0) {
            com.destroystokyo.paper.event.entity.ProjectileCollideEvent collide = new com.destroystokyo.paper.event.entity.ProjectileCollideEvent(projectile, hitEntity);
            Bukkit.getPluginManager().callEvent(collide);
            event.setCancelled(collide.isCancelled());
        }
        return event;
    }

    // Fusion Core: the hand of a multi-block placement in BlockMultiPlaceEvent, as Paper (patch 0319)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "callBlockMultiPlaceEvent", remap = false,
        at = @At(value = "NEW", remap = false, target = "org/bukkit/event/block/BlockMultiPlaceEvent"))
    private static org.bukkit.event.block.BlockMultiPlaceEvent fusioncore$multiPlaceHand(List<org.bukkit.block.BlockState> states, Block clicked, ItemStack item,
                                                                                  Player player, boolean canBuild,
                                                                                  com.llamalad7.mixinextras.injector.wrapoperation.Operation<org.bukkit.event.block.BlockMultiPlaceEvent> original,
                                                                                  net.minecraft.server.level.ServerLevel world, net.minecraft.world.entity.player.Player who,
                                                                                  net.minecraft.world.InteractionHand hand, List<org.bukkit.block.BlockState> blockStates,
                                                                                  int clickedX, int clickedY, int clickedZ) {
        return io.izzel.arclight.common.mod.util.FusionCoreEvents.newBlockMultiPlaceEvent(states, clicked, item, player, canBuild,
            org.bukkit.craftbukkit.v.CraftEquipmentSlot.getHand(hand));
    }
}
