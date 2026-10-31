package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.TpsBarSpec;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Fusion Core: Purpur's {@code /tpsbar}, a boss bar per player with TPS, MSPT and ping. Settings in the {@code tpsbar}
 * section of fusioncore.conf. Everything runs on the server thread: the command, and {@link #tick} from the server tick.
 * TPS and MSPT are the values of {@code /tps}: Paper's {@code getTPS()[0]} and {@code getAverageTickTime()}.
 */
public final class FusionCoreTpsBar {

    public static final String PERMISSION = "purpur.command.tpsbar";
    public static final String PERMISSION_OTHER = "purpur.command.tpsbar.other";

    private static final Map<UUID, Shown> SHOWN = new LinkedHashMap<>();
    // kept in the player's data: a bar that was on comes back on join and after a restart, until /tpsbar turns it off
    private static final NamespacedKey KEY = new NamespacedKey("fusioncore", "tpsbar");

    private FusionCoreTpsBar() {
    }

    private record Shown(Player player, BossBar bar) {
    }

    private static TpsBarSpec spec() {
        return ArclightConfig.spec().getTpsBar();
    }

    public static void register(CraftServer server) {
        if (!spec().isEnabled()) {
            return;
        }
        if (server.getPluginManager().getPermission(PERMISSION) == null) {
            server.getPluginManager().addPermission(new Permission(PERMISSION, "Toggle the TPS boss bar", PermissionDefault.OP));
        }
        if (server.getPluginManager().getPermission(PERMISSION_OTHER) == null) {
            server.getPluginManager().addPermission(new Permission(PERMISSION_OTHER, "Toggle the TPS boss bar of another player", PermissionDefault.OP));
        }
        server.getCommandMap().register("tpsbar", "purpur", new TpsBarCommand());
    }

    /** From the server tick: drops the bars of players who left, updates the rest every tick-interval ticks. */
    public static void tick(int tickCount) {
        if (SHOWN.isEmpty() || tickCount % spec().getTickInterval() != 0) {
            return;
        }
        for (Iterator<Shown> it = SHOWN.values().iterator(); it.hasNext(); ) {
            Shown shown = it.next();
            // the same Player object while online; a rejoin is a new one, which gets the bar again in onJoin
            if (Bukkit.getPlayer(shown.player().getUniqueId()) != shown.player()) {
                FusionCoreAudiences.hideBossBar(shown.player(), shown.bar());
                it.remove();
            }
        }
        if (!SHOWN.isEmpty()) {
            Values values = Values.now();
            for (Shown shown : SHOWN.values()) {
                update(shown, values);
            }
        }
    }

    /** At server stop: bars are taken off and forgotten. */
    public static void stop() {
        for (Shown shown : new ArrayList<>(SHOWN.values())) {
            FusionCoreAudiences.hideBossBar(shown.player(), shown.bar());
        }
        SHOWN.clear();
    }

    /** Shows or hides the bar of a player; true when it is shown now. */
    public static boolean toggle(Player player) {
        Shown old = SHOWN.remove(player.getUniqueId());
        if (old != null) {
            FusionCoreAudiences.hideBossBar(old.player(), old.bar());
            if (old.player() == player) {
                player.getPersistentDataContainer().remove(KEY);
                return false;
            }
        }
        player.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
        show(player);
        return true;
    }

    /** After a player joined: the bar is shown again if it was on when they left (and they still may use /tpsbar). */
    public static void onJoin(Player player) {
        if (spec().isEnabled() && !isShown(player) && player.getPersistentDataContainer().has(KEY, PersistentDataType.BYTE)
            && player.hasPermission(PERMISSION)) {
            Shown old = SHOWN.remove(player.getUniqueId());
            if (old != null) {
                FusionCoreAudiences.hideBossBar(old.player(), old.bar());
            }
            show(player);
        }
    }

    private static void show(Player player) {
        Shown shown = new Shown(player, BossBar.bossBar(Component.empty(), 0F, BossBar.Color.GREEN, overlay()));
        update(shown, Values.now());
        FusionCoreAudiences.showBossBar(player, shown.bar());
        SHOWN.put(player.getUniqueId(), shown);
    }

    public static boolean isShown(Player player) {
        Shown shown = SHOWN.get(player.getUniqueId());
        return shown != null && shown.player() == player;
    }

    private record Values(double tps, double mspt) {
        static Values now() {
            return new Values(FusionCorePaperServer.getTPS()[0], FusionCorePaperServer.getAverageTickTime());
        }
    }

    private enum Level {GOOD, MEDIUM, LOW}

    private static void update(Shown shown, Values values) {
        TpsBarSpec spec = spec();
        TpsBarSpec.Thresholds limits = spec.getThresholds();
        int ping = shown.player().getPing();
        Level tpsLevel = values.tps() >= limits.getTpsGood() ? Level.GOOD : values.tps() >= limits.getTpsMedium() ? Level.MEDIUM : Level.LOW;
        Level msptLevel = values.mspt() < limits.getMsptGood() ? Level.GOOD : values.mspt() < limits.getMsptMedium() ? Level.MEDIUM : Level.LOW;
        Level pingLevel = ping < limits.getPingGood() ? Level.GOOD : ping < limits.getPingMedium() ? Level.MEDIUM : Level.LOW;

        MiniMessage mm = MiniMessage.miniMessage();
        Component title = mm.deserialize(spec.getTitle(),
            Placeholder.component("tps", colored(String.format(Locale.ROOT, "%.1f", Math.min(20D, values.tps())), tpsLevel)),
            Placeholder.component("mspt", colored(String.format(Locale.ROOT, "%.1f", values.mspt()), msptLevel)),
            Placeholder.component("ping", colored(Integer.toString(ping), pingLevel)));

        float progress;
        Level barLevel;
        switch (spec.getFillMode().toUpperCase(Locale.ROOT)) {
            case "TPS" -> {
                progress = (float) (values.tps() / 20D);
                barLevel = tpsLevel;
            }
            case "PING" -> {
                progress = (float) ping / Math.max(1, limits.getPingMedium());
                barLevel = pingLevel;
            }
            default -> {
                progress = (float) (values.mspt() / 50D);
                barLevel = msptLevel;
            }
        }
        BossBar bar = shown.bar();
        bar.name(title);
        bar.progress(Math.max(0F, Math.min(1F, progress)));
        bar.color(color(barLevel));
        bar.overlay(overlay());
    }

    private static Component colored(String value, Level level) {
        TpsBarSpec.Levels colors = spec().getTextColor();
        String template = switch (level) {
            case GOOD -> colors.getGood();
            case MEDIUM -> colors.getMedium();
            case LOW -> colors.getLow();
        };
        return MiniMessage.miniMessage().deserialize(template == null ? "<text>" : template, Placeholder.unparsed("text", value));
    }

    private static BossBar.Color color(Level level) {
        TpsBarSpec.Levels colors = spec().getProgressColor();
        String name = switch (level) {
            case GOOD -> colors.getGood();
            case MEDIUM -> colors.getMedium();
            case LOW -> colors.getLow();
        };
        try {
            return BossBar.Color.valueOf(String.valueOf(name).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return level == Level.GOOD ? BossBar.Color.GREEN : level == Level.MEDIUM ? BossBar.Color.YELLOW : BossBar.Color.RED;
        }
    }

    private static BossBar.Overlay overlay() {
        try {
            return BossBar.Overlay.valueOf(String.valueOf(spec().getOverlay()).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Overlay.NOTCHED_20;
        }
    }

    private static final class TpsBarCommand extends Command {

        private TpsBarCommand() {
            super("tpsbar");
            setDescription("Toggle a boss bar with TPS, MSPT and ping");
            setUsage("/tpsbar [player]");
            setPermission(PERMISSION);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            if (!testPermission(sender)) {
                return true;
            }
            Player target;
            if (args.length == 0) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Usage: /tpsbar <player>");
                    return true;
                }
                target = player;
            } else if (args.length == 1) {
                if (sender instanceof Player self && args[0].equalsIgnoreCase(self.getName())) {
                    target = self;
                } else {
                    if (!sender.hasPermission(PERMISSION_OTHER)) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to toggle the bar of another player.");
                        return true;
                    }
                    target = Bukkit.getPlayerExact(args[0]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Player " + args[0] + " is not online.");
                        return true;
                    }
                }
            } else {
                sender.sendMessage("Usage: " + getUsage());
                return true;
            }
            boolean on = toggle(target);
            String state = on ? ChatColor.GREEN + "on" : ChatColor.RED + "off";
            sender.sendMessage(ChatColor.GRAY + "TPS bar " + state
                + (target == sender ? "" : ChatColor.GRAY + " for " + target.getName()));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
            if (args.length == 1 && sender.hasPermission(PERMISSION_OTHER)) {
                List<String> names = new ArrayList<>();
                String prefix = args[0].toLowerCase(Locale.ROOT);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                        names.add(player.getName());
                    }
                }
                return names;
            }
            return List.of();
        }
    }
}
