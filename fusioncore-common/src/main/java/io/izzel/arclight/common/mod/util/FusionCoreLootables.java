package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.loottable.LootableInventory;
import com.destroystokyo.paper.loottable.LootableInventoryReplenishEvent;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.LootablesSpec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Fusion Core: Paper's replenishable lootables (patch 0095), saved under {@code Paper.LootableData}. Settings are the
 * {@code lootables} section of {@code fusioncore.conf}.
 */
public final class FusionCoreLootables {

    private static final Random RANDOM = new Random();
    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern NOT_NUMERIC = Pattern.compile("[^-\\d.]");

    private FusionCoreLootables() {
    }

    public static LootablesSpec config() {
        return ArclightConfig.spec().getLootables();
    }

    public static boolean autoReplenish() {
        return config().isAutoReplenish();
    }

    /** Paper's {@code Duration#seconds}: a number with an optional unit s, m, h or d. */
    static long seconds(String value) {
        String str = SPACE.matcher(value).replaceAll("");
        if (str.isEmpty()) {
            return 0;
        }
        char unit = str.charAt(str.length() - 1);
        str = NOT_NUMERIC.matcher(str).replaceAll("");
        double num;
        try {
            num = Double.parseDouble(str);
        } catch (Exception e) {
            num = 0D;
        }
        switch (unit) {
            case 'd' -> num *= 60 * 60 * 24;
            case 'h' -> num *= 60 * 60;
            case 'm' -> num *= 60;
            default -> {
            }
        }
        return (int) num;
    }

    /** How the data reaches the holder's seed, loot table and API view (Paper's {@code LootTableInterface}). */
    public interface Holder<T> {

        void setSeed(T holder, long seed);

        boolean hasLootTable(T holder);

        LootableInventory getInventoryForEvent(T holder);
    }

    public static final Holder<RandomizableContainer> CONTAINER = new Holder<>() {
        @Override
        public void setSeed(RandomizableContainer holder, long seed) {
            holder.setLootTableSeed(seed);
        }

        @Override
        public boolean hasLootTable(RandomizableContainer holder) {
            return holder.getLootTable() != null;
        }

        @Override
        public LootableInventory getInventoryForEvent(RandomizableContainer holder) {
            // Paper: block.getState(false); Spigot has no live block states, so this is a snapshot
            org.bukkit.block.Block block = CraftBlock.at(Objects.requireNonNull(holder.getLevel(), "Cannot manage loot tables on block entities not in world"), holder.getBlockPos());
            return (LootableInventory) (Object) block.getState();
        }
    };

    public static final Holder<ContainerEntity> ENTITY = new Holder<>() {
        @Override
        public void setSeed(ContainerEntity holder, long seed) {
            holder.setLootTableSeed(seed);
        }

        @Override
        public boolean hasLootTable(ContainerEntity holder) {
            return holder.getLootTable() != null;
        }

        @Override
        public LootableInventory getInventoryForEvent(ContainerEntity holder) {
            return (LootableInventory) (Object) ((EntityBridge) holder).bridge$getBukkitEntity();
        }
    };

    /** Paper's {@code PaperLootableInventoryData}. */
    public static final class Data {

        private static final String ROOT = "Paper.LootableData";
        private static final String LAST_FILL = "lastFill";
        private static final String NEXT_REFILL = "nextRefill";
        private static final String NUM_REFILLS = "numRefills";
        private static final String LOOTED_PLAYERS = "lootedPlayers";

        private long lastFill = -1;
        private long nextRefill = -1;
        private int numRefills = 0;
        private @Nullable Map<UUID, Long> lootedPlayers;

        public long getLastFill() {
            return this.lastFill;
        }

        public long getNextRefill() {
            return this.nextRefill;
        }

        public long setNextRefill(long nextRefill) {
            long prev = this.nextRefill;
            this.nextRefill = nextRefill;
            return prev;
        }

        public <T> boolean shouldReplenish(T holder, Holder<T> holderInterface, @Nullable Player player) {
            // No Loot Table associated
            if (!holderInterface.hasLootTable(holder)) {
                return false;
            }
            LootablesSpec config = config();
            // ALWAYS process the first fill or if the feature is disabled
            if (this.lastFill == -1 || !config.isAutoReplenish()) {
                return true;
            }
            // Only process refills when a player is set
            if (player == null) {
                return false;
            }
            // Chest is not scheduled for refill
            if (this.nextRefill == -1) {
                return false;
            }
            // Check if max refills has been hit
            if (config.getMaxRefills() != -1 && this.numRefills >= config.getMaxRefills()) {
                return false;
            }
            // Refill has not been reached
            if (this.nextRefill > System.currentTimeMillis()) {
                return false;
            }
            org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((EntityBridge) player).bridge$getBukkitEntity();
            LootableInventoryReplenishEvent event = new LootableInventoryReplenishEvent(bukkitPlayer, holderInterface.getInventoryForEvent(holder));
            event.setCancelled(!this.canPlayerLoot(player.getUUID()));
            Bukkit.getPluginManager().callEvent(event);
            return !event.isCancelled();
        }

