package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@link Lootable} additions (patch 0039): table and seed at once, {@code hasLootTable}, {@code clearLootTable}. */
@Mixin(value = Lootable.class, remap = false)
public interface LootableMixin {

    default void setLootTable(@Nullable LootTable table, long seed) {
        ((Lootable) this).setLootTable(table);
        ((Lootable) this).setSeed(seed);
    }

    default boolean hasLootTable() {
        return ((Lootable) this).getLootTable() != null;
    }

    default void clearLootTable() {
        ((Lootable) this).setLootTable(null);
    }
}
