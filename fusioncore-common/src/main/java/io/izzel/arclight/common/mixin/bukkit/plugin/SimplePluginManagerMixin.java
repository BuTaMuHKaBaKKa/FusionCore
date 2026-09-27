package io.izzel.arclight.common.mixin.bukkit.plugin;

import io.izzel.arclight.common.mod.server.scheduler.PaperSchedulers;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Level;

/**
 * Fusion Core: a disabled plugin loses its Folia global and async tasks together with its Bukkit
 * tasks, as in Paper's {@code PaperPluginInstanceManager#disablePlugin} (patch 0843).
 */
@Mixin(value = SimplePluginManager.class, remap = false)
public abstract class SimplePluginManagerMixin {

    @Shadow @Final private Server server;
    @Shadow @Final private java.util.Map<String, org.bukkit.permissions.Permission> permissions;
    @Shadow @Final private java.util.Map<Boolean, java.util.Set<org.bukkit.permissions.Permission>> defaultPerms;
    @Shadow public abstract void addPermission(org.bukkit.permissions.Permission perm, boolean dirty);
    @Shadow public abstract void dirtyPermissibles();

    @Shadow private java.io.File updateDirectory;
    @Shadow public abstract Plugin[] loadPlugins(java.io.File[] files);

    // Fusion Core: Paper's PluginManager#overridePermissionManager (API patch 0009): while one is set, every PermissionManager
    // method of the plugin manager goes to it; null goes back to the plugin manager's own permissions
    private io.papermc.paper.plugin.PermissionManager fusioncore$permissions;

    public void overridePermissionManager(Plugin plugin, io.papermc.paper.plugin.PermissionManager permissionManager) {
        this.fusioncore$permissions = permissionManager == this.server.getPluginManager() ? null : permissionManager;
    }

    /** Fusion Core: Paper's {@code isTransitiveDependency}: the runtime dependency tree. */
    public boolean isTransitiveDependency(io.papermc.paper.plugin.configuration.PluginMeta plugin, io.papermc.paper.plugin.configuration.PluginMeta depend) {
        return io.izzel.arclight.common.mod.util.FusionCorePluginClassLoaders.Dependencies.INSTANCE.isTransitiveDependency(plugin, depend);
    }

    /** Fusion Core: Paper's {@code loadPlugins(File, List)}: the directory's plugins and the extra jars, sorted by dependencies together. */
    public Plugin[] loadPlugins(java.io.File directory, java.util.List<java.io.File> extraPluginJars) {
        com.google.common.base.Preconditions.checkArgument(directory != null, "Directory cannot be null");
        com.google.common.base.Preconditions.checkArgument(directory.isDirectory(), "Directory must be a directory");
        if (!this.server.getUpdateFolder().isEmpty()) {
            this.updateDirectory = new java.io.File(directory, this.server.getUpdateFolder());
        }
        java.util.List<java.io.File> files = new java.util.ArrayList<>(java.util.Arrays.asList(directory.listFiles()));
        files.addAll(extraPluginJars);
        return this.loadPlugins(files.toArray(new java.io.File[0]));
    }

    @Inject(method = "getPermission", cancellable = true, at = @At("HEAD"))
    private void fusioncore$getPermission(String name, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<org.bukkit.permissions.Permission> cir) {
        if (this.fusioncore$permissions != null) cir.setReturnValue(this.fusioncore$permissions.getPermission(name));
    }

