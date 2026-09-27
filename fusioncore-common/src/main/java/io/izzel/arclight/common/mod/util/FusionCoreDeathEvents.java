package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.bridge.bukkit.EntityDeathEventBridge;
import io.izzel.arclight.common.bridge.core.world.entity.LivingEntityDeathBridge;
import io.izzel.arclight.common.mixin.core.world.entity.LivingEntityPaperAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.v.CraftSound;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Fusion Core: Paper's cancellable death events (patch 0242 "Improve death events"): the fields the core puts into the event before the
 * call and the death sound played from it. The sound is no longer played in {@code LivingEntity#hurt} ({@code LivingEntityDeathMixin}).
 */
public final class FusionCoreDeathEvents {

    private FusionCoreDeathEvents() {
    }

    /** Paper's {@code CraftEventFactory#populateFields}. */
    public static void populate(LivingEntity victim, EntityDeathEvent event) {
        EntityDeathEventBridge bridge = (EntityDeathEventBridge) event;
        bridge.bridge$initReviveHealth(victim.getMaxHealth());
        SoundEvent sound = ((LivingEntityPaperAccessor) victim).fusioncore$getDeathSound();
        org.bukkit.Sound asBukkit = null;
        if (sound != null) {
            try {
                asBukkit = CraftSound.minecraftToBukkit(sound);
            } catch (RuntimeException e) {
                // a sound Bukkit does not know (unregistered mod sound): the event shows none, the entity's own still plays
            }
        }
        bridge.bridge$initDeathSound(!((LivingEntityDeathBridge) victim).fusioncore$isSilentDeath() && !victim.isSilent(), sound, asBukkit,
            org.bukkit.SoundCategory.valueOf(victim.getSoundSource().name()), ((LivingEntityPaperAccessor) victim).fusioncore$getSoundVolume(),
            victim.getVoicePitch());
    }

    /** Paper's {@code CraftEventFactory#playDeathSound}; call only when the event was not cancelled. */
    public static void playDeathSound(LivingEntity victim, EntityDeathEvent event) {
        EntityDeathEventBridge bridge = (EntityDeathEventBridge) event;
        org.bukkit.Sound sound = bridge.bridge$getDeathSound();
        org.bukkit.SoundCategory category = bridge.bridge$getDeathSoundCategory();
        if (!bridge.bridge$shouldPlayDeathSound() || category == null) {
            return;
        }
        SoundEvent vanilla = (SoundEvent) bridge.bridge$getVanillaDeathSound();
        SoundEvent effect;
        if (vanilla != null && sound == bridge.bridge$getBukkitVanillaDeathSound()) {
            effect = vanilla;
        } else if (sound != null) {
            effect = CraftSound.bukkitToMinecraft(sound);
        } else {
            return;
        }
        net.minecraft.world.entity.player.Player source = victim instanceof net.minecraft.world.entity.player.Player player ? player : null;
        victim.level().playSound(source, victim.getX(), victim.getY(), victim.getZ(), effect, SoundSource.valueOf(category.name()),
            bridge.bridge$getDeathSoundVolume(), bridge.bridge$getDeathSoundPitch());
    }

    public static boolean isCancelled(EntityDeathEvent event) {
        return event != null && ((EntityDeathEventBridge) event).bridge$isCancelled();
    }
}
