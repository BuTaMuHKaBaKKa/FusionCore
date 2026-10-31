package io.izzel.arclight.common.mod.util;

import com.destroystokyo.paper.antixray.ChunkPacketBlockController;
import com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray;
import com.destroystokyo.paper.antixray.ChunkPacketInfo;
import com.mojang.logging.LogUtils;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.i18n.ArclightConfig;
import io.izzel.arclight.i18n.conf.AntiXraySpec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.GlobalPalette;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fusion Core: Paper's Anti-Xray on the core's chunk packets (section {@code anti-xray} of fusioncore.conf).
 * <p>
 * Paper keeps the replacement blocks in every chunk section's palette from the moment the section is created or read, so
 * the obfuscated bits always have a palette entry. Here they are put into the palette right before the chunk packet is
 * built, on the server thread (the palette may grow, as when a block is placed; the extra entries are not saved, the
 * palette is rebuilt from the blocks on save). While the packet data is written, the position of every section's block
 * bits in the packet buffer is recorded; then Paper's engine rewrites those bits in place on a background thread, and the
 * packet waits for it right before it is encoded for the network (packets keep their order). A chunk packet built off
 * the server thread is sent as it is.
 */
public final class FusionCoreAntiXray {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Object CREATE_LOCK = new Object();
    private static final ThreadLocal<Boolean> BYPASS = new ThreadLocal<>();
    private static final ThreadLocal<ChunkPacketInfo<BlockState>> INFO = new ThreadLocal<>();
    private static final ThreadLocal<int[]> SECTION = ThreadLocal.withInitial(() -> new int[]{-1, 0});
    private static final ThreadLocal<java.util.concurrent.CompletableFuture<Void>> TASK = new ThreadLocal<>();

    /** Holds the pending obfuscation of a chunk packet; the packet waits for it before it is encoded */
    public interface PendingPacket {

        void fusioncore$setAntiXrayTask(java.util.concurrent.CompletableFuture<Void> task);

        void fusioncore$awaitAntiXray();
    }

