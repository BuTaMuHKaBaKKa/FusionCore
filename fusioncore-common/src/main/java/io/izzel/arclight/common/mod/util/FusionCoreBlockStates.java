package io.izzel.arclight.common.mod.util;

import com.google.common.base.Preconditions;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.level.block.entity.BlockEntityPaperStateBridge;
import io.izzel.arclight.common.bridge.core.world.level.block.entity.LidStateBridge;
import io.izzel.arclight.common.mixin.bukkit.CraftBlockEntityStateAccessor;
import io.izzel.arclight.common.mixin.core.world.entity.api.EndDragonFightApiInvoker;
import io.izzel.arclight.common.mixin.core.world.level.block.entity.api.AbstractFurnaceApiInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.Vec3;
import org.bukkit.NamespacedKey;
import org.bukkit.block.sign.Side;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.entity.EnderCrystal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Fusion Core: bodies of Paper's block state API. */
public final class FusionCoreBlockStates {

    private FusionCoreBlockStates() {
    }

    private static BlockEntity snapshot(Object state) {
        return ((CraftBlockEntityStateAccessor) state).fusioncore$getSnapshot();
    }

    private static BlockEntity live(Object state) {
        return ((CraftBlockEntityStateAccessor) state).arclight$getBlockEntity();
    }

    private static Level levelOf(CraftBlockState state) {
        return state.isPlaced() ? ((org.bukkit.craftbukkit.v.CraftWorld) state.getWorld()).getHandle() : null;
    }

    private static void requirePlaced(CraftBlockState state) {
        Preconditions.checkState(state.isPlaced(), "The blockState must be placed to call this method");
    }

    private static void ensureNoWorldGeneration(CraftBlockState state) {
        LevelAccessor access = state.getWorldHandle();
        Preconditions.checkState(access == null || access instanceof Level, "This operation is not supported during world generation!");
    }

    // Campfire, patch 0634 (snapshot)

    private static boolean[] stopCooking(Object campfire, int index) {
        Preconditions.checkArgument(-1 < index && index < 4, "Slot index must be between 0 (incl) to 3 (incl)");
        return ((BlockEntityPaperStateBridge) snapshot(campfire)).fusioncore$stopCooking();
    }

    public static boolean isCookingDisabled(Object campfire, int index) {
        return stopCooking(campfire, index)[index];
    }

    public static boolean setCookingDisabled(Object campfire, int index, boolean disabled) {
        boolean[] flags = stopCooking(campfire, index);
        boolean previous = flags[index];
        flags[index] = disabled;
        return previous;
    }

    // Furnace, patches 0247 and 0664 (snapshot)

    public static double cookSpeedMultiplier(Object furnace) {
        return ((BlockEntityPaperStateBridge) snapshot(furnace)).fusioncore$cookSpeedMultiplier();
    }

    public static void setCookSpeedMultiplier(Object furnace, double multiplier) {
        Preconditions.checkArgument(multiplier >= 0, "Furnace speed multiplier cannot be negative");
        Preconditions.checkArgument(multiplier <= 200, "Furnace speed multiplier cannot more than 200");
        AbstractFurnaceBlockEntity snapshot = (AbstractFurnaceBlockEntity) snapshot(furnace);
        ((BlockEntityPaperStateBridge) snapshot).fusioncore$setCookSpeedMultiplier(multiplier);
        // Paper looks the recipe up without a level when the state is not placed; vanilla's lookup needs one, any does.
        Level level = levelOf((CraftBlockState) furnace);
        snapshot.cookingTotalTime = AbstractFurnaceApiInvoker.fusioncore$totalCookTime(level != null ? level : io.izzel.arclight.common.mod.server.ArclightServer.getMinecraftServer().overworld(), snapshot);
    }

    public static int recipeUsedCount(Object furnace, NamespacedKey recipe) {
        return ((AbstractFurnaceBlockEntity) snapshot(furnace)).recipesUsed.getInt(CraftNamespacedKey.toMinecraft(recipe));
    }

    public static boolean hasRecipeUsedCount(Object furnace, NamespacedKey recipe) {
        return ((AbstractFurnaceBlockEntity) snapshot(furnace)).recipesUsed.containsKey(CraftNamespacedKey.toMinecraft(recipe));
    }

