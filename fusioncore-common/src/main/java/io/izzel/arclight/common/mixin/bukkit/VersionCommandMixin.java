package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.common.mod.server.FusionCoreCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VersionCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core builds have no entry in Spigot's commit history, so the upstream check
 * reports them as "unknown version". Describe the build instead.
 */
@Mixin(value = VersionCommand.class, remap = false)
public class VersionCommandMixin {

    @Inject(method = "sendVersion", at = @At("HEAD"), cancellable = true)
    private void fusioncore$describeBuild(CommandSender sender, CallbackInfo ci) {
        String version = System.getProperty("arclight.version");
        String line = "Fusion Core " + (version == null ? "unknown build" : version) + ".";
        // /version is open to everyone; only point at /fusioncore for those who can run it.
        if (sender.hasPermission(FusionCoreCommand.PERMISSION)) {
            line += " Use /fusioncore for build details.";
        }
        sender.sendMessage(line);
        // Fusion Core: the previous version from Paper's version history, as Paper's /version shows it
        com.destroystokyo.paper.VersionHistoryManager.VersionData data = com.destroystokyo.paper.VersionHistoryManager.INSTANCE.getVersionData();
        if (data != null && data.getOldVersion() != null) {
            sender.sendMessage(org.bukkit.ChatColor.GRAY + "" + org.bukkit.ChatColor.ITALIC + "Previous version: " + data.getOldVersion());
        }
        ci.cancel();
    }
}
