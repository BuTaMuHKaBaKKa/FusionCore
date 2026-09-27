package io.izzel.arclight.common.mixin.core.world.entity.api;

import io.izzel.arclight.common.bridge.core.world.entity.api.ExperienceOrbApiBridge;
import io.izzel.arclight.common.mod.util.FusionCoreExperienceOrbs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.ExperienceOrb$SpawnReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/** Fusion Core: Paper's orb spawn reason, source and trigger (patch 0125), saved as {@code Paper.ExpData}. */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbApiMixin implements ExperienceOrbApiBridge {

    @Unique private UUID fusioncore$sourceEntityId;
    @Unique private UUID fusioncore$triggerEntityId;
    @Unique private ExperienceOrb$SpawnReason fusioncore$spawnReason = ExperienceOrb$SpawnReason.UNKNOWN;

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDI)V", at = @At("RETURN"))
    private void fusioncore$spawnContext(Level level, double x, double y, double z, int value, CallbackInfo ci) {
        FusionCoreExperienceOrbs.applyContext((ExperienceOrb) (Object) this);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$saveExpData(CompoundTag tag, CallbackInfo ci) {
        CompoundTag comp = new CompoundTag();
        if (this.fusioncore$sourceEntityId != null) {
            comp.putUUID("source", this.fusioncore$sourceEntityId);
        }
        if (this.fusioncore$triggerEntityId != null) {
            comp.putUUID("trigger", this.fusioncore$triggerEntityId);
        }
        if (this.fusioncore$spawnReason != null && this.fusioncore$spawnReason != ExperienceOrb$SpawnReason.UNKNOWN) {
            comp.putString("reason", this.fusioncore$spawnReason.name());
        }
        tag.put("Paper.ExpData", comp);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$readExpData(CompoundTag tag, CallbackInfo ci) {
        if (!tag.contains("Paper.ExpData", Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag comp = tag.getCompound("Paper.ExpData");
        if (comp.hasUUID("source")) {
            this.fusioncore$sourceEntityId = comp.getUUID("source");
        }
        if (comp.hasUUID("trigger")) {
            this.fusioncore$triggerEntityId = comp.getUUID("trigger");
        }
        if (comp.contains("reason")) {
            String reason = comp.getString("reason");
            try {
                this.fusioncore$spawnReason = ExperienceOrb$SpawnReason.valueOf(reason);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Invalid spawnReason set for experience orb: " + e.getMessage() + " - " + reason);
            }
        }
    }

    @Override
    public ExperienceOrb$SpawnReason fusioncore$spawnReason() {
        return this.fusioncore$spawnReason;
    }

    @Override
    public UUID fusioncore$triggerEntityId() {
        return this.fusioncore$triggerEntityId;
    }

    @Override
    public UUID fusioncore$sourceEntityId() {
        return this.fusioncore$sourceEntityId;
    }

    @Override
    public void fusioncore$setSpawnData(ExperienceOrb$SpawnReason reason, UUID trigger, UUID source) {
        this.fusioncore$spawnReason = reason != null ? reason : ExperienceOrb$SpawnReason.UNKNOWN;
        this.fusioncore$triggerEntityId = trigger;
        this.fusioncore$sourceEntityId = source;
    }
}
