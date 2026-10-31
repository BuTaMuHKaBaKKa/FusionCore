package io.izzel.arclight.common.mixin.core.server.level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreTrackerGrid;
import io.izzel.arclight.i18n.ArclightConfig;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// Fusion Core: optimization.tracker-nearby-players - an entity that changed section is checked against the players near it
// (and those that see it now) instead of every player of the level; the result is the same (FusionCoreTrackerGrid)
@Mixin(ChunkMap.class)
public abstract class ChunkMapTrackerMixin {

    // @formatter:off
    @Shadow @Final public ServerLevel level;
    // @formatter:on

    @Unique private static final int FUSIONCORE$MIN_PLAYERS = 8;
    @Unique private final FusionCoreTrackerGrid fusioncore$grid = new FusionCoreTrackerGrid();
    @Unique private boolean fusioncore$gridReady;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void fusioncore$buildGrid(CallbackInfo ci) {
        List<ServerPlayer> players = this.level.players();
        boolean wasReady = this.fusioncore$gridReady;
        this.fusioncore$gridReady = players.size() >= FUSIONCORE$MIN_PLAYERS && ArclightConfig.spec().getOptimization().isTrackerNearbyPlayers();
        if (this.fusioncore$gridReady) {
            this.fusioncore$grid.build(players);
        } else if (wasReady) {
            // the last snapshot must not keep players that left
            this.fusioncore$grid.clear();
        }
    }

    @WrapOperation(method = "tick()V", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/server/level/ChunkMap$TrackedEntity;updatePlayers(Ljava/util/List;)V"))
    private void fusioncore$nearbyPlayers(ChunkMap.TrackedEntity tracked, List<ServerPlayer> players, Operation<Void> original) {
        if (this.fusioncore$gridReady && players.size() == this.fusioncore$grid.playerCount()) {
            original.call(tracked, this.fusioncore$grid.candidates(tracked, players));
        } else {
            original.call(tracked, players);
        }
    }
}