        public <T> boolean shouldClearLootTable(T holder, Holder<T> holderInterface, @Nullable Player player) {
            this.lastFill = System.currentTimeMillis();
            LootablesSpec config = config();
            if (config.isAutoReplenish()) {
                long min = seconds(config.getRefreshMin());
                long max = seconds(config.getRefreshMax());
                this.nextRefill = this.lastFill + (min + RANDOM.nextLong(Math.max(1, max - min + 1))) * 1000L;
                this.numRefills++;
                if (config.isResetSeedOnFill()) {
                    holderInterface.setSeed(holder, 0);
                }
                if (player != null) { // numRefills can grow without a player in lootedPlayers (minecarts), as in Paper
                    this.setPlayerLootedState(player.getUUID(), true);
                }
                return false;
            }
            return true;
        }

        public void loadNbt(CompoundTag base) {
            if (!base.contains(ROOT, Tag.TAG_COMPOUND)) {
                return;
            }
            CompoundTag comp = base.getCompound(ROOT);
            if (comp.contains(LAST_FILL)) {
                this.lastFill = comp.getLong(LAST_FILL);
            }
            if (comp.contains(NEXT_REFILL)) {
                this.nextRefill = comp.getLong(NEXT_REFILL);
            }
            if (comp.contains(NUM_REFILLS)) {
                this.numRefills = comp.getInt(NUM_REFILLS);
            }
            if (comp.contains(LOOTED_PLAYERS, Tag.TAG_LIST)) {
                ListTag list = comp.getList(LOOTED_PLAYERS, Tag.TAG_COMPOUND);
                int size = list.size();
                if (size > 0) {
                    this.lootedPlayers = new HashMap<>(size);
                }
                for (int i = 0; i < size; i++) {
                    CompoundTag cmp = list.getCompound(i);
                    this.lootedPlayers.put(cmp.getUUID("UUID"), cmp.getLong("Time"));
                }
            }
        }

        public void saveNbt(CompoundTag base) {
            CompoundTag comp = new CompoundTag();
            if (this.nextRefill != -1) {
                comp.putLong(NEXT_REFILL, this.nextRefill);
            }
            if (this.lastFill != -1) {
                comp.putLong(LAST_FILL, this.lastFill);
            }
            if (this.numRefills != 0) {
                comp.putInt(NUM_REFILLS, this.numRefills);
            }
            if (this.lootedPlayers != null && !this.lootedPlayers.isEmpty()) {
                ListTag list = new ListTag();
                for (Map.Entry<UUID, Long> entry : this.lootedPlayers.entrySet()) {
                    CompoundTag cmp = new CompoundTag();
                    cmp.putUUID("UUID", entry.getKey());
                    cmp.putLong("Time", entry.getValue());
                    list.add(cmp);
                }
                comp.put(LOOTED_PLAYERS, list);
            }
            if (!comp.isEmpty()) {
                base.put(ROOT, comp);
            }
        }

        public void setPlayerLootedState(UUID player, boolean looted) {
            if (looted && this.lootedPlayers == null) {
                this.lootedPlayers = new HashMap<>();
            }
            if (looted) {
                this.lootedPlayers.put(player, System.currentTimeMillis());
            } else if (this.lootedPlayers != null) {
                this.lootedPlayers.remove(player);
            }
        }

        public boolean canPlayerLoot(UUID player) {
            Long lastLooted = this.getLastLooted(player);
            LootablesSpec config = config();
            if (!config.isRestrictPlayerReloot() || lastLooted == null) {
                return true;
            }
            String time = config.getRestrictPlayerRelootTime();
            if (time == null || time.isBlank() || time.trim().equalsIgnoreCase("disabled")) {
                return false;
            }
            return TimeUnit.SECONDS.toMillis(seconds(time)) + lastLooted < System.currentTimeMillis();
        }

        public boolean hasPlayerLooted(UUID player) {
            return this.lootedPlayers != null && this.lootedPlayers.containsKey(player);
        }

        public @Nullable Long getLastLooted(UUID player) {
            return this.lootedPlayers != null ? this.lootedPlayers.get(player) : null;
        }
    }
}
