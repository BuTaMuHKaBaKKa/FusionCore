package io.izzel.arclight.common.mixin.bukkit;

import net.kyori.adventure.util.TriState;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code permissionValue} on {@link Permissible} (Paper patch 0006 "Adventure"), Paper's bodies. */
@Mixin(value = Permissible.class, remap = false)
public interface PermissibleMixin {

    default TriState permissionValue(final Permission permission) {
        Permissible self = (Permissible) this;
        if (self.isPermissionSet(permission)) {
            return TriState.byBoolean(self.hasPermission(permission));
        } else {
            return TriState.NOT_SET;
        }
    }

    default TriState permissionValue(final String permission) {
        Permissible self = (Permissible) this;
        if (self.isPermissionSet(permission)) {
            return TriState.byBoolean(self.hasPermission(permission));
        } else {
            return TriState.NOT_SET;
        }
    }
}
