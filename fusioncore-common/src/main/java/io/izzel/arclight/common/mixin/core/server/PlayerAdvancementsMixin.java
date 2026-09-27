package io.izzel.arclight.common.mixin.core.server;

import io.izzel.arclight.common.bridge.bukkit.PlayerAdvancementDoneEventBridge;
import io.izzel.arclight.common.bridge.core.advancements.AdvancementHolderBridge;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {

    @Shadow private ServerPlayer player;

    // Fusion Core: the announcement left by PlayerAdvancementDoneEvent listeners (Paper's message()).
    @Unique private net.kyori.adventure.text.Component fusioncore$announcement;

    @Inject(method = "award",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V"))
    public void arclight$callEvent(AdvancementHolder advancementHolder, String criterionKey, CallbackInfoReturnable<Boolean> cir) {
        // Fusion Core: as in Paper, the event carries the vanilla announcement and may replace or drop it.
        net.kyori.adventure.text.Component announcement = advancementHolder.value().display()
            .filter(DisplayInfo::shouldAnnounceChat)
            .map(info -> FusionCoreAdventure.asAdventure(info.getType().createAnnouncement(advancementHolder, this.player), this.player.registryAccess()))
            .orElse(null);
        var event = new PlayerAdvancementDoneEvent((Player) this.player.bridge$getBukkitEntity(), ((AdvancementHolderBridge) (Object) advancementHolder).bridge$getBukkit());
        ((PlayerAdvancementDoneEventBridge) event).fusioncore$setMessage(announcement);
        Bukkit.getPluginManager().callEvent(event);
        this.fusioncore$announcement = ((PlayerAdvancementDoneEventBridge) event).fusioncore$message();
    }

    // Fusion Core: the vanilla announcement lambda in award (display().ifPresent(...)), matched by
    // descriptor because lambda names differ between dev and runtime mappings. As in Paper, it
    // announces when the event left a message, whatever the display says, and sends that message.
    @Redirect(method = "*(Lnet/minecraft/advancements/AdvancementHolder;Lnet/minecraft/advancements/DisplayInfo;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/DisplayInfo;shouldAnnounceChat()Z"))
    private boolean fusioncore$announce(DisplayInfo info) {
        return this.fusioncore$announcement != null;
    }

    @Redirect(method = "*(Lnet/minecraft/advancements/AdvancementHolder;Lnet/minecraft/advancements/DisplayInfo;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void fusioncore$broadcast(PlayerList playerList, Component vanilla, boolean overlay) {
        net.kyori.adventure.text.Component announcement = this.fusioncore$announcement;
        this.fusioncore$announcement = null;
        if (announcement != null) {
            playerList.broadcastSystemMessage(FusionCoreAdventure.asVanilla(announcement, this.player.registryAccess()), overlay);
        }
    }

    // Fusion Core: Paper's PlayerAdvancementCriterionGrantEvent (Paper patch 0168): a cancelled grant is taken back
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementProgress;grantProgress(Ljava/lang/String;)Z"))
    private boolean fusioncore$criterionGrantEvent(net.minecraft.advancements.AdvancementProgress progress, String criterion, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original,
                                                 @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) AdvancementHolder advancement) {
        if (!original.call(progress, criterion)) {
            return false;
        }
        if (!io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents.criterionGrant(this.player, advancement, criterion)) {
            progress.revokeProgress(criterion);
            return false;
        }
        return true;

    }
}
