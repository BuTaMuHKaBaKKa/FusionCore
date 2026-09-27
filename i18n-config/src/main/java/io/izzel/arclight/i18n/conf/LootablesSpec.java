package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Fusion Core: Paper's {@code lootables} world settings (patch 0095 "LootTable API and replenishable lootables"), one set
 * for all worlds. Durations use Paper's format: a number with {@code s}, {@code m}, {@code h} or {@code d}.
 */
@ConfigSerializable
public class LootablesSpec {

    @Setting("auto-replenish")
    private boolean autoReplenish = false;

    @Setting("restrict-player-reloot")
    private boolean restrictPlayerReloot = true;

    @Setting("restrict-player-reloot-time")
    private String restrictPlayerRelootTime = "disabled";

    @Setting("reset-seed-on-fill")
    private boolean resetSeedOnFill = true;

    @Setting("max-refills")
    private int maxRefills = -1;

    @Setting("refresh-min")
    private String refreshMin = "12h";

    @Setting("refresh-max")
    private String refreshMax = "2d";

    @Setting("retain-unlooted-shulker-box-loot-table-on-non-player-break")
    private boolean retainUnlootedShulkerBoxLootTableOnNonPlayerBreak = true;

    public boolean isAutoReplenish() {
        return autoReplenish;
    }

    public boolean isRestrictPlayerReloot() {
        return restrictPlayerReloot;
    }

    public String getRestrictPlayerRelootTime() {
        return restrictPlayerRelootTime;
    }

    public boolean isResetSeedOnFill() {
        return resetSeedOnFill;
    }

    public int getMaxRefills() {
        return maxRefills;
    }

    public String getRefreshMin() {
        return refreshMin;
    }

    public String getRefreshMax() {
        return refreshMax;
    }

    public boolean isRetainUnlootedShulkerBoxLootTableOnNonPlayerBreak() {
        return retainUnlootedShulkerBoxLootTableOnNonPlayerBreak;
    }
}
