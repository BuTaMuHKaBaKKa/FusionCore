package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.bridge.bukkit.EntityDeathEventBridge;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/** Fusion Core: Paper's cancellable death event with revive health and death sound (API patch 0142). */
@Mixin(value = EntityDeathEvent.class, remap = false)
public abstract class EntityDeathEventMixin implements Cancellable, EntityDeathEventBridge {

    @Shadow public abstract LivingEntity getEntity();

    @Unique private boolean fusioncore$cancelled;
    @Unique private double fusioncore$reviveHealth;
    @Unique private boolean fusioncore$shouldPlayDeathSound;
    @Unique private Sound fusioncore$deathSound;
    @Unique private SoundCategory fusioncore$deathSoundCategory;
    @Unique private float fusioncore$deathSoundVolume;
    @Unique private float fusioncore$deathSoundPitch;
    @Unique private Object fusioncore$vanillaDeathSound;
    @Unique private Sound fusioncore$bukkitVanillaDeathSound;

    @Override
    public boolean isCancelled() {
        return this.fusioncore$cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.fusioncore$cancelled = cancel;
    }

    public double getReviveHealth() {
        return this.fusioncore$reviveHealth;
    }

    public void setReviveHealth(double reviveHealth) throws IllegalArgumentException {
        double maxHealth = this.getEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        if ((maxHealth != 0 && reviveHealth <= 0) || (reviveHealth > maxHealth)) {
            throw new IllegalArgumentException("Health must be between 0 (exclusive) and " + maxHealth + " (inclusive), but was " + reviveHealth);
        }
        this.fusioncore$reviveHealth = reviveHealth;
    }

    public boolean shouldPlayDeathSound() {
        return this.fusioncore$shouldPlayDeathSound;
    }

    public void setShouldPlayDeathSound(boolean playDeathSound) {
        this.fusioncore$shouldPlayDeathSound = playDeathSound;
    }

    public Sound getDeathSound() {
        return this.fusioncore$deathSound;
    }

    public void setDeathSound(Sound sound) {
        this.fusioncore$deathSound = sound;
    }

    public SoundCategory getDeathSoundCategory() {
        return this.fusioncore$deathSoundCategory;
    }

    public void setDeathSoundCategory(SoundCategory soundCategory) {
        this.fusioncore$deathSoundCategory = soundCategory;
    }

    public float getDeathSoundVolume() {
        return this.fusioncore$deathSoundVolume;
    }

    public void setDeathSoundVolume(float volume) {
        this.fusioncore$deathSoundVolume = volume;
    }

    public float getDeathSoundPitch() {
        return this.fusioncore$deathSoundPitch;
    }

    public void setDeathSoundPitch(float pitch) {
        this.fusioncore$deathSoundPitch = pitch;
    }

    @Override
    public boolean bridge$isCancelled() {
        return this.isCancelled();
    }

    @Override
    public double bridge$getReviveHealth() {
        return this.fusioncore$reviveHealth;
    }

    @Override
    public void bridge$initReviveHealth(double health) {
        this.fusioncore$reviveHealth = health;
    }

    @Override
    public Object bridge$getVanillaDeathSound() {
        return this.fusioncore$vanillaDeathSound;
    }

    @Override
    public void bridge$initDeathSound(boolean play, Object vanilla, Sound asBukkit, SoundCategory category, float volume, float pitch) {
        this.fusioncore$shouldPlayDeathSound = play;
        this.fusioncore$vanillaDeathSound = vanilla;
        this.fusioncore$bukkitVanillaDeathSound = asBukkit;
        this.fusioncore$deathSound = asBukkit;
        this.fusioncore$deathSoundCategory = category;
        this.fusioncore$deathSoundVolume = volume;
        this.fusioncore$deathSoundPitch = pitch;
    }

    @Override
    public boolean bridge$shouldPlayDeathSound() {
        return this.fusioncore$shouldPlayDeathSound;
    }

    @Override
    public Sound bridge$getBukkitVanillaDeathSound() {
        return this.fusioncore$bukkitVanillaDeathSound;
    }

    @Override
    public Sound bridge$getDeathSound() {
        return this.fusioncore$deathSound;
    }

    @Override
    public SoundCategory bridge$getDeathSoundCategory() {
        return this.fusioncore$deathSoundCategory;
    }

    @Override
    public float bridge$getDeathSoundVolume() {
        return this.fusioncore$deathSoundVolume;
    }

    @Override
    public float bridge$getDeathSoundPitch() {
        return this.fusioncore$deathSoundPitch;
    }

    @Override
    public boolean bridge$shouldDropExperience() {
        return true;
    }

    @Override
    public void bridge$setShouldDropExperience(boolean doExpDrop) {
    }

    @Override
    public List<org.bukkit.inventory.ItemStack> bridge$getItemsToKeep() {
        return List.of();
    }
}
