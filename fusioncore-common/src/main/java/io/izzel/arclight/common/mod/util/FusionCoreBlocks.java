package io.izzel.arclight.common.mod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/** Fusion Core: bodies of Paper's Block additions that need the vanilla level. */
public final class FusionCoreBlocks {

    private FusionCoreBlocks() {
    }

    /**
     * Paper's {@code CraftBlock#breakNaturally(ItemStack, boolean, boolean)}: Spigot's break (drops for the tool),
     * plus the break particles/sound and the block's experience, as a player breaking it would get.
     */
    public static boolean breakNaturally(Block block, ItemStack tool, boolean triggerEffect, boolean dropExperience) {
        CraftBlock craft = (CraftBlock) block;
        ServerLevel level = ((org.bukkit.craftbukkit.v.CraftWorld) block.getWorld()).getHandle();
        BlockPos pos = craft.getPosition();
        BlockState state = level.getBlockState(pos);
        int exp = dropExperience ? ((io.izzel.arclight.common.bridge.core.world.level.block.BlockBridge) state.getBlock())
            .bridge$getExpDrop(state, level, pos, tool == null ? net.minecraft.world.item.ItemStack.EMPTY : CraftItemStack.asNMSCopy(tool)) : 0;
        boolean broken = tool == null ? block.breakNaturally() : block.breakNaturally(tool);
        if (broken) {
            if (triggerEffect) {
                if (state.getBlock() instanceof BaseFireBlock) {
                    level.levelEvent(LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
                } else {
                    level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, net.minecraft.world.level.block.Block.getId(state));
                }
            }
            if (exp > 0) {
                state.getBlock().popExperience(level, pos, exp);
            }
        }
        return broken;
    }

    /** Paper's {@code CraftBlock#randomTick()}. */
    public static void randomTick(Block block) {
        CraftBlock craft = (CraftBlock) block;
        ServerLevel level = ((org.bukkit.craftbukkit.v.CraftWorld) block.getWorld()).getHandle();
        BlockPos pos = craft.getPosition();
        BlockState state = level.getBlockState(pos);
        state.randomTick(level, pos, level.random);
    }

    /**
     * Blocks whose class overrides {@code BlockBehaviour#entityInside}: the ones Paper fires EntityInsideBlockEvent for
     * (Paper patch 0538 adds the event to each vanilla block that does something there), mods' blocks included.
     */
    private static final ClassValue<Boolean> HAS_ENTITY_INSIDE = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            for (Class<?> c = type; c != null && c != net.minecraft.world.level.block.state.BlockBehaviour.class; c = c.getSuperclass()) {
                try {
                    c.getDeclaredMethod("entityInside", net.minecraft.world.level.block.state.BlockState.class, net.minecraft.world.level.Level.class,
                        net.minecraft.core.BlockPos.class, net.minecraft.world.entity.Entity.class);
                    return true;
                } catch (NoSuchMethodException ignored) {
                }
            }
            return false;
        }
    };

    /** Paper's EntityInsideBlockEvent: false when a listener cancelled what the block does to the entity. */
    public static boolean entityInside(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level,
                                       net.minecraft.core.BlockPos pos, net.minecraft.world.entity.Entity entity) {
        if (level.isClientSide || io.papermc.paper.event.entity.EntityInsideBlockEvent.getHandlerList().getRegisteredListeners().length == 0
            || !HAS_ENTITY_INSIDE.get(state.getBlock().getClass())) {
            return true;
        }
        io.papermc.paper.event.entity.EntityInsideBlockEvent event = new io.papermc.paper.event.entity.EntityInsideBlockEvent(
            ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) entity).bridge$getBukkitEntity(), org.bukkit.craftbukkit.v.block.CraftBlock.at(level, pos));
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * Players a beacon reaches, as vanilla's {@code BeaconBlockEntity#applyEffects} collects them, with Paper's custom
     * effect range (patch 0429). Replaces CraftBukkit's {@code getHumansInRange}, which NeoForge's beacon lacks.
     */
    public static java.util.List<net.minecraft.world.entity.player.Player> beaconPlayers(net.minecraft.world.level.block.entity.BeaconBlockEntity beacon) {
        net.minecraft.world.level.Level level = beacon.getLevel();
        if (level == null || beacon.levels <= 0) {
            return java.util.List.of();
        }
        double range = ((io.izzel.arclight.common.bridge.core.world.level.block.entity.BeaconBlockEntityBridge) beacon).fusioncore$effectRange();
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(beacon.getBlockPos()).inflate(range).expandTowards(0.0, level.getHeight(), 0.0);
        return level.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, box);
    }

    /** Paper's {@code CraftBlockData#getDestroySpeed} (patch 0432); only mining efficiency modifiers are summed. */
    public static float destroySpeed(net.minecraft.world.level.block.state.BlockState state, org.bukkit.inventory.ItemStack itemStack, boolean considerEnchants) {
        net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(itemStack);
        float speed = stack.getDestroySpeed(state);
        if (speed > 1.0F && considerEnchants) {
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute = net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY;
            double[] base = {attribute.value().getDefaultValue()};
            double[] baseMul = {1};
            double[] totalMul = {1};
            net.minecraft.world.item.enchantment.EnchantmentHelper.forEachModifier(stack, net.minecraft.world.entity.EquipmentSlot.MAINHAND, (holder, modifier) -> {
                if (!holder.is(attribute)) {
                    return;
                }
                switch (modifier.operation()) {
                    case ADD_VALUE -> base[0] += modifier.amount();
                    case ADD_MULTIPLIED_BASE -> baseMul[0] += modifier.amount();
                    case ADD_MULTIPLIED_TOTAL -> totalMul[0] *= 1D + modifier.amount();
                }
            });
            speed += (float) attribute.value().sanitizeValue(base[0] * baseMul[0] * totalMul[0]);
        }
        return speed;
    }

    /** Paper's {@code CraftBlockState#getDrops(ItemStack, Entity)} (patch 0744). */
    public static java.util.Collection<ItemStack> stateDrops(org.bukkit.craftbukkit.v.block.CraftBlockState state, ItemStack tool, org.bukkit.entity.Entity entity) {
        com.google.common.base.Preconditions.checkState(state.isPlaced(), "The blockState must be placed to call this method");
        ServerLevel level = ((org.bukkit.craftbukkit.v.CraftWorld) state.getWorld()).getHandle();
        BlockPos pos = state.getPosition();
        BlockState data = state.getHandle();
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(tool);
        if (tool == null || !data.requiresCorrectToolForDrops() || nms.isCorrectToolForDrops(data)) {
            return net.minecraft.world.level.block.Block.getDrops(data, level, pos, level.getBlockEntity(pos),
                entity == null ? null : ((org.bukkit.craftbukkit.v.entity.CraftEntity) entity).getHandle(), nms)
                .stream().map(CraftItemStack::asBukkitCopy).toList();
        }
        return java.util.Collections.emptyList();
    }
}
