package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.craftbukkit.v.conversations.ConversationTracker;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.ThreadLocalRandom;

/** Fusion Core: Adventure messages, action bar, titles and sounds for players, sent as vanilla components. */
@Mixin(value = CraftPlayer.class, remap = false)
public abstract class CraftPlayerAudienceMixin {

    @Shadow @Final private ConversationTracker conversationTracker;

    @Shadow public abstract ServerPlayer getHandle();

    @SuppressWarnings("deprecation")
    public void sendMessage(Identity source, Component message, MessageType type) {
        ServerPlayer handle = this.getHandle();
        // same guards as CraftPlayer#sendMessage(String): not while a modal conversation runs
        if (handle.connection == null || this.conversationTracker.isConversingModaly()) {
            return;
        }
        handle.sendSystemMessage(FusionCoreAdventure.asVanilla(message, handle.registryAccess()));
    }

    public void sendActionBar(Component message) {
        ServerPlayer handle = this.getHandle();
        if (handle.connection != null) {
            handle.connection.send(new ClientboundSetActionBarTextPacket(FusionCoreAdventure.asVanilla(message, handle.registryAccess())));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void sendTitlePart(TitlePart<T> part, T value) {
        ServerPlayer handle = this.getHandle();
        if (handle.connection == null) {
            return;
        }
        if (part == TitlePart.TITLE) {
            handle.connection.send(new ClientboundSetTitleTextPacket(FusionCoreAdventure.asVanilla((Component) value, handle.registryAccess())));
        } else if (part == TitlePart.SUBTITLE) {
            handle.connection.send(new ClientboundSetSubtitleTextPacket(FusionCoreAdventure.asVanilla((Component) value, handle.registryAccess())));
        } else if (part == TitlePart.TIMES) {
            Title.Times times = (Title.Times) value;
            handle.connection.send(new ClientboundSetTitlesAnimationPacket(ticks(times.fadeIn()), ticks(times.stay()), ticks(times.fadeOut())));
        }
    }

    public void clearTitle() {
        ServerPlayer handle = this.getHandle();
        if (handle.connection != null) {
            handle.connection.send(new ClientboundClearTitlesPacket(false));
        }
    }

    public void playSound(Sound sound) {
        CraftPlayer self = (CraftPlayer) (Object) this;
        Location at = self.getLocation();
        this.playSound(sound, at.getX(), at.getY(), at.getZ());
    }

    public void playSound(Sound sound, double x, double y, double z) {
        CraftPlayer self = (CraftPlayer) (Object) this;
        self.playSound(new Location(self.getWorld(), x, y, z), sound.name().asString(), category(sound.source()), sound.volume(), sound.pitch(), seed(sound));
    }

    public void playSound(Sound sound, Sound.Emitter emitter) {
        CraftPlayer self = (CraftPlayer) (Object) this;
        if (emitter == Sound.Emitter.self()) {
            self.playSound(self, sound.name().asString(), category(sound.source()), sound.volume(), sound.pitch(), seed(sound));
        } else if (emitter instanceof org.bukkit.entity.Entity entity) {
            self.playSound(entity, sound.name().asString(), category(sound.source()), sound.volume(), sound.pitch(), seed(sound));
        }
        // other emitters have no Bukkit counterpart; Paper throws for them, we stay silent
    }

    public void stopSound(SoundStop stop) {
        CraftPlayer self = (CraftPlayer) (Object) this;
        SoundCategory category = stop.source() == null ? null : category(stop.source());
        if (stop.sound() != null) {
            self.stopSound(stop.sound().asString(), category);
        } else if (category != null) {
            self.stopSound(category);
        } else {
            self.stopAllSounds();
        }
    }

    private static int ticks(java.time.Duration duration) {
        return (int) (duration.toMillis() / 50L);
    }

    private static long seed(Sound sound) {
        return sound.seed().orElseGet(() -> ThreadLocalRandom.current().nextLong());
    }

    private static SoundCategory category(Sound.Source source) {
        return switch (source) {
            case MASTER -> SoundCategory.MASTER;
            case MUSIC -> SoundCategory.MUSIC;
            case RECORD -> SoundCategory.RECORDS;
            case WEATHER -> SoundCategory.WEATHER;
            case BLOCK -> SoundCategory.BLOCKS;
            case HOSTILE -> SoundCategory.HOSTILE;
            case NEUTRAL -> SoundCategory.NEUTRAL;
            case PLAYER -> SoundCategory.PLAYERS;
            case AMBIENT -> SoundCategory.AMBIENT;
            case VOICE -> SoundCategory.VOICE;
        };
    }

    // Fusion Core: the rest of Paper's Audience on players (Paper patch 0010); Adventure's defaults for these do nothing

    public void showBossBar(net.kyori.adventure.bossbar.BossBar bar) {
        io.izzel.arclight.common.mod.util.FusionCoreAudiences.showBossBar((org.bukkit.entity.Player) (Object) this, bar);
    }

    public void hideBossBar(net.kyori.adventure.bossbar.BossBar bar) {
        io.izzel.arclight.common.mod.util.FusionCoreAudiences.hideBossBar((org.bukkit.entity.Player) (Object) this, bar);
    }

    public Iterable<? extends net.kyori.adventure.bossbar.BossBar> activeBossBars() {
        return io.izzel.arclight.common.mod.util.FusionCoreAudiences.activeBossBars((org.bukkit.entity.Player) (Object) this);
    }

    public void sendResourcePacks(net.kyori.adventure.resource.ResourcePackRequest request) {
        io.izzel.arclight.common.mod.util.FusionCoreAudiences.sendResourcePacks((org.bukkit.entity.Player) (Object) this, request);
    }

    public void removeResourcePacks(java.util.UUID id, java.util.UUID... others) {
        io.izzel.arclight.common.mod.util.FusionCoreAudiences.removeResourcePacks((org.bukkit.entity.Player) (Object) this, id, others);
    }

    public void clearResourcePacks() {
        io.izzel.arclight.common.mod.util.FusionCoreAudiences.clearResourcePacks((org.bukkit.entity.Player) (Object) this);
    }

    public void openBook(net.kyori.adventure.inventory.Book book) {
        io.izzel.arclight.common.mod.util.FusionCoreAudiences.openBook((org.bukkit.entity.Player) (Object) this, book);
    }

    public void sendMessage(Component message, net.kyori.adventure.chat.ChatType.Bound boundChatType) {
        io.izzel.arclight.common.mod.util.FusionCoreAudiences.sendMessage((org.bukkit.entity.Player) (Object) this, message, boundChatType);
    }

    public void deleteMessage(net.kyori.adventure.chat.SignedMessage.Signature signature) {
        io.izzel.arclight.common.mod.util.FusionCoreAudiences.deleteMessage((org.bukkit.entity.Player) (Object) this, signature);
    }

    // Paper keeps header and footer apart: setting one leaves the other
    public void sendPlayerListHeader(Component header) {
        io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge tab = (io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge) this;
        tab.fusioncore$setPlayerListHeaderFooter(io.izzel.arclight.common.mod.util.FusionCoreAdventure.asVanilla(header), tab.fusioncore$getPlayerListFooter());
    }

    public void sendPlayerListFooter(Component footer) {
        io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge tab = (io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge) this;
        tab.fusioncore$setPlayerListHeaderFooter(tab.fusioncore$getPlayerListHeader(), io.izzel.arclight.common.mod.util.FusionCoreAdventure.asVanilla(footer));
    }

    public void sendPlayerListHeaderAndFooter(Component header, Component footer) {
        ((io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge) this).fusioncore$setPlayerListHeaderFooter(
            io.izzel.arclight.common.mod.util.FusionCoreAdventure.asVanilla(header), io.izzel.arclight.common.mod.util.FusionCoreAdventure.asVanilla(footer));
    }

    public net.kyori.adventure.pointer.Pointers pointers() {
        return io.izzel.arclight.common.mod.util.FusionCoreAudiences.entityPointers((org.bukkit.entity.Player) (Object) this);
    }
}
