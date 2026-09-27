package io.izzel.arclight.common.mixin.core.world.effect;

import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(MobEffectUtil.class)
public class MobEffectUtilMixin {

    @Decorate(method = "addEffectToPlayersAround", inject = true, at = @At(value = "INVOKE", remap = false, target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private static void arclight$pushCause(@Local(ordinal = -1) List<ServerPlayer> players) {
        var cause = ArclightCaptures.getEffectCause();
        if (cause != null) {
            for (ServerPlayer player : players) {
                ((ServerPlayerBridge) player).bridge$pushEffectCause(cause);
            }
        }
    }

    // Fusion Core: Paper's ElderGuardianAppearanceEvent (patch 0550), tested only for players vanilla would affect
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "addEffectToPlayersAround", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerLevel;getPlayers(Ljava/util/function/Predicate;)Ljava/util/List;"))
    private static List<ServerPlayer> fusioncore$elderGuardianAppearance(net.minecraft.server.level.ServerLevel level, java.util.function.Predicate<? super ServerPlayer> test,
                                                                       com.llamalad7.mixinextras.injector.wrapoperation.Operation<List<ServerPlayer>> original,
                                                                       @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) @org.jetbrains.annotations.Nullable net.minecraft.world.entity.Entity source) {
        if (source instanceof net.minecraft.world.entity.monster.ElderGuardian) {
            java.util.function.Predicate<? super ServerPlayer> vanilla = test;
            test = player -> vanilla.test(player) && io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.elderGuardianAppearance(source, player);
        }
        return original.call(level, test);
    }
}
