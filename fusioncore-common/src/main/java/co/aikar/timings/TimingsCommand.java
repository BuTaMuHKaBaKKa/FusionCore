package co.aikar.timings;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

/**
 * Fusion Core: Paper's {@code /timings} command for plugins that register it; points to spark. Not registered by default.
 *
 * @deprecated Timings will be removed in the future
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("removal")
public class TimingsCommand extends BukkitCommand {
    private static final List<String> TIMINGS_SUBCOMMANDS = ImmutableList.of("report", "reset", "on", "off", "paste", "verbon", "verboff");

    public TimingsCommand(@NotNull String name) {
        super(name);
        this.description = "Manages Spigot Timings data to see performance of the server.";
        this.usageMessage = "/timings <reset|report|on|off|verbon|verboff>";
        this.setPermission("bukkit.command.timings");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String currentAlias, @NotNull String[] args) {
        if (!testPermission(sender)) {
            return true;
        }
        if (args.length < 1) {
            audience(sender).sendMessage(text("Usage: " + this.usageMessage, NamedTextColor.RED));
            return true;
        }
        if ("off".equalsIgnoreCase(args[0])) {
            Timings.setTimingsEnabled(false);
            audience(sender).sendMessage(text("Disabled Timings"));
            return true;
        }
        audience(sender).sendMessage(text(Timings.UNAVAILABLE, NamedTextColor.RED));
        return true;
    }

    /** javac does not see that {@code CommandSender} is an Adventure {@code Audience} (Fusion Core adds it with a mixin). */
    private static Audience audience(CommandSender sender) {
        return (Audience) (Object) sender;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        Preconditions.checkNotNull(sender, "Sender cannot be null");
        Preconditions.checkNotNull(args, "Arguments cannot be null");
        Preconditions.checkNotNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], TIMINGS_SUBCOMMANDS,
                new ArrayList<String>(TIMINGS_SUBCOMMANDS.size()));
        }
        return ImmutableList.of();
    }
}
