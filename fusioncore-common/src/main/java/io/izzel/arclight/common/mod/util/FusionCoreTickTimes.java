package io.izzel.arclight.common.mod.util;

/** Fusion Core: Paper's {@code MinecraftServer.TickTimes}, handed to plugins that look it up by reflection. */
public class FusionCoreTickTimes {

    /** The name plugins know this class by. */
    public static final String PAPER_NAME = "net.minecraft.server.MinecraftServer$TickTimes";

    private final long[] times;

    public FusionCoreTickTimes(int length) {
        this.times = new long[length];
    }

    void add(int index, long time) {
        this.times[index % this.times.length] = time;
    }

    public long[] getTimes() {
        return this.times.clone();
    }

    public double getAverage() {
        long total = 0L;
        for (long value : this.times) {
            total += value;
        }
        return ((double) total / (double) this.times.length) * 1.0E-6D;
    }

    /** Records one tick's duration in the three rings, as Paper does at the end of {@code tickServer}. */
    public static void record(FusionCoreTickTimes five, FusionCoreTickTimes ten, FusionCoreTickTimes sixty, int tickCount, long nanos) {
        five.add(tickCount, nanos);
        ten.add(tickCount, nanos);
        sixty.add(tickCount, nanos);
    }
}
