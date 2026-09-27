package io.izzel.arclight.common.mod.util;

/**
 * Fusion Core: Paper's static methods on {@link org.bukkit.Chunk}; a mixin cannot add public statics, so
 * {@code PaperCallTransformer} points plugin calls here.
 */
public final class FusionCoreChunkStatics {

    private FusionCoreChunkStatics() {
    }

    /** Paper: chunk x in the low 32 bits, z in the high 32 bits. */
    public static long getChunkKey(int x, int z) {
        return (long) x & 0xffffffffL | ((long) z & 0xffffffffL) << 32;
    }

    public static long getChunkKey(org.bukkit.Location loc) {
        return getChunkKey((int) Math.floor(loc.getX()) >> 4, (int) Math.floor(loc.getZ()) >> 4);
    }
}
