package io.izzel.arclight.common.mod.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class ArclightCallbackExecutor implements Executor, Runnable {

    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    // Fusion Core: while chunk ticket levels are being applied, chunk load/unload events wait (server thread only)
    private int held;

    @Override
    public void execute(Runnable runnable) {
        queue.add(runnable);
    }

    /**
     * Fusion Core: events queued from now on wait for {@link #release()}. A plugin handling ChunkLoadEvent in the middle of
     * DistanceManager#runAllUpdates could load or unload chunks while the remaining chunks were being scheduled, and a chunk
     * then went to generation with a neighbour that had no ChunkHolder (NPE in ChunkMap#acquireGeneration).
     */
    public void hold() {
        held++;
    }

    /** Ends a {@link #hold()}; the last one runs what was queued meanwhile */
    public void release() {
        if (--held == 0) {
            run();
        }
    }

    @Override
    public void run() {
        if (held > 0) {
            return;
        }
        for (;;) {
            var poll = queue.poll();
            if (poll != null) {
                poll.run();
            } else {
                return;
            }
        }
    }
}
