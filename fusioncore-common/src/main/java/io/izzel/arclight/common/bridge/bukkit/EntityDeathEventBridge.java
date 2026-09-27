package io.izzel.arclight.common.bridge.bukkit;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import java.util.List;

/** Fusion Core: Paper's death event state for core code, which does not see what {@code EntityDeathEventMixin} adds. */
public interface EntityDeathEventBridge {

    boolean bridge$isCancelled();

    double bridge$getReviveHealth();

    /** Sets the revive health without Paper's range check: the core puts the entity's max health here before the call. */
    void bridge$initReviveHealth(double health);

    /**
     * Fills the death sound before the call. {@code vanilla} is the entity's own {@code SoundEvent}, played as is while the plugin leaves the
     * event's sound alone: a mod's sound may have no Bukkit {@code Sound}, then {@code asBukkit} is null.
     */
    void bridge$initDeathSound(boolean play, Object vanilla, Sound asBukkit, SoundCategory category, float volume, float pitch);

    boolean bridge$shouldPlayDeathSound();

    Object bridge$getVanillaDeathSound();

    Sound bridge$getBukkitVanillaDeathSound();

    Sound bridge$getDeathSound();

    SoundCategory bridge$getDeathSoundCategory();

    float bridge$getDeathSoundVolume();

    float bridge$getDeathSoundPitch();

    boolean bridge$shouldDropExperience();

    void bridge$setShouldDropExperience(boolean doExpDrop);

    List<org.bukkit.inventory.ItemStack> bridge$getItemsToKeep();
}