    public static void setRecipeUsedCount(Object furnace, org.bukkit.inventory.CookingRecipe<?> recipe, int count) {
        ResourceLocation location = CraftNamespacedKey.toMinecraft(recipe.getKey());
        Level level = levelOf((CraftBlockState) furnace);
        java.util.Optional<RecipeHolder<?>> nms = (level != null ? level.getRecipeManager() : io.izzel.arclight.common.mod.server.ArclightServer.getMinecraftServer().getRecipeManager()).byKey(location);
        Preconditions.checkArgument(nms.isPresent() && nms.get().value() instanceof AbstractCookingRecipe, recipe.getKey() + " is not recognized as a valid and registered furnace recipe");
        AbstractFurnaceBlockEntity snapshot = (AbstractFurnaceBlockEntity) snapshot(furnace);
        if (count > 0) {
            snapshot.recipesUsed.put(location, count);
        } else {
            snapshot.recipesUsed.removeInt(location);
        }
    }

    public static void setRecipesUsed(Object furnace, Map<org.bukkit.inventory.CookingRecipe<?>, Integer> recipesUsed) {
        ((AbstractFurnaceBlockEntity) snapshot(furnace)).recipesUsed.clear();
        recipesUsed.forEach((recipe, count) -> {
            if (count != null) {
                setRecipeUsedCount(furnace, recipe, count);
            }
        });
    }

    // Hopper, patch 0932 (snapshot)

    public static int transferCooldown(Object hopper) {
        return ((HopperBlockEntity) snapshot(hopper)).cooldownTime;
    }

    public static void setTransferCooldown(Object hopper, int cooldown) {
        Preconditions.checkArgument(cooldown >= 0, "Hooper transfer cooldown cannot be negative (" + cooldown + ")");
        ((HopperBlockEntity) snapshot(hopper)).setCooldown(cooldown);
    }

    // Sign, patch 0833 (editor on the live sign, facing on the snapshot)

    public static UUID allowedEditor(Object sign) {
        ensureNoWorldGeneration((CraftBlockState) sign);
        return ((SignBlockEntity) live(sign)).getPlayerWhoMayEdit();
    }

    public static void setAllowedEditor(Object sign, UUID uuid) {
        ensureNoWorldGeneration((CraftBlockState) sign);
        ((SignBlockEntity) live(sign)).setAllowedPlayerEditor(uuid);
    }

    /** Vanilla's {@code SignBlockEntity#isFacingFrontText(Player)} for a point, as Paper's overload. */
    public static Side interactableSide(Object sign, double x, double z) {
        requirePlaced((CraftBlockState) sign);
        BlockEntity snapshot = snapshot(sign);
        BlockState state = snapshot.getBlockState();
        boolean front = false;
        if (state.getBlock() instanceof SignBlock signBlock) {
            Vec3 center = signBlock.getSignHitboxCenterPosition(state);
            double dx = x - (snapshot.getBlockPos().getX() + center.x);
            double dz = z - (snapshot.getBlockPos().getZ() + center.z);
            float rotation = signBlock.getYRotationDegrees(state);
            float facing = (float) (Mth.atan2(dz, dx) * 180.0F / (float) Math.PI) - 90.0F;
            front = Mth.degreesDifferenceAbs(rotation, facing) <= 90.0F;
        }
        return front ? Side.FRONT : Side.BACK;
    }

    // Chest and ender chest, patch 0963

    public static boolean chestBlocked(CraftBlockState chest) {
        if (!chest.isPlaced()) {
            return false;
        }
        LevelAccessor world = chest.getWorldHandle();
        BlockState data = chest.getHandle();
        if (ChestBlock.isChestBlockedAt(world, chest.getPosition())) {
            return true;
        }
        if (ChestBlock.getBlockType(data) == DoubleBlockCombiner.BlockType.SINGLE) {
            return false;
        }
        Direction direction = ChestBlock.getConnectedDirection(data);
        BlockPos neighbour = chest.getPosition().relative(direction);
        BlockState neighbourState = world.hasChunkAt(neighbour) ? world.getBlockState(neighbour) : null;
        return neighbourState != null
            && neighbourState.is(data.getBlock())
            && ChestBlock.getBlockType(neighbourState) != DoubleBlockCombiner.BlockType.SINGLE
            && ChestBlock.getConnectedDirection(neighbourState) == direction.getOpposite()
            && ChestBlock.isChestBlockedAt(world, neighbour);
    }

    public static boolean enderChestBlocked(CraftBlockState enderChest) {
        BlockPos above = enderChest.getPosition().above();
        return enderChest.isPlaced() && enderChest.getWorldHandle().getBlockState(above).isRedstoneConductor(enderChest.getWorldHandle(), above);
    }

    // Lidded, patch 0541 (live): opened by the API, not by players

