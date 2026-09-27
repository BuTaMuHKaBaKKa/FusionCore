package co.aikar.timings;

import org.jetbrains.annotations.NotNull;

/**
 * Fusion Core: Paper's full-server-tick timing. Timings are always off here, so it does nothing.
 *
 * @deprecated Timings will be removed in the future
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("removal")
public class FullServerTickHandler extends TimingHandler {

    FullServerTickHandler() {
    }

    @NotNull
    @Override
    public Timing startTiming() {
        return this;
    }

    @Override
    public void stopTiming() {
    }
}