    // Paper runs the obfuscation on a background executor; the packet is encoded once it is done (see PendingPacket)
    private static void submit(Runnable obfuscation) {
        java.util.concurrent.CompletableFuture<Void> task;
        try {
            task = java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    obfuscation.run();
                } catch (Throwable t) {
                    LOGGER.error("Anti-Xray failed on a chunk packet, it is sent as it is", t);
                }
            }, net.minecraft.Util.backgroundExecutor());
        } catch (java.util.concurrent.RejectedExecutionException e) {
            obfuscation.run();
            task = java.util.concurrent.CompletableFuture.completedFuture(null);
        }
        TASK.set(task);
    }

    private FusionCoreAntiXray() {
    }

    public static ChunkPacketBlockController controller(Level level) {
        if (!(level instanceof ServerLevel)) {
            return ChunkPacketBlockController.NO_OPERATION_INSTANCE;
        }
        // kept on the level: a static map would hold unloaded levels through the controller's own reference to its level
        FusionCoreLevelData data = (FusionCoreLevelData) level;
        Object controller = data.fusioncore$antiXray();
        if (controller == null) {
            synchronized (CREATE_LOCK) {
                controller = data.fusioncore$antiXray();
                if (controller == null) {
                    controller = create(level);
                    data.fusioncore$setAntiXray(controller);
                }
            }
        }
        return (ChunkPacketBlockController) controller;
    }

    private static ChunkPacketBlockController create(Level level) {
        String name = ((WorldBridge) level).bridge$getWorld().getName();
        AntiXraySpec.Resolved conf = ArclightConfig.spec().getAntiXray().resolve(name);
        if (!conf.enabled()) {
            return ChunkPacketBlockController.NO_OPERATION_INSTANCE;
        }
        var settings = new ChunkPacketBlockControllerAntiXray.Settings(
            ChunkPacketBlockControllerAntiXray.EngineMode.of(conf.engineMode()), conf.maxBlockHeight(), conf.updateRadius(),
            conf.lavaObscures(), conf.usePermission(), blocks(conf.hiddenBlocks(), name), blocks(conf.replacementBlocks(), name));
        LOGGER.info("Anti-Xray in {}: engine-mode {}, {} hidden blocks, up to y {}", name, conf.engineMode(), settings.hiddenBlocks().size(), conf.maxBlockHeight());
        return new ChunkPacketBlockControllerAntiXray(level, FusionCoreAntiXray::submit, settings);
    }

    private static List<Block> blocks(List<String> ids, String world) {
        List<Block> blocks = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation key = ResourceLocation.tryParse(id.toLowerCase(Locale.ROOT));
            if (key != null && BuiltInRegistries.BLOCK.containsKey(key)) {
                blocks.add(BuiltInRegistries.BLOCK.get(key));
            } else {
                LOGGER.warn("Anti-Xray in {}: unknown block {}", world, id);
            }
        }
        return blocks;
    }

    /** Set around building the packet for a player who may see real blocks (paper.antixray.bypass with use-permission) */
    public static void setBypass(boolean bypass) {
        if (bypass) {
            BYPASS.set(true);
        } else {
            BYPASS.remove();
        }
    }

    /** Before the chunk data of a packet is built: puts the presets into the palettes, returns the info to fill, or null */
    public static ChunkPacketInfo<BlockState> begin(ClientboundLevelChunkWithLightPacket packet, LevelChunk chunk) {
        ChunkPacketBlockController controller = controller(chunk.getLevel());
        if (controller == ChunkPacketBlockController.NO_OPERATION_INSTANCE || Boolean.TRUE.equals(BYPASS.get())
            || !((ServerLevel) chunk.getLevel()).getServer().isSameThread()) {
            return null;
        }
        ChunkPacketInfo<BlockState> info = controller.getChunkPacketInfo(packet, chunk);
        if (info == null) {
            return null;
        }
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            BlockState[] presets = controller.getPresetBlockStates(chunk.getLevel(), chunk.getPos(), chunk.getMinSection() + i);
            if (presets == null) {
                continue;
            }
            PalettedContainer<BlockState> states = sections[i].getStates();
            if (!(states.data.palette() instanceof GlobalPalette)) {
                for (BlockState preset : presets) {
                    // may resize the container, so read the palette every time
                    states.data.palette().idFor(preset);
                }
            }
            info.setPresetValues(i, presets);
        }
        INFO.set(info);
        return info;
    }

    /** After the chunk data is built: starts the obfuscation of the recorded sections in the packet buffer */
    public static void end(ClientboundLevelChunkWithLightPacket packet, LevelChunk chunk, ChunkPacketInfo<BlockState> info) {
        INFO.remove();
        SECTION.get()[0] = -1;
        if (info != null && info.getBuffer() != null) {
            controller(chunk.getLevel()).modifyBlocks(packet, info);
            java.util.concurrent.CompletableFuture<Void> task = TASK.get();
            TASK.remove();
            if (task != null) {
                ((PendingPacket) packet).fusioncore$setAntiXrayTask(task);
            }
        }
    }

    public static ChunkPacketInfo<BlockState> current() {
        return INFO.get();
    }

    /** extractChunkData writes section {@code index} next */
    public static void section(int index) {
        SECTION.get()[0] = index;
    }

    /** LevelChunkSection#write is writing its block states (1) or not (0) */
    public static void statesWriting(boolean writing) {
        SECTION.get()[1] = writing ? 1 : 0;
    }

    /** PalettedContainer.Data#write, right before the block bits: records where they are in the packet buffer */
    public static void recordData(FriendlyByteBuf buf, BitStorage storage, Palette<?> palette) {
        ChunkPacketInfo<BlockState> info = INFO.get();
        int[] section = SECTION.get();
        if (info == null || section[0] < 0 || section[1] == 0) {
            return;
        }
        @SuppressWarnings("unchecked") Palette<BlockState> states = (Palette<BlockState>) palette;
        info.setBits(section[0], storage.getBits());
        info.setPalette(section[0], states);
        info.setIndex(section[0], buf.writerIndex() + VarInt.getByteSize(storage.getRaw().length));
    }
}
