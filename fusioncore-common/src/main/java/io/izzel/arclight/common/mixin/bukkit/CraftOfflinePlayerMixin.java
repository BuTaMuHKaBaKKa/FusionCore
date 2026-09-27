package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v.CraftOfflinePlayer;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

/** Fusion Core: Paper's OfflinePlayer API (last login/seen, persistent data view) read from the player file. */
@Mixin(value = CraftOfflinePlayer.class, remap = false)
public abstract class CraftOfflinePlayerMixin {

    @Shadow public abstract Player getPlayer();

    @Shadow private CompoundTag getData() { return null; }

    @Shadow private File getDataFile() { return null; }

    private long fusioncore$paperTime(String key) {
        CompoundTag data = this.getData();
        if (data == null) {
            return 0;
        }
        CompoundTag paper = data.getCompound("Paper");
        return paper.contains(key) ? paper.getLong(key) : this.getDataFile().lastModified();
    }

    public long getLastLogin() {
        Player player = this.getPlayer();
        return player != null ? io.izzel.arclight.common.mod.util.FusionCorePlayers.state(player).fusioncore$loginTime() : this.fusioncore$paperTime("LastLogin");
    }

    public long getLastSeen() {
        Player player = this.getPlayer();
        return player != null ? System.currentTimeMillis() : this.fusioncore$paperTime("LastSeen");
    }

    public io.papermc.paper.persistence.PersistentDataContainerView getPersistentDataContainer() {
        Player player = this.getPlayer();
        if (player != null) {
            return (io.papermc.paper.persistence.PersistentDataContainerView) (Object) player.getPersistentDataContainer();
        }
        org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer container =
            new org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer(new org.bukkit.craftbukkit.v.persistence.CraftPersistentDataTypeRegistry());
        CompoundTag data = this.getData();
        if (data != null && data.contains("BukkitValues")) {
            container.putAll(data.getCompound("BukkitValues"));
        }
        return (io.papermc.paper.persistence.PersistentDataContainerView) (Object) container;
    }
}