    public static boolean lidOpen(Object lidded) {
        BlockEntity live = live(lidded);
        if (live instanceof ChestBlockEntity chest) {
            return ((LidStateBridge) chest.openersCounter).fusioncore$lidOpened();
        } else if (live instanceof BarrelBlockEntity barrel) {
            return ((LidStateBridge) barrel.openersCounter).fusioncore$lidOpened();
        } else if (live instanceof EnderChestBlockEntity enderChest) {
            return ((LidStateBridge) enderChest.openersCounter).fusioncore$lidOpened();
        } else if (live instanceof LidStateBridge shulker) {
            return shulker.fusioncore$lidOpened();
        }
        return false;
    }

    // Beehive, patch 0524 (snapshot)

    public static void clearEntities(Object storage) {
        if (snapshot(storage) instanceof BeehiveBlockEntity hive) {
            hive.stored.clear();
        }
    }

    // Sculk sensor, patch 0665 (snapshot)

    public static int listenerRange(Object sensor) {
        return ((VibrationSystem) snapshot(sensor)).getVibrationUser().getListenerRadius();
    }

    public static void setListenerRange(Object sensor, int range) {
        Preconditions.checkArgument(range > 0, "Vibration listener range must be greater than 0");
        ((BlockEntityPaperStateBridge) snapshot(sensor)).fusioncore$setRangeOverride(range);
    }

    // Sculk catalyst, patch 0855 (live)

    public static void bloom(Object catalyst, io.papermc.paper.math.Position position, int charge) {
        Preconditions.checkNotNull(position);
        CraftBlockState state = (CraftBlockState) catalyst;
        requirePlaced(state);
        SculkCatalystBlockEntity live = (SculkCatalystBlockEntity) live(catalyst);
        net.minecraft.server.level.ServerLevel level = ((org.bukkit.craftbukkit.v.CraftWorld) state.getWorld()).getHandle();
        live.getListener().bloom(level, live.getBlockPos(), live.getBlockState(), level.getRandom());
        live.getListener().getSculkSpreader().addCursors(new BlockPos(position.blockX(), position.blockY(), position.blockZ()), charge);
    }

    // Block data, patch 0666: bounds of the block's own property, found by name (mods reuse the vanilla names)

    public static int minimum(Object blockData, String property) {
        return bound(blockData, property, true);
    }

    public static int maximum(Object blockData, String property) {
        return bound(blockData, property, false);
    }

