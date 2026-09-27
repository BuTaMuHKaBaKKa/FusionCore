package co.aikar.timings;

/**
 * Fusion Core: the public tick counters of Paper's {@code TimingHistory}; unused while timings are off.
 *
 * @deprecated Timings will be removed in the future
 */
@Deprecated(forRemoval = true)
public class TimingHistory {

    public static long lastMinuteTime;
    public static long timedTicks;
    public static long playerTicks;
    public static long entityTicks;
    public static long tileEntityTicks;
    public static long activatedEntityTicks;

    private TimingHistory() {
    }
}
