package io.izzel.arclight.common.mixin.bukkit;

import com.destroystokyo.paper.Title;
import io.izzel.arclight.common.mod.util.FusionCoreTitles;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's deprecated BungeeCord title and tab list API (patch 0024), bodies in {@link FusionCoreTitles}. */
@Mixin(value = Player.class, remap = false)
public interface PlayerMixin extends net.kyori.adventure.identity.Identified, net.kyori.adventure.bossbar.BossBarViewer {

    default void setPlayerListHeaderFooter(BaseComponent[] header, BaseComponent[] footer) {
        FusionCoreTitles.setPlayerListHeaderFooter((Player) this, header, footer);
    }

    default void setPlayerListHeaderFooter(BaseComponent header, BaseComponent footer) {
        FusionCoreTitles.setPlayerListHeaderFooter((Player) this,
            header == null ? null : new BaseComponent[]{header}, footer == null ? null : new BaseComponent[]{footer});
    }

    default void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        FusionCoreTitles.setTitleTimes((Player) this, fadeInTicks, stayTicks, fadeOutTicks);
    }

    default void setSubtitle(BaseComponent[] subtitle) {
        FusionCoreTitles.setSubtitle((Player) this, subtitle);
    }

    default void setSubtitle(BaseComponent subtitle) {
        FusionCoreTitles.setSubtitle((Player) this, new BaseComponent[]{subtitle});
    }

    default void showTitle(BaseComponent[] title) {
        FusionCoreTitles.showTitle((Player) this, title);
    }

    default void showTitle(BaseComponent title) {
        FusionCoreTitles.showTitle((Player) this, new BaseComponent[]{title});
    }

    default void showTitle(BaseComponent[] title, BaseComponent[] subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        FusionCoreTitles.showTitle((Player) this, title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
    }

    default void showTitle(BaseComponent title, BaseComponent subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        FusionCoreTitles.showTitle((Player) this, new BaseComponent[]{title}, new BaseComponent[]{subtitle}, fadeInTicks, stayTicks, fadeOutTicks);
    }

    default void sendTitle(Title title) {
        FusionCoreTitles.send((Player) this, title);
    }

    default void updateTitle(Title title) {
        FusionCoreTitles.update((Player) this, title);
    }

    default void hideTitle() {
        FusionCoreTitles.hideTitle((Player) this);
    }

    // Fusion Core: Paper's Adventure API on players (Paper patch 0006/0010 "Adventure"). Display and list
    // names stay stored the Spigot way (legacy text / the list-name component built from it), so colours
    // and RGB survive while click and hover events in them do not; everything else is Paper's.

    @Override
    default net.kyori.adventure.identity.Identity identity() {
        return net.kyori.adventure.identity.Identity.identity(((Player) this).getUniqueId());
    }

    default net.kyori.adventure.text.Component displayName() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(((Player) this).getDisplayName());
    }

    default void displayName(net.kyori.adventure.text.Component displayName) {
        ((Player) this).setDisplayName(displayName == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(displayName));
    }

    default void playerListName(net.kyori.adventure.text.Component name) {
        ((Player) this).setPlayerListName(name == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(name));
    }

    default net.kyori.adventure.text.Component playerListName() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.fromLegacy(((Player) this).getPlayerListName());
    }

    default net.kyori.adventure.text.Component playerListHeader() {
        net.minecraft.network.chat.Component header = ((io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge) this).fusioncore$getPlayerListHeader();
        return header == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.asAdventure(header);
    }

    default net.kyori.adventure.text.Component playerListFooter() {
        net.minecraft.network.chat.Component footer = ((io.izzel.arclight.common.bridge.bukkit.CraftPlayerTabListBridge) this).fusioncore$getPlayerListFooter();
        return footer == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.asAdventure(footer);
    }

    default void kick() {
        this.kick((net.kyori.adventure.text.Component) null);
    }

    /** Paper: the kick screen shows the component as given (Spigot's kickPlayer goes through legacy text). */
    default void kick(net.kyori.adventure.text.Component message) {
        this.kick(message, org.bukkit.event.player.PlayerKickEvent$Cause.PLUGIN);
    }

    default void kick(net.kyori.adventure.text.Component message, org.bukkit.event.player.PlayerKickEvent$Cause cause) {
        org.spigotmc.AsyncCatcher.catchOp("player kick");
        io.izzel.arclight.common.mod.util.FusionCoreEvents.kickCause(cause);
        var handle = ((org.bukkit.craftbukkit.v.entity.CraftPlayer) (Object) this).getHandle();
        var connection = ((io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge) handle).bridge$getTransferCookieConnection();
        if (connection != null) {
            connection.kickPlayer(io.izzel.arclight.common.mod.util.FusionCoreAdventure.asVanillaNullToEmpty(message));
        }
        io.izzel.arclight.common.mod.util.FusionCoreEvents.clearKickCause();
    }

    /** Paper: the client's language as a {@link java.util.Locale}. */
    default java.util.Locale locale() {
        java.util.Locale locale = net.kyori.adventure.translation.Translator.parseLocale(((Player) this).getLocale());
        return locale == null ? java.util.Locale.US : locale;
    }

    default void sendSignChange(org.bukkit.Location loc, java.util.List<? extends net.kyori.adventure.text.Component> lines) {
        this.sendSignChange(loc, lines, org.bukkit.DyeColor.BLACK);
    }

    default void sendSignChange(org.bukkit.Location loc, java.util.List<? extends net.kyori.adventure.text.Component> lines, org.bukkit.DyeColor dyeColor) {
        this.sendSignChange(loc, lines, dyeColor, false);
    }

    default void sendSignChange(org.bukkit.Location loc, java.util.List<? extends net.kyori.adventure.text.Component> lines, boolean hasGlowingText) {
        this.sendSignChange(loc, lines, org.bukkit.DyeColor.BLACK, hasGlowingText);
    }

    default void sendSignChange(org.bukkit.Location loc, java.util.List<? extends net.kyori.adventure.text.Component> lines, org.bukkit.DyeColor dyeColor, boolean hasGlowingText) {
        String[] legacy = null;
        if (lines != null) {
            legacy = new String[lines.size()];
            for (int i = 0; i < legacy.length; i++) {
                net.kyori.adventure.text.Component line = lines.get(i);
                legacy[i] = line == null ? "" : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(line);
            }
        }
        ((Player) this).sendSignChange(loc, legacy, dyeColor, hasGlowingText);
    }

    default void setResourcePack(String url, byte[] hash, net.kyori.adventure.text.Component prompt) {
        this.setResourcePack(url, hash, prompt, false);
    }

    default void setResourcePack(String url, byte[] hash, net.kyori.adventure.text.Component prompt, boolean force) {
        this.setResourcePack(java.util.UUID.nameUUIDFromBytes(url.getBytes(java.nio.charset.StandardCharsets.UTF_8)), url, hash, prompt, force);
    }

    default void setResourcePack(java.util.UUID uuid, String url, byte[] hash, net.kyori.adventure.text.Component prompt, boolean force) {
        ((Player) this).setResourcePack(uuid, url, hash, prompt == null ? null : io.izzel.arclight.common.mod.util.FusionCoreAdventure.toLegacyRgb(prompt), force);
    }

    /** Paper: a player's hover shows its display name. */
    default net.kyori.adventure.text.event.HoverEvent<net.kyori.adventure.text.event.HoverEvent.ShowEntity> asHoverEvent(
        java.util.function.UnaryOperator<net.kyori.adventure.text.event.HoverEvent.ShowEntity> op) {
        Player self = (Player) this;
        return net.kyori.adventure.text.event.HoverEvent.showEntity(op.apply(net.kyori.adventure.text.event.HoverEvent.ShowEntity.showEntity(
            net.kyori.adventure.key.Key.key(self.getType().getKey().toString()), self.getUniqueId(), this.displayName())));
    }

    /** Paper's {@code Player#setPlayerProfile} (Paper patch 0176), see {@code FusionCoreProfiles}. */
    default void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile profile) {
        io.izzel.arclight.common.mod.util.FusionCoreProfiles.setPlayerProfile((org.bukkit.entity.Player) this, profile);
    }

    // Fusion Core: Paper's lookAt (Paper patch 0721 "More Teleport API"); the vanilla player sends the look-at packet

    default void lookAt(double x, double y, double z, io.papermc.paper.entity.LookAnchor playerAnchor) {
        ((org.bukkit.craftbukkit.v.entity.CraftPlayer) (Object) this).getHandle().lookAt(
            io.izzel.arclight.common.mod.util.FusionCoreTeleport.toNms(playerAnchor), new net.minecraft.world.phys.Vec3(x, y, z));
    }

    default void lookAt(io.papermc.paper.math.Position position, io.papermc.paper.entity.LookAnchor playerAnchor) {
        this.lookAt(position.x(), position.y(), position.z(), playerAnchor);
    }

    default void lookAt(org.bukkit.entity.Entity entity, io.papermc.paper.entity.LookAnchor playerAnchor, io.papermc.paper.entity.LookAnchor entityAnchor) {
        ((org.bukkit.craftbukkit.v.entity.CraftPlayer) (Object) this).getHandle().lookAt(io.izzel.arclight.common.mod.util.FusionCoreTeleport.toNms(playerAnchor),
            ((org.bukkit.craftbukkit.v.entity.CraftEntity) entity).getHandle(), io.izzel.arclight.common.mod.util.FusionCoreTeleport.toNms(entityAnchor));
    }

    // Fusion Core: Paper's smaller Player API; bodies in FusionCorePlayers, each names its Paper patch

    default org.bukkit.entity.Firework boostElytra(org.bukkit.inventory.ItemStack firework) {
        com.google.common.base.Preconditions.checkState(((Player) this).isGliding(), "Player must be gliding");
        return ((Player) this).fireworkBoost(firework);
    }

    default void addAdditionalChatCompletions(java.util.Collection<String> completions) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.chatCompletions((Player) this, completions, true);
    }

    default void removeAdditionalChatCompletions(java.util.Collection<String> completions) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.chatCompletions((Player) this, completions, false);
    }

    default int applyMending(int amount) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.applyMending((Player) this, amount);
    }

    default void giveExp(int amount, boolean applyMending) {
        ((Player) this).giveExp(applyMending ? io.izzel.arclight.common.mod.util.FusionCorePlayers.applyMending((Player) this, amount) : amount);
    }

    default int calculateTotalExperiencePoints() {
        Player self = (Player) this;
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.totalExperiencePoints(self.getLevel()) + Math.round(this.getExperiencePointsNeededForNextLevel() * self.getExp());
    }

    default void setExperienceLevelAndProgress(int totalExperience) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.setExperienceLevelAndProgress((Player) this, totalExperience);
    }

    default int getExperiencePointsNeededForNextLevel() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.handle(this).getXpNeededForNextLevel();
    }

    default float getCooldownPeriod() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.handle(this).getCurrentItemAttackStrengthDelay();
    }

    default float getCooledAttackStrength(float adjustTicks) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.handle(this).getAttackStrengthScale(adjustTicks);
    }

    default void resetCooldown() {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.handle(this).resetAttackStrengthTicker();
    }

    default <T> T getClientOption(com.destroystokyo.paper.ClientOption<T> option) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.clientOption((Player) this, option);
    }

    default java.time.Duration getIdleDuration() {
        return java.time.Duration.ofMillis(net.minecraft.Util.getMillis() - io.izzel.arclight.common.mod.util.FusionCorePlayers.handle(this).getLastActionTime());
    }

    default void resetIdleDuration() {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.handle(this).resetLastActionTime();
    }

    default boolean isConnected() {
        return !io.izzel.arclight.common.mod.util.FusionCorePlayers.handle(this).hasDisconnected();
    }

    default void sendOpLevel(byte level) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.sendOpLevel((Player) this, level);
    }

    default void sendMultiBlockChange(java.util.Map<? extends io.papermc.paper.math.Position, org.bukkit.block.data.BlockData> blockChanges) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.sendMultiBlockChange((Player) this, blockChanges);
    }

    default void sendMultiBlockChange(java.util.Map<? extends io.papermc.paper.math.Position, org.bukkit.block.data.BlockData> blockChanges, boolean suppressLightUpdates) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.sendMultiBlockChange((Player) this, blockChanges);
    }

    default void sendEntityEffect(org.bukkit.EntityEffect effect, org.bukkit.entity.Entity target) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.sendEntityEffect((Player) this, effect, target);
    }

    default void showElderGuardian() {
        this.showElderGuardian(false);
    }

    default void showElderGuardian(boolean silent) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.gameEvent((Player) this, net.minecraft.network.protocol.game.ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT, silent ? 0F : 1F);
    }

    default void showWinScreen() {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.gameEvent((Player) this, net.minecraft.network.protocol.game.ClientboundGameEventPacket.WIN_GAME, 1);
    }

    default boolean hasSeenWinScreen() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.handle(this).seenCredits;
    }

    default void setHasSeenWinScreen(boolean hasSeenWinScreen) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.handle(this).seenCredits = hasSeenWinScreen;
    }

    default int getWardenWarningCooldown() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.wardenAccess((Player) this).fusioncore$getCooldownTicks();
    }

    default void setWardenWarningCooldown(int cooldown) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.wardenAccess((Player) this).fusioncore$setCooldownTicks(Math.max(cooldown, 0));
    }

    default int getWardenTimeSinceLastWarning() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.wardenAccess((Player) this).fusioncore$getTicksSinceLastWarning();
    }

    default void setWardenTimeSinceLastWarning(int time) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.wardenAccess((Player) this).fusioncore$setTicksSinceLastWarning(time);
    }

    default int getWardenWarningLevel() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.warden((Player) this).getWarningLevel();
    }

    default void setWardenWarningLevel(int warningLevel) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.warden((Player) this).setWarningLevel(warningLevel);
    }

    default void increaseWardenWarningLevel() {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.wardenAccess((Player) this).fusioncore$increaseWarningLevel();
    }

    // listing API: the state is in CraftPlayerMixin, these only serve other Player implementations
    default boolean isListed(Player other) {
        return true;
    }

    default boolean listPlayer(Player other) {
        return false;
    }

    default boolean unlistPlayer(Player other) {
        return false;
    }

    // Paper patch 0119: string and BungeeCord action bars
    @Deprecated
    default void sendActionBar(String message) {
        if (message == null || message.isEmpty()) return;
        io.izzel.arclight.common.mod.util.FusionCorePlayers.sendActionBar((Player) this, org.bukkit.craftbukkit.v.util.CraftChatMessage.fromStringOrNull(message));
    }

    @Deprecated
    default void sendActionBar(char alternateChar, String message) {
        if (message == null || message.isEmpty()) return;
        this.sendActionBar(org.bukkit.ChatColor.translateAlternateColorCodes(alternateChar, message));
    }

    @Deprecated
    default void sendActionBar(net.md_5.bungee.api.chat.BaseComponent... message) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.sendActionBar((Player) this, org.bukkit.craftbukkit.v.util.CraftChatMessage.fromJSON(net.md_5.bungee.chat.ComponentSerializer.toString(message)));
    }

    default void sendMessage(net.md_5.bungee.api.ChatMessageType position, net.md_5.bungee.api.chat.BaseComponent... components) {
        ((Player) this).spigot().sendMessage(position, components);
    }

    // Paper's resource pack overloads with the hash as a hex string
    default void setResourcePack(String url, String hash) {
        this.setResourcePack(url, hash, false);
    }

    default void setResourcePack(String url, String hash, boolean required) {
        this.setResourcePack(url, hash, required, null);
    }

    default void setResourcePack(String url, String hash, boolean required, net.kyori.adventure.text.Component resourcePackPrompt) {
        this.setResourcePack(java.util.UUID.nameUUIDFromBytes(url.getBytes(java.nio.charset.StandardCharsets.UTF_8)), url, hash, resourcePackPrompt, required);
    }

    default void setResourcePack(java.util.UUID uuid, String url, String hash, net.kyori.adventure.text.Component resourcePackPrompt, boolean required) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.setResourcePack((Player) this, uuid, url, hash, resourcePackPrompt, required);
    }

    @Deprecated
    default String getResourcePackHash() {
        return null;
    }

    // Paper's deprecated ban helpers
    default org.bukkit.BanEntry banPlayerFull(String reason) {
        return this.banPlayerFull(reason, null, null);
    }

    default org.bukkit.BanEntry banPlayerFull(String reason, String source) {
        return this.banPlayerFull(reason, null, source);
    }

    default org.bukkit.BanEntry banPlayerFull(String reason, java.util.Date expires) {
        return this.banPlayerFull(reason, expires, null);
    }

    default org.bukkit.BanEntry banPlayerFull(String reason, java.util.Date expires, String source) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.banName((Player) this, reason, expires, source, true);
        return this.banPlayerIP(reason, expires, source, true);
    }

    default org.bukkit.BanEntry banPlayerIP(String reason) {
        return this.banPlayerIP(reason, null, null);
    }

    default org.bukkit.BanEntry banPlayerIP(String reason, boolean kickPlayer) {
        return this.banPlayerIP(reason, null, null, kickPlayer);
    }

    default org.bukkit.BanEntry banPlayerIP(String reason, String source) {
        return this.banPlayerIP(reason, null, source);
    }

    default org.bukkit.BanEntry banPlayerIP(String reason, String source, boolean kickPlayer) {
        return this.banPlayerIP(reason, null, source, kickPlayer);
    }

    default org.bukkit.BanEntry banPlayerIP(String reason, java.util.Date expires) {
        return this.banPlayerIP(reason, expires, null);
    }

    default org.bukkit.BanEntry banPlayerIP(String reason, java.util.Date expires, boolean kickPlayer) {
        return this.banPlayerIP(reason, expires, null, kickPlayer);
    }

    default org.bukkit.BanEntry banPlayerIP(String reason, java.util.Date expires, String source) {
        return this.banPlayerIP(reason, expires, source, true);
    }

    default org.bukkit.BanEntry banPlayerIP(String reason, java.util.Date expires, String source, boolean kickPlayer) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.banIp((Player) this, reason, expires, source, kickPlayer);
    }

    // Fusion Core: Paper's Player API that needs core hooks; state in CraftPlayerMixin, hooks named in FusionCorePlayers

    default boolean getAffectsSpawning() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this).fusioncore$affectsSpawning();
    }

    default void setAffectsSpawning(boolean affects) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this).fusioncore$setAffectsSpawning(affects);
    }

    default net.kyori.adventure.util.TriState hasFlyingFallDamage() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this).fusioncore$flyingFallDamage();
    }

    default void setFlyingFallDamage(net.kyori.adventure.util.TriState flyingFallDamage) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this).fusioncore$setFlyingFallDamage(java.util.Objects.requireNonNull(flyingFallDamage, "flyingFallDamage"));
    }

    default String getClientBrandName() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this).fusioncore$clientBrand();
    }

    default long getLastLogin() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this).fusioncore$loginTime();
    }

    default long getLastSeen() {
        return ((Player) this).isOnline() ? System.currentTimeMillis() : io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this).fusioncore$lastSaveTime();
    }

    default io.papermc.paper.persistence.PersistentDataContainerView getPersistentDataContainer() {
        return (io.papermc.paper.persistence.PersistentDataContainerView) (Object) ((org.bukkit.entity.Entity) this).getPersistentDataContainer();
    }

    default org.bukkit.event.player.PlayerResourcePackStatusEvent.Status getResourcePackStatus() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.state(this).fusioncore$resourcePackStatus();
    }

    default boolean hasResourcePack() {
        return this.getResourcePackStatus() == org.bukkit.event.player.PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED;
    }

    /** Fusion Core: the player's own view distance, capped by the world's (vanilla loads chunks only that far). */
    default int getViewDistance() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.sendViewDistance((Player) this);
    }

    default void setViewDistance(int viewDistance) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.setViewDistance((Player) this, viewDistance);
    }

    default int getSendViewDistance() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.sendViewDistance((Player) this);
    }

    default void setSendViewDistance(int viewDistance) {
        io.izzel.arclight.common.mod.util.FusionCorePlayers.setViewDistance((Player) this, viewDistance);
    }

    default int getNoTickViewDistance() {
        return this.getViewDistance();
    }

    default void setNoTickViewDistance(int viewDistance) {
        this.setViewDistance(viewDistance);
    }

    default int getSimulationDistance() {
        return ((Player) this).getWorld().getSimulationDistance();
    }

    /** Fusion Core: vanilla ticks chunks by one distance for everybody; a per-player simulation distance is not supported. */
    default void setSimulationDistance(int simulationDistance) {
        throw new UnsupportedOperationException("Fusion Core does not support a per-player simulation distance; use World#setSimulationDistance");
    }

    default java.util.Set<Long> getSentChunkKeys() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.sentChunkKeys((Player) this);
    }

    default java.util.Set<org.bukkit.Chunk> getSentChunks() {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.sentChunks((Player) this);
    }

    default boolean isChunkSent(long chunkKey) {
        return io.izzel.arclight.common.mod.util.FusionCorePlayers.isChunkSent((Player) this, chunkKey);
    }

    default boolean isChunkSent(org.bukkit.Chunk chunk) {
        return chunk.getWorld().equals(((Player) this).getWorld()) && io.izzel.arclight.common.mod.util.FusionCorePlayers.isChunkSent((Player) this, net.minecraft.world.level.ChunkPos.asLong(chunk.getX(), chunk.getZ()));
    }

    /** Fusion Core has no PROXY protocol support, so there is never a proxied address. */
    default java.net.InetSocketAddress getHAProxyAddress() {
        return null;
    }

    /** Paper: the Adventure boss bars shown to the player; CraftPlayerAudienceMixin answers for the server's players. */
    default Iterable<? extends net.kyori.adventure.bossbar.BossBar> activeBossBars() {
        return java.util.Set.of();
    }
}
