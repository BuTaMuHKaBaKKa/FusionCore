package io.izzel.arclight.common.mod.util;

/**
 * Fusion Core: Paper's chunks.max-auto-save-chunks-per-tick. Marks the server's periodic autosave (saveEverything without
 * flush and force) so that the chunk maps queue their chunks instead of saving them in one tick.
 */
public final class FusionCoreAutoSave {

    private static boolean autoSaving;
    /** Chunks saved from the autosave queues since the start (for /fusioncore-style diagnostics and probes) */
    public static long savedFromQueue;

    private FusionCoreAutoSave() {
    }

    /** Implemented by ChunkMap: chunks waiting for their autosave */
    public interface Queue {

        int fusioncore$autoSaveQueued();
    }

    public static boolean isAutoSaving() {
        return autoSaving;
    }

    public static void setAutoSaving(boolean value) {
        autoSaving = value;
    }
}
