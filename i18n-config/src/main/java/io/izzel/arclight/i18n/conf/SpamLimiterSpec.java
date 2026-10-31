package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

// Fusion Core: Paper's spam-limiter (paper-global.yml) for command suggestion requests, Paper's defaults
@ConfigSerializable
public class SpamLimiterSpec {

    @Setting("tab-spam-increment")
    private int tabSpamIncrement = 1;

    @Setting("tab-spam-limit")
    private int tabSpamLimit = 500;

    @Setting("tab-complete-max-pending")
    private int tabCompleteMaxPending = 16;

    public int getTabSpamIncrement() {
        return tabSpamIncrement;
    }

    public int getTabSpamLimit() {
        return tabSpamLimit;
    }

    public int getTabCompleteMaxPending() {
        return tabCompleteMaxPending;
    }
}
