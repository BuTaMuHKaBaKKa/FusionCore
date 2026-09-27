package org.bukkit;

/**
 * Fusion Core: Paper's deprecated {@code World.ChunkLoadCallback}, compiled as a top-level class under the binary
 * name the JVM looks up (Spigot's World is not ours to edit).
 */
@Deprecated
@SuppressWarnings("checkstyle:TypeName")
public interface World$ChunkLoadCallback extends java.util.function.Consumer<Chunk> {

    void onLoad(Chunk chunk);

    @Override
    default void accept(Chunk chunk) {
        onLoad(chunk);
    }
}
