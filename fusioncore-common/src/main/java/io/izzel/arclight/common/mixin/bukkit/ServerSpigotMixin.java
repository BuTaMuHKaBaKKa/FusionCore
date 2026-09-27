package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fusion Core: Paper's config accessors on {@code Server.Spigot}; {@code getPaperConfig()} is built from Fusion Core's settings.
 */
@Mixin(value = org.bukkit.Server.Spigot.class, remap = false)
public abstract class ServerSpigotMixin {

    @Shadow public abstract YamlConfiguration getConfig();

    public YamlConfiguration getSpigotConfig() {
        return this.getConfig();
    }

    /** Paper: the loaded bukkit.yml. */
    public YamlConfiguration getBukkitConfig() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.bukkitConfig();
    }

    public YamlConfiguration getPaperConfig() {
        return io.izzel.arclight.common.mod.util.FusionCorePaperServer.paperConfig();
    }
}
