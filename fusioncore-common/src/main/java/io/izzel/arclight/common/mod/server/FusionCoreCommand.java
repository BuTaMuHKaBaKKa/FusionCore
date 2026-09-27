package io.izzel.arclight.common.mod.server;

import io.izzel.arclight.api.ArclightVersion;
import io.izzel.arclight.common.mod.ArclightCommon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Fusion Core additions. Licensed under GPL-3.0 with the parent project.
 *
 * <p>Loader specifics are read through {@link ArclightCommon.Api} rather than a loader API
 * directly, so this command stays in the platform-agnostic module.</p>
 */
public final class FusionCoreCommand extends Command {

    public static final String PERMISSION = "fusioncore.admin";

    private FusionCoreCommand() {
        super("fusioncore");
        setDescription("Show Fusion Core version and runtime diagnostics");
        setUsage("/fusioncore [info|status|mods|plugins]");
        setPermission(PERMISSION);
    }

    public static void register(CraftServer server) {
        if (server.getPluginManager().getPermission(PERMISSION) == null) {
            server.getPluginManager().addPermission(new Permission(PERMISSION,
                "View Fusion Core runtime diagnostics", PermissionDefault.OP));
        }
        server.getCommandMap().register("fusioncore", new FusionCoreCommand());
        io.izzel.arclight.common.mod.util.FusionCoreAdventureProviders.registerCommands(server); // Paper's /callback
    }

    private static String version() {
        String version = System.getProperty("arclight.version");
        return version == null ? "unknown build" : version;
    }

    private static String loader() {
        var api = ArclightCommon.api();
        return api == null ? "unknown" : api.loaderName() + " " + api.loaderVersion();
    }

    private static List<String> mods() {
        var api = ArclightCommon.api();
        return api == null ? List.of() : api.loadedMods();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        if (args.length > 1) {
            sender.sendMessage(getUsage());
            return true;
        }
        String action = args.length == 0 ? "info" : args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "info" -> {
                sender.sendMessage(ChatColor.AQUA + "Fusion Core " + version());
                sender.sendMessage("Bukkit " + Bukkit.getBukkitVersion() + " on " + loader()
                    + ", based on Arclight " + ArclightVersion.current().getReleaseName() + " (GPL-3.0).");
                sender.sendMessage("Paper/Purpur-only APIs are not fully implemented.");
                sender.sendMessage(getUsage());
            }
            case "status" -> {
                Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
                long enabled = Arrays.stream(plugins).filter(Plugin::isEnabled).count();
                Runtime runtime = Runtime.getRuntime();
                long mib = 1024L * 1024L;
                sender.sendMessage(ChatColor.AQUA + "Fusion Core runtime status");
                sender.sendMessage(loader() + " | Loaded mods: " + mods().size());
                sender.sendMessage("Plugins enabled: " + enabled + "/" + plugins.length
                    + " | Worlds: " + Bukkit.getWorlds().size()
                    + " | Players: " + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
                sender.sendMessage("Heap used/max: " + (runtime.totalMemory() - runtime.freeMemory()) / mib
                    + "/" + runtime.maxMemory() / mib + " MiB | JVM uptime: "
                    + ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + " s");
            }
            case "mods" -> {
                List<String> mods = mods();
                if (mods.isEmpty()) {
                    sender.sendMessage("No mod list available from the loader.");
                    return true;
                }
                sender.sendMessage(ChatColor.AQUA + "Loaded mods (" + mods.size() + "):");
                mods.forEach(sender::sendMessage);
            }
            case "plugins" -> {
                Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
                if (plugins.length == 0) {
                    sender.sendMessage("No plugins loaded. Put Bukkit/Spigot plugin JARs in plugins/ and restart.");
                    return true;
                }
                Arrays.stream(plugins).sorted(Comparator.comparing(Plugin::getName, String.CASE_INSENSITIVE_ORDER))
                    .forEach(plugin -> sender.sendMessage((plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED)
                        + plugin.getDescription().getFullName() + (plugin.isEnabled() ? " [enabled]" : " [disabled]")));
            }
            default -> sender.sendMessage(getUsage());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!testPermissionSilent(sender) || args.length != 1) {
            return Collections.emptyList();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return List.of("info", "status", "mods", "plugins").stream()
            .filter(value -> value.startsWith(prefix)).toList();
    }
}
