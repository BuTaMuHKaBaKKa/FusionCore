package io.izzel.arclight.common.mixin.core.world.level.levelgen;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.server.level.ServerLevelBridge;
import io.izzel.arclight.common.bridge.core.world.entity.monster.PhantomBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.util.CraftLocation;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    // Fusion Core: player whose remaining phantoms this tick are skipped (PhantomPreSpawnEvent#setShouldAbortSpawn)
    @Unique private ServerPlayer fusioncore$abortedFor;

    @Inject(method = "tick", at = @At("HEAD"))
    private void fusioncore$resetAbort(ServerLevel level, boolean bl, boolean bl2, CallbackInfoReturnable<Integer> cir) {
        this.fusioncore$abortedFor = null;
    }

    /** Fusion Core: Paper's PhantomPreSpawnEvent, fired before each phantom is created. */
    @Decorate(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
    private Entity fusioncore$phantomPreSpawn(EntityType<?> type, Level level, @Local(ordinal = -1) ServerPlayer player, @Local(ordinal = -1) BlockPos pos) throws Throwable {
        if (player == this.fusioncore$abortedFor) {
            return null;
        }
        var event = new PhantomPreSpawnEvent(CraftLocation.toBukkit(pos, level), ((EntityBridge) player).bridge$getBukkitEntity(), CreatureSpawnEvent.SpawnReason.NATURAL);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (event.shouldAbortSpawn()) {
                this.fusioncore$abortedFor = player;
            }
            return null;
        }
        var entity = (Entity) DecorationOps.callsite().invoke(type, level);
        if (entity instanceof PhantomBridge phantom) {
            phantom.bridge$setSpawningEntity(player.getUUID());
        }
        return entity;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    private void arclight$addSpawnReason(ServerLevel level, boolean bl, boolean bl2, CallbackInfoReturnable<Integer> cir) {
        ((ServerLevelBridge)level).bridge$pushAddEntityReason(CreatureSpawnEvent.SpawnReason.NATURAL);
    }
}
