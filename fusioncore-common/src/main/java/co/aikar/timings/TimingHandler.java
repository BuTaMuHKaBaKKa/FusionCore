package co.aikar.timings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: stand-in for Paper's package-private {@code TimingHandler}; measures nothing (timings are off). */
@SuppressWarnings("removal")
class TimingHandler implements Timing {

    TimingHandler() {
    }

    @NotNull
    @Override
    public Timing startTiming() {
        return this;
    }

    @Override
    public void stopTiming() {
    }

    @NotNull
    @Override
    public Timing startTimingIfSync() {
        return this;
    }

    @Override
    public void stopTimingIfSync() {
    }

    @Override
    public void abort() {
    }

    @Nullable
    @Override
    public TimingHandler getTimingHandler() {
        return this;
    }

    @Override
    public void close() {
    }
}
