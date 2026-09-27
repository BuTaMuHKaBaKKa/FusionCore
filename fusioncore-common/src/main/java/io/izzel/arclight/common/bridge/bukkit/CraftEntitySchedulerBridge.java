package io.izzel.arclight.common.bridge.bukkit;

import io.izzel.arclight.common.mod.server.scheduler.EntityTaskScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;

/** Fusion Core: the Folia entity scheduler of a {@code CraftEntity}, created on first use. */
public interface CraftEntitySchedulerBridge {

    EntityScheduler fusioncore$apiScheduler();

    /** Null while no plugin has used the scheduler. */
    EntityTaskScheduler fusioncore$taskSchedulerIfCreated();
}