    private static int bound(Object blockData, String name, boolean min) {
        BlockState state = ((CraftBlockData) blockData).getState();
        for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
            if (property.getName().equals(name) && property instanceof net.minecraft.world.level.block.state.properties.IntegerProperty ints) {
                return min ? Collections.min(ints.getPossibleValues()) : Collections.max(ints.getPossibleValues());
            }
        }
        throw new IllegalStateException(state.getBlock() + " has no property " + name);
    }

    public static boolean flag(Object blockData, String name) {
        BlockState state = ((CraftBlockData) blockData).getState();
        for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
            if (property.getName().equals(name) && property instanceof net.minecraft.world.level.block.state.properties.BooleanProperty flag) {
                return state.getValue(flag);
            }
        }
        throw new IllegalStateException(state.getBlock() + " has no property " + name);
    }

    public static void setFlag(Object blockData, String name, boolean value) {
        BlockState state = ((CraftBlockData) blockData).getState();
        for (net.minecraft.world.level.block.state.properties.Property<?> property : state.getProperties()) {
            if (property.getName().equals(name) && property instanceof net.minecraft.world.level.block.state.properties.BooleanProperty flag) {
                ((io.izzel.arclight.common.mixin.bukkit.CraftBlockDataAccessor) blockData).fusioncore$setState(state.setValue(flag, value));
                return;
            }
        }
        throw new IllegalStateException(state.getBlock() + " has no property " + name);
    }

    // Dragon battle, patch 0867

    public static int gatewayCount(EndDragonFight fight) {
        return 20 - fight.gateways.size();
    }

    public static boolean spawnNewGateway(EndDragonFight fight) {
        if (!fight.gateways.isEmpty()) {
            ((EndDragonFightApiInvoker) fight).fusioncore$spawnNewGateway();
            return true;
        }
        return false;
    }

    public static void spawnNewGateway(EndDragonFight fight, io.papermc.paper.math.Position position) {
        ((io.izzel.arclight.common.mixin.core.world.entity.api.EndDragonFightGatewayAtApiInvoker) fight).fusioncore$spawnNewGateway(new BlockPos(position.blockX(), position.blockY(), position.blockZ()));
    }

    public static List<EnderCrystal> respawnCrystals(EndDragonFight fight) {
        if (fight.respawnCrystals == null) {
            return Collections.emptyList();
        }
        return liveCrystals(fight.respawnCrystals);
    }

    public static List<EnderCrystal> healingCrystals(EndDragonFight fight) {
        List<EndCrystal> crystals = new ArrayList<>();
        for (SpikeFeature.EndSpike spike : SpikeFeature.getSpikesForLevel(fight.level)) {
            crystals.addAll(fight.level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()));
        }
        return liveCrystals(crystals);
    }

    private static List<EnderCrystal> liveCrystals(List<EndCrystal> crystals) {
        List<EnderCrystal> bukkit = new ArrayList<>();
        for (EndCrystal crystal : crystals) {
            if (!crystal.isRemoved() && crystal.isAlive() && ((EntityBridge) crystal).bridge$isValid()) {
                bukkit.add((EnderCrystal) ((EntityBridge) crystal).bridge$getBukkitEntity());
            }
        }
        return Collections.unmodifiableList(bukkit);
    }

    // Spawner, patch 0286: block spawner (snapshot) or spawner minecart

    public static void setSpawnedItem(Object spawner, org.bukkit.inventory.ItemStack itemStack) {
        Preconditions.checkArgument(itemStack != null && !itemStack.getType().isAir(), "spawners cannot spawn air");
        net.minecraft.world.level.BaseSpawner base;
        if (spawner instanceof org.bukkit.craftbukkit.v.entity.CraftEntity entity) {
            base = ((net.minecraft.world.entity.vehicle.MinecartSpawner) entity.getHandle()).getSpawner();
        } else {
            base = ((net.minecraft.world.level.block.entity.SpawnerBlockEntity) snapshot(spawner)).getSpawner();
        }
        net.minecraft.nbt.CompoundTag entity = new net.minecraft.nbt.CompoundTag();
        entity.putString(net.minecraft.world.entity.Entity.ID_TAG, net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(net.minecraft.world.entity.EntityType.ITEM).toString());
        entity.put("Item", org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(itemStack).save(io.izzel.arclight.common.mod.server.ArclightServer.getMinecraftServer().registryAccess()));
        java.util.Optional<net.minecraft.world.entity.EquipmentTable> equipment = java.util.Optional.ofNullable(base.nextSpawnData).flatMap(net.minecraft.world.level.SpawnData::equipment);
        base.nextSpawnData = new net.minecraft.world.level.SpawnData(entity, java.util.Optional.empty(), equipment);
    }

    // Fusion Core: Paper's Spawner#isActivated / resetTimer (patch 0286), placed block spawner or spawner minecart

    private record SpawnerAt(net.minecraft.world.level.BaseSpawner spawner, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
    }

    private static SpawnerAt spawnerAt(Object spawner) {
        if (spawner instanceof org.bukkit.craftbukkit.v.entity.CraftEntity entity) {
            net.minecraft.world.entity.vehicle.MinecartSpawner cart = (net.minecraft.world.entity.vehicle.MinecartSpawner) entity.getHandle();
            return new SpawnerAt(cart.getSpawner(), cart.level(), cart.blockPosition());
        }
        org.bukkit.block.BlockState state = (org.bukkit.block.BlockState) spawner;
        Preconditions.checkState(state.isPlaced(), "The blockState must be placed to call this method");
        net.minecraft.world.level.Level level = ((org.bukkit.craftbukkit.v.CraftWorld) state.getWorld()).getHandle();
        return new SpawnerAt(((net.minecraft.world.level.block.entity.SpawnerBlockEntity) snapshot(spawner)).getSpawner(), level,
            new net.minecraft.core.BlockPos(state.getX(), state.getY(), state.getZ()));
    }

    /** A player is within the spawner's required range. */
    public static boolean spawnerActivated(Object spawner) {
        SpawnerAt at = spawnerAt(spawner);
        return ((io.izzel.arclight.common.mixin.core.world.entity.api.BaseSpawnerApiInvoker) at.spawner()).fusioncore$isNearPlayer(at.level(), at.pos());
    }

    /** Picks a new random delay and next spawn, as after a spawn (a block spawner's snapshot needs update()). */
    public static void resetSpawnerTimer(Object spawner) {
        SpawnerAt at = spawnerAt(spawner);
        ((io.izzel.arclight.common.mixin.core.world.entity.api.BaseSpawnerApiInvoker) at.spawner()).fusioncore$delay(at.level(), at.pos());
    }
}
