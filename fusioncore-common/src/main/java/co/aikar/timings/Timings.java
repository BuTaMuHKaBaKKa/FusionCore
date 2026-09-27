package co.aikar.timings;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's timings API as it behaves with timings disabled: every timing is a no-op. Use spark to profile. */
@Deprecated(forRemoval = true)
@SuppressWarnings("removal")
public final class Timings {

    public static final Timing NULL_HANDLER = new NullTimingHandler();

    static final String UNAVAILABLE = "Timings are not available on Fusion Core (Paper has scheduled them for removal); use the spark profiler.";

    private static boolean verboseEnabled = false;
    private static int historyInterval = -1;
    private static int historyLength = -1;

    private Timings() {
    }

    @NotNull
    public static Timing of(@NotNull Plugin plugin, @NotNull String name) {
        return NULL_HANDLER;
    }

    @NotNull
    public static Timing of(@NotNull Plugin plugin, @NotNull String name, @Nullable Timing groupHandler) {
        return NULL_HANDLER;
    }

    @NotNull
    public static Timing ofStart(@NotNull Plugin plugin, @NotNull String name) {
        return NULL_HANDLER.startTiming();
    }

    @NotNull
    public static Timing ofStart(@NotNull Plugin plugin, @NotNull String name, @Nullable Timing groupHandler) {
        return NULL_HANDLER.startTiming();
    }

    public static boolean isTimingsEnabled() {
        return false;
    }

    public static void setTimingsEnabled(boolean enabled) {
        if (enabled) {
            Bukkit.getLogger().warning(UNAVAILABLE);
        }
    }

    @NotNull
    public static Component deprecationMessage() {
        return Component.text()
            .color(TextColor.color(0xffc93a))
            .append(Component.text("[!] The timings profiler has been enabled but has been scheduled for removal from Paper in the future."))
            .append(Component.newline())
            .append(Component.text("    We recommend migrating to the spark profiler."))
            .append(Component.newline())
            .append(Component.text("    For more information please visit: ")
                .append(Component.text()
                    .content("https://github.com/PaperMC/Paper/discussions/10565")
                    .clickEvent(ClickEvent.openUrl("https://github.com/PaperMC/Paper/discussions/10565"))))
            .build();
    }

    public static boolean isVerboseTimingsEnabled() {
        return verboseEnabled;
    }

    public static void setVerboseTimingsEnabled(boolean enabled) {
        verboseEnabled = enabled;
    }

    public static int getHistoryInterval() {
        return historyInterval;
    }

    public static void setHistoryInterval(int interval) {
        historyInterval = interval;
    }

    public static int getHistoryLength() {
        return historyLength;
    }

    public static void setHistoryLength(int length) {
        historyLength = length;
    }

    public static void reset() {
    }

    public static void generateReport(@Nullable CommandSender sender) {
        (sender == null ? Bukkit.getConsoleSender() : sender).sendMessage(UNAVAILABLE);
    }

    /**
     * Paper queues a report and calls {@link TimingsReportListener#done(String)} with its URL once uploaded. No report
     * is made here: the listener's senders get the notice and {@code done} runs at once with no URL, so a plugin
     * waiting for the callback is not left hanging.
     */
    public static void generateReport(@NotNull TimingsReportListener sender) {
        sender.sendMessage(UNAVAILABLE);
        sender.done();
    }
}
