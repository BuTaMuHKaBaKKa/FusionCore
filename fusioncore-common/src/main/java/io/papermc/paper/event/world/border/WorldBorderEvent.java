// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.event.world.border;

import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.ApiStatus;

public abstract class WorldBorderEvent extends WorldEvent {

    protected final WorldBorder worldBorder;

    @ApiStatus.Internal
    protected WorldBorderEvent(final World world, final WorldBorder worldBorder) {
        super(world);
        this.worldBorder = worldBorder;
    }

    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }
}
