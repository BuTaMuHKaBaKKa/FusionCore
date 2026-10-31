package io.izzel.arclight.common.mixin.core.server.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreAutoSave;
import io.izzel.arclight.common.mod.util.FusionCoreWorldSettings;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

// Fusion Core: Paper's chunks.max-auto-save-chunks-per-tick. The autosave (not /save-all, not a stop) queues the loaded
// chunks instead of saving them all in one tick; every tick at most that many are saved through vanilla's saveChunkIfNeeded,
// so the chunk save cooldown, NeoForge's ChunkDataEvent.Save and the storage of mods' data are the same as before.
@Mixin(ChunkMap.class)
public abstract class ChunkMapAutoSaveMixin implements FusionCoreAutoSave.Queue {

    // @formatter:off
    @Shadow @Final public ServerLevel level;
    @Shadow private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap;
    @Shadow private boolean saveChunkIfNeeded(ChunkHolder holder) { return false; }
    // @formatter:on

    @Unique private final LongLinkedOpenHashSet fusioncore$autoSaveQueue = new LongLinkedOpenHashSet();

    // any other save (/save-all, flush, stop, World#save) saves every chunk now; what the queue still holds is done
    @Inject(method = "saveAllChunks", at = @At("HEAD"))
    private void fusioncore$fullSave(boolean flush, CallbackInfo ci) {
        if (!FusionCoreAutoSave.isAutoSaving()) {
            this.fusioncore$autoSaveQueue.clear();
        }
    }

    @WrapOperation(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectCollection;forEach(Ljava/util/function/Consumer;)V"))
    private void fusioncore$queueAutoSave(ObjectCollection<ChunkHolder> holders, Consumer<? super ChunkHolder> save, Operation<Void> original) {
        if (!FusionCoreAutoSave.isAutoSaving() || FusionCoreWorldSettings.spec(this.level).getChunks().getMaxAutoSaveChunksPerTick() <= 0) {
            original.call(holders, save);
            return;
        }
        for (ChunkHolder holder : holders) {
            this.fusioncore$autoSaveQueue.add(holder.getPos().toLong());
        }
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("HEAD"))
    private void fusioncore$drainAutoSave(BooleanSupplier hasMoreTime, CallbackInfo ci) {
        // /save-off (level.noSave): vanilla writes nothing more from ChunkMap#tick, neither does the queue
        if (this.fusioncore$autoSaveQueue.isEmpty() || this.level.noSave()) {
            return;
        }
        int limit = FusionCoreWorldSettings.spec(this.level).getChunks().getMaxAutoSaveChunksPerTick();
        if (limit <= 0) {
            limit = Integer.MAX_VALUE;
        }
        // chunks that need no save cost a map lookup each; bound the walk so one tick never scans the whole queue
        int saved = 0;
        int checked = 0;
        int maxChecked = limit >= Integer.MAX_VALUE / 16 ? Integer.MAX_VALUE : limit * 16;
        while (saved < limit && checked < maxChecked && !this.fusioncore$autoSaveQueue.isEmpty()) {
            long pos = this.fusioncore$autoSaveQueue.removeFirstLong();
            checked++;
            ChunkHolder holder = this.visibleChunkMap.get(pos);
            if (holder != null && this.saveChunkIfNeeded(holder)) {
                saved++;
                FusionCoreAutoSave.savedFromQueue++;
            }
        }
    }

    @Override
    public int fusioncore$autoSaveQueued() {
        return this.fusioncore$autoSaveQueue.size();
    }
}
