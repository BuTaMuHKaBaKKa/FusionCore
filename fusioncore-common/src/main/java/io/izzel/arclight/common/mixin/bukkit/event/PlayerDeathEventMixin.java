package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.kyori.adventure.text.Component;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fusion Core: Paper's component death message (Paper patch 0006 "Adventure"). Stored as the legacy string Spigot uses, so colours
 * and RGB survive and click/hover do not.
 */
@Mixin(value = PlayerDeathEvent.class, remap = false)
public abstract class PlayerDeathEventMixin extends EntityDeathEventMixin {

    @Unique private java.util.List<org.bukkit.inventory.ItemStack> fusioncore$itemsToKeep = new java.util.ArrayList<>();
    @Unique private boolean fusioncore$doExpDrop = true;

    @Shadow public abstract String getDeathMessage();

    @Shadow public abstract void setDeathMessage(String value);

    @Shadow public abstract org.bukkit.entity.Player getEntity();

    /** Fusion Core: Paper's {@code PlayerDeathEvent#getPlayer}, the dying player, as {@code getEntity()}. */
    public org.bukkit.entity.Player getPlayer() {
        return this.getEntity();
    }


    /** Fusion Core: Paper's items the player keeps on death (patch 0168); applied by {@code EntityEventHandler}. */
    public java.util.List<org.bukkit.inventory.ItemStack> getItemsToKeep() {
        return this.fusioncore$itemsToKeep;
    }

    /** Fusion Core: Paper's shouldDropExperience (patch 0176). */
    public boolean shouldDropExperience() {
        return this.fusioncore$doExpDrop;
    }

    public void setShouldDropExperience(boolean doExpDrop) {
        this.fusioncore$doExpDrop = doExpDrop;
    }

    @Override
    public boolean bridge$shouldDropExperience() {
        return this.fusioncore$doExpDrop;
    }

    @Override
    public void bridge$setShouldDropExperience(boolean doExpDrop) {
        this.fusioncore$doExpDrop = doExpDrop;
    }

    @Override
    public java.util.List<org.bukkit.inventory.ItemStack> bridge$getItemsToKeep() {
        return this.fusioncore$itemsToKeep;
    }

    public Component deathMessage() {
        String legacy = this.getDeathMessage();
        return legacy == null ? null : FusionCoreAdventure.fromLegacy(legacy);
    }

    public void deathMessage(Component deathMessage) {
        this.setDeathMessage(deathMessage == null ? null : FusionCoreAdventure.toLegacyRgb(deathMessage));
    }
}
