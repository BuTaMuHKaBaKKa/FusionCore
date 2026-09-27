package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fusion Core: Paper's drops in the shear events (patch 0916), wired into NeoForge's {@code IShearable}.
 * Mod entities keep their own drops.
 */
public final class FusionCoreShearing {

    private FusionCoreShearing() {
    }

    // the drops the last event ended with, for the onSheared call right after it
    private static final ThreadLocal<List<ItemStack>> DROPS = new ThreadLocal<>();
    // the dispenser of a dispenser shearing, around ShearsDispenseItemBehavior#tryShearLivingEntity
    private static final ThreadLocal<BlockPos> DISPENSER = new ThreadLocal<>();

    /** Paper's {@code generateDefaultDrops}: null for entities Paper has none for. */
    public static @Nullable List<ItemStack> defaultDrops(Entity entity) {
        if (entity instanceof Sheep sheep) {
            int count = 1 + sheep.getRandom().nextInt(3);
            List<ItemStack> drops = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                drops.add(new ItemStack(Sheep.ITEM_BY_DYE.get(sheep.getColor())));
            }
            return drops;
        }
        if (entity instanceof MushroomCow cow) {
            List<ItemStack> drops = new ArrayList<>(5);
            for (int i = 0; i < 5; i++) {
                drops.add(new ItemStack(cow.getVariant().getBlockState().getBlock()));
            }
            return drops;
        }
        if (entity instanceof SnowGolem) {
            return new ArrayList<>(List.of(new ItemStack(Items.CARVED_PUMPKIN)));
        }
        if (entity instanceof Bogged bogged && bogged.level() instanceof ServerLevel level) {
            LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, bogged.position())
                .withParameter(LootContextParams.THIS_ENTITY, bogged)
                .create(LootContextParamSets.SHEARING);
            return new ArrayList<>(level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.BOGGED_SHEAR).getRandomItems(params));
        }
        return null;
    }

    private static List<org.bukkit.inventory.ItemStack> bukkit(@Nullable List<ItemStack> drops) {
        if (drops == null) {
            return Collections.emptyList();
        }
        List<org.bukkit.inventory.ItemStack> list = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) {
            list.add(CraftItemStack.asBukkitCopy(drop));
        }
        return list;
    }

    private static void keep(@Nullable List<ItemStack> defaults, List<org.bukkit.inventory.ItemStack> drops) {
        if (defaults == null) {
            DROPS.remove();
            return;
        }
        List<ItemStack> list = new ArrayList<>(drops.size());
        for (org.bukkit.inventory.ItemStack drop : drops) {
            ItemStack stack = CraftItemStack.asNMSCopy(drop);
            if (!stack.isEmpty()) {
                list.add(stack);
            }
        }
        DROPS.set(list);
    }

    /** {@code PlayerShearEntityEvent} with drops, once the entity is shearable; false when a listener cancelled it. */
    public static boolean playerShear(Player player, Entity entity, ItemStack tool, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return true;
        }
        List<ItemStack> defaults = defaultDrops(entity);
        PlayerShearEntityEvent event = FusionCoreEvents.newPlayerShearEntityEvent(((ServerPlayerBridge) serverPlayer).bridge$getBukkitEntity(),
            ((EntityBridge) entity).bridge$getBukkitEntity(), CraftItemStack.asBukkitCopy(tool), CraftEquipmentSlot.getHand(hand), bukkit(defaults));
        if (!FusionCoreEvents.call(event)) {
            DROPS.remove();
            return false;
        }
        keep(defaults, ((io.izzel.arclight.common.bridge.bukkit.ShearDropsBridge) event).fusioncore$getDrops());
        return true;
    }

    public static void withDispenser(BlockPos dispenser, Runnable shear) {
        DISPENSER.set(dispenser);
        try {
            shear.run();
        } finally {
            DISPENSER.remove();
        }
    }

    /** {@code BlockShearEntityEvent} with drops for a dispenser, once the entity is shearable; false when cancelled. */
    public static boolean dispenserShear(ServerLevel level, Entity entity, ItemStack tool) {
        BlockPos dispenser = DISPENSER.get();
        if (dispenser == null) {
            return true;
        }
        List<ItemStack> defaults = defaultDrops(entity);
        BlockShearEntityEvent event = FusionCoreEvents.newBlockShearEntityEvent(CraftBlock.at(level, dispenser),
            ((EntityBridge) entity).bridge$getBukkitEntity(), CraftItemStack.asCraftMirror(tool), bukkit(defaults));
        if (!FusionCoreEvents.call(event)) {
            DROPS.remove();
            return false;
        }
        keep(defaults, ((io.izzel.arclight.common.bridge.bukkit.ShearDropsBridge) event).fusioncore$getDrops());
        return true;
    }

    // the entity whose own drops are held back while onSheared runs, and how many were
    private static final ThreadLocal<Entity> HELD_BACK = new ThreadLocal<>();
    private static final ThreadLocal<int[]> HELD_BACK_COUNT = new ThreadLocal<>();

    /** Called where an entity spawns a drop ({@code Entity#spawnAtLocation}, the mooshroom's shear drops): true to drop nothing. */
    public static boolean holdBack(Entity entity) {
        if (HELD_BACK.get() != entity) {
            return false;
        }
        HELD_BACK_COUNT.get()[0]++;
        return true;
    }

    /** Runs {@code onSheared} and returns the event's drops in place of the entity's own. */
    public static List<ItemStack> shear(Entity entity, java.util.function.Supplier<List<ItemStack>> onSheared) {
        List<ItemStack> drops = DROPS.get();
        DROPS.remove();
        if (drops == null) {
            return onSheared.get();
        }
        Entity outer = HELD_BACK.get();
        int[] outerCount = HELD_BACK_COUNT.get();
        int[] count = new int[1];
        HELD_BACK.set(entity);
        HELD_BACK_COUNT.set(count);
        List<ItemStack> own;
        try {
            own = onSheared.get();
        } finally {
            HELD_BACK.set(outer);
            HELD_BACK_COUNT.set(outerCount);
        }
        if (count[0] == 0 && own.isEmpty()) {
            return own;
        }
        return drops;
    }
}
