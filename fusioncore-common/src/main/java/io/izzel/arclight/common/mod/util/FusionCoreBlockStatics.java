package io.izzel.arclight.common.mod.util;

/**
 * Fusion Core: Paper's statics on {@link org.bukkit.block.Block} (a mixin cannot add public statics); plugin calls are
 * rewritten here by {@code PaperCallTransformer}. Same packing as Paper's block key.
 */
public final class FusionCoreBlockStatics {

    private FusionCoreBlockStatics() {
    }

    public static long getBlockKey(int x, int y, int z) {
        return ((long) x & 0x7FFFFFF) | (((long) z & 0x7FFFFFF) << 27) | ((long) y << 54);
    }

    public static int getBlockKeyX(long packed) {
        return (int) ((packed << 37) >> 37);
    }

    public static int getBlockKeyY(long packed) {
        return (int) (packed >> 54);
    }

    public static int getBlockKeyZ(long packed) {
        return (int) ((packed << 10) >> 37);
    }
}
