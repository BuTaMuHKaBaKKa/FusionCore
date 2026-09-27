package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Raid#getId} and {@code getBossBar} (Paper patch 0936 "More Raid API"). */
@Mixin(value = org.bukkit.Raid.class, remap = false)
public interface RaidMixin {

    default int getId() {
        return ((org.bukkit.craftbukkit.v.CraftRaid) (Object) this).getHandle().getId();
    }

    default org.bukkit.boss.BossBar getBossBar() {
        return new org.bukkit.craftbukkit.v.boss.CraftBossBar(((io.izzel.arclight.common.mixin.core.world.entity.raid.RaidPaperAccessor)
            ((org.bukkit.craftbukkit.v.CraftRaid) (Object) this).getHandle()).fusioncore$raidEvent());
    }
}
