package io.izzel.arclight.common.mod.util;

import com.mojang.logging.LogUtils;
import io.izzel.arclight.i18n.ArclightConfig;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Fusion Core: player data saved in the background ({@code optimization.async-playerdata-save}). The player's NBT, with
 * the data mods add through NeoForge's events, is still built and compressed on the server thread; one background thread
 * only writes the bytes to disk, in order. Reading a player's file waits for its pending write; if the write does not
 * finish in time or failed, the reader gets the newest data from memory instead of the older file. The server waits for
 * all writes when it stops.
 */
public final class FusionCorePlayerDataSaving {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadLocal<byte[]> ENCODED = new ThreadLocal<>();
    private static final Map<String, Pending> PENDING = new ConcurrentHashMap<>();
    private static volatile ExecutorService executor;

    /** The newest data of a player not yet known to be on disk; it stays here until a write of it succeeds */
    private static final class Pending {
        final byte[] bytes;
        volatile Future<?> future;

        Pending(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    private FusionCorePlayerDataSaving() {
    }

    public static boolean enabled() {
        return ArclightConfig.spec().getOptimization().isAsyncPlayerDataSave();
    }

    private static synchronized ExecutorService executor() {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread thread = new Thread(r, "Fusion Core player data IO");
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY - 1);
                return thread;
            });
        }
        return executor;
    }

    /** In place of writing the temporary file: compress now, keep the bytes for {@link #submit} */
    public static void encode(CompoundTag tag) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(16384);
        NbtIo.writeCompressed(tag, out);
        ENCODED.set(out.toByteArray());
    }

    public static boolean hasEncoded() {
        return ENCODED.get() != null;
    }

    /** In place of replacing the player's file: write the encoded bytes to the temporary file and replace, in the background */
    public static void submit(String uuid, String name, Path file, Path temp, Path old) {
        byte[] bytes = ENCODED.get();
        ENCODED.remove();
        Pending pending = new Pending(bytes);
        PENDING.put(uuid, pending);
        pending.future = executor().submit(() -> {
            try {
                Files.write(temp, bytes);
                Util.safeReplaceFile(file, temp, old);
                // on disk now; forget it unless a newer save replaced it
                PENDING.remove(uuid, pending);
            } catch (Exception e) {
                // the file keeps older data: the entry stays, readers take the bytes from memory
                LOGGER.error("Failed to save player data for {}", name, e);
            }
        });
    }

    /**
     * Waits for the pending write of this player. Returns null when the file is up to date (nothing pending, or written), or
     * the newest compressed data when the write did not finish in time or failed: the caller reads that, not the file.
     */
    public static byte[] await(String uuid) {
        Pending pending = PENDING.get(uuid);
        if (pending == null) {
            return null;
        }
        Future<?> future = pending.future;
        try {
            if (future != null) {
                future.get(30, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.warn("The player data of {} was not written in time; using the newest data from memory", uuid);
        }
        return PENDING.get(uuid) == pending ? pending.bytes : null;
    }

    /** {@link #await} decoded: the newest data of the player when its file is behind, else null (read the file) */
    public static CompoundTag awaitNewer(String uuid) {
        byte[] bytes = await(uuid);
        if (bytes == null) {
            return null;
        }
        try {
            return NbtIo.readCompressed(new java.io.ByteArrayInputStream(bytes), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            LOGGER.error("Cannot decode the pending player data of {}", uuid, e);
            return null;
        }
    }

    /** Writes everything that is pending; the server calls it when it stops */
    public static void flush() {
        ExecutorService service;
        synchronized (FusionCorePlayerDataSaving.class) {
            service = executor;
            executor = null;
        }
        if (service == null) {
            return;
        }
        service.shutdown();
        try {
            if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
                LOGGER.error("Player data writes did not finish in 60 seconds; not on disk: {}", PENDING.keySet());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (!PENDING.isEmpty()) {
            LOGGER.error("Player data that could not be written: {}", PENDING.keySet());
        }
    }
}