    @Inject(method = {"addPermission(Lorg/bukkit/permissions/Permission;)V"}, cancellable = true, at = @At("HEAD"))
    private void fusioncore$addPermission(org.bukkit.permissions.Permission perm, CallbackInfo ci) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.addPermission(perm);
            ci.cancel();
        }
    }

    @Inject(method = "addPermission(Lorg/bukkit/permissions/Permission;Z)V", cancellable = true, at = @At("HEAD"))
    private void fusioncore$addPermissionDirty(org.bukkit.permissions.Permission perm, boolean dirty, CallbackInfo ci) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.addPermission(perm);
            ci.cancel();
        }
    }

    @Inject(method = "getDefaultPermissions", cancellable = true, at = @At("HEAD"))
    private void fusioncore$getDefaultPermissions(boolean op, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<java.util.Set<org.bukkit.permissions.Permission>> cir) {
        if (this.fusioncore$permissions != null) cir.setReturnValue(this.fusioncore$permissions.getDefaultPermissions(op));
    }

    @Inject(method = "removePermission(Lorg/bukkit/permissions/Permission;)V", cancellable = true, at = @At("HEAD"))
    private void fusioncore$removePermission(org.bukkit.permissions.Permission perm, CallbackInfo ci) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.removePermission(perm);
            ci.cancel();
        }
    }

    @Inject(method = "removePermission(Ljava/lang/String;)V", cancellable = true, at = @At("HEAD"))
    private void fusioncore$removePermissionByName(String name, CallbackInfo ci) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.removePermission(name);
            ci.cancel();
        }
    }

    @Inject(method = "recalculatePermissionDefaults", cancellable = true, at = @At("HEAD"))
    private void fusioncore$recalculate(org.bukkit.permissions.Permission perm, CallbackInfo ci) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.recalculatePermissionDefaults(perm);
            ci.cancel();
        }
    }

    @Inject(method = "subscribeToPermission", cancellable = true, at = @At("HEAD"))
    private void fusioncore$subscribe(String permission, org.bukkit.permissions.Permissible permissible, CallbackInfo ci) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.subscribeToPermission(permission, permissible);
            ci.cancel();
        }
    }

    @Inject(method = "unsubscribeFromPermission", cancellable = true, at = @At("HEAD"))
    private void fusioncore$unsubscribe(String permission, org.bukkit.permissions.Permissible permissible, CallbackInfo ci) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.unsubscribeFromPermission(permission, permissible);
            ci.cancel();
        }
    }

    @Inject(method = "getPermissionSubscriptions", cancellable = true, at = @At("HEAD"))
    private void fusioncore$subscriptions(String permission, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<java.util.Set<org.bukkit.permissions.Permissible>> cir) {
        if (this.fusioncore$permissions != null) cir.setReturnValue(this.fusioncore$permissions.getPermissionSubscriptions(permission));
    }

    @Inject(method = "subscribeToDefaultPerms", cancellable = true, at = @At("HEAD"))
    private void fusioncore$subscribeDefault(boolean op, org.bukkit.permissions.Permissible permissible, CallbackInfo ci) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.subscribeToDefaultPerms(op, permissible);
            ci.cancel();
        }
    }

    @Inject(method = "unsubscribeFromDefaultPerms", cancellable = true, at = @At("HEAD"))
    private void fusioncore$unsubscribeDefault(boolean op, org.bukkit.permissions.Permissible permissible, CallbackInfo ci) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.unsubscribeFromDefaultPerms(op, permissible);
            ci.cancel();
        }
    }

    @Inject(method = "getDefaultPermSubscriptions", cancellable = true, at = @At("HEAD"))
    private void fusioncore$defaultSubscriptions(boolean op, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<java.util.Set<org.bukkit.permissions.Permissible>> cir) {
        if (this.fusioncore$permissions != null) cir.setReturnValue(this.fusioncore$permissions.getDefaultPermSubscriptions(op));
    }

    @Inject(method = "getPermissions", cancellable = true, at = @At("HEAD"))
    private void fusioncore$getPermissions(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<java.util.Set<org.bukkit.permissions.Permission>> cir) {
        if (this.fusioncore$permissions != null) cir.setReturnValue(this.fusioncore$permissions.getPermissions());
    }

    /** Fusion Core: Paper's {@code PermissionManager#clearPermissions} (API patch 0009), for {@code Server#reloadPermissions}. */
    public void clearPermissions() {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.clearPermissions();
            return;
        }
        this.permissions.clear();
        this.defaultPerms.get(true).clear();
        this.defaultPerms.get(false).clear();
    }

    /** Fusion Core: Paper's {@code PermissionManager#addPermissions}: all added, then permissibles recalculated once. */
    public void addPermissions(java.util.List<org.bukkit.permissions.Permission> perms) {
        if (this.fusioncore$permissions != null) {
            this.fusioncore$permissions.addPermissions(perms);
            return;
        }
        for (org.bukkit.permissions.Permission perm : perms) {
            this.addPermission(perm, false);
        }
        this.dirtyPermissibles();
    }

    @Inject(method = "disablePlugin(Lorg/bukkit/plugin/Plugin;)V", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lorg/bukkit/scheduler/BukkitScheduler;cancelTasks(Lorg/bukkit/plugin/Plugin;)V"))
    private void fusioncore$cancelFoliaTasks(Plugin plugin, CallbackInfo ci) {
        try {
            PaperSchedulers.GLOBAL.cancelTasks(plugin);
        } catch (Throwable ex) {
            this.server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while cancelling global tasks for "
                + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
        try {
            PaperSchedulers.ASYNC.cancelTasks(plugin);
        } catch (Throwable ex) {
            this.server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while cancelling async tasks for "
                + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
        // Fusion Core: Paper's lifecycle handlers of the plugin go with it (patch 0926)
        try {
            io.papermc.paper.plugin.lifecycle.event.LifecycleEventRunner.INSTANCE.unregisterAllEventHandlersFor(plugin);
        } catch (Throwable ex) {
            this.server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while unregistering lifecycle event handlers for "
                + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
        }
    }

    // Fusion Core: Paper's ServerExceptionEvent (Paper patch api/0021 "Add exception reporting event") for errors that the
    // plugin manager logs: a listener that threw (not for ServerExceptionEvent itself, no loop), and a plugin that failed
    // to enable or disable

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "fireEvent", at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;log(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V"))
    private void fusioncore$eventException(java.util.logging.Logger logger, Level level, String message, Throwable thrown, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                         @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) org.bukkit.event.Event event, @com.llamalad7.mixinextras.sugar.Local org.bukkit.plugin.RegisteredListener registration) {
        original.call(logger, level, message, thrown);
        if (!(event instanceof com.destroystokyo.paper.event.server.ServerExceptionEvent)) {
            this.server.getPluginManager().callEvent(new com.destroystokyo.paper.event.server.ServerExceptionEvent(
                new com.destroystokyo.paper.exception.ServerEventException(message, thrown, registration.getPlugin(), registration.getListener(), event)));
        }
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = {"enablePlugin", "disablePlugin(Lorg/bukkit/plugin/Plugin;)V"}, at = @At(value = "INVOKE", target = "Ljava/util/logging/Logger;log(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V"))
    private void fusioncore$enableDisableException(java.util.logging.Logger logger, Level level, String message, Throwable thrown, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
                                                 @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) Plugin plugin) {
        original.call(logger, level, message, thrown);
        this.server.getPluginManager().callEvent(new com.destroystokyo.paper.event.server.ServerExceptionEvent(
            new com.destroystokyo.paper.exception.ServerPluginEnableDisableException(message, thrown, plugin)));
    }
}
