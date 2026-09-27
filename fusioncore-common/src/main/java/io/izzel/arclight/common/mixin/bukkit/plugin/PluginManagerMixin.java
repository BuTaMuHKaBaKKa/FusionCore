package io.izzel.arclight.common.mixin.bukkit.plugin;

import io.papermc.paper.plugin.PermissionManager;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/**
 * Fusion Core: Paper's {@link PermissionManager} as a supertype of {@link PluginManager} (Paper API patch 0009). Spigot's
 * interface already has the other methods; {@code SimplePluginManager} overrides these two with direct access.
 */
@Mixin(value = PluginManager.class, remap = false)
public interface PluginManagerMixin extends PermissionManager {

    @Override
    default void addPermissions(List<Permission> perm) {
        for (Permission permission : perm) {
            ((PluginManager) this).addPermission(permission);
        }
    }

    @Override
    default void clearPermissions() {
        for (Permission permission : java.util.List.copyOf(((PluginManager) this).getPermissions())) {
            ((PluginManager) this).removePermission(permission);
        }
    }

    /** Fusion Core: Paper's {@code isTransitiveDependency} (API patch 0009); {@code SimplePluginManager} has the same body. */
    default boolean isTransitiveDependency(io.papermc.paper.plugin.configuration.PluginMeta plugin, io.papermc.paper.plugin.configuration.PluginMeta depend) {
        return io.izzel.arclight.common.mod.util.FusionCorePluginClassLoaders.Dependencies.INSTANCE.isTransitiveDependency(plugin, depend);
    }

    /** Fusion Core: Paper's {@code overridePermissionManager}; implemented by {@code SimplePluginManager}. */
    default void overridePermissionManager(org.bukkit.plugin.Plugin plugin, PermissionManager permissionManager) {
        throw new UnsupportedOperationException("This plugin manager cannot override its permission manager");
    }
}
