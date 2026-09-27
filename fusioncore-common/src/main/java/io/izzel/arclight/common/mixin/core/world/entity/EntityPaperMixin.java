package io.izzel.arclight.common.mixin.core.world.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.bridge.core.entity.EntityPaperBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/** Fusion Core: Paper's per-entity state, saved under Paper's NBT keys so worlds move between Paper and Fusion Core. */
@Mixin(Entity.class)
public abstract class EntityPaperMixin implements EntityPaperBridge {

    // @formatter:off
    @Shadow public abstract Level level();
    @Shadow protected abstract void setLevel(Level level);
    @Shadow protected abstract void onBelowWorld();
    // @formatter:on

    @Unique private org.bukkit.util.Vector fusioncore$origin;
    @Unique private UUID fusioncore$originWorld;
    @Unique private CreatureSpawnEvent.SpawnReason fusioncore$spawnReason;
    @Unique private boolean fusioncore$fromMobSpawner;
    @Unique private boolean fusioncore$freezeLocked;
    @Unique private boolean fusioncore$fixedPose;

    @Override
    public org.bukkit.util.Vector fusioncore$originVector() {
        return this.fusioncore$origin != null ? this.fusioncore$origin.clone() : null;
    }

    @Override
    public UUID fusioncore$originWorld() {
        return this.fusioncore$originWorld;
    }

    @Override
    public void fusioncore$setOrigin(org.bukkit.Location location) {
        this.fusioncore$origin = location.toVector();
        this.fusioncore$originWorld = location.getWorld() == null ? null : location.getWorld().getUID();
    }

    @Override
    public CreatureSpawnEvent.SpawnReason fusioncore$spawnReason() {
        return this.fusioncore$spawnReason;
    }

    @Override
    public void fusioncore$setSpawnReason(CreatureSpawnEvent.SpawnReason reason) {
        this.fusioncore$spawnReason = reason;
    }

    @Override
    public boolean fusioncore$fromMobSpawner() {
        return this.fusioncore$fromMobSpawner;
    }

    @Override
    public void fusioncore$setFromMobSpawner(boolean fromMobSpawner) {
        this.fusioncore$fromMobSpawner = fromMobSpawner;
    }

    @Override
    public boolean fusioncore$freezeLocked() {
        return this.fusioncore$freezeLocked;
    }

    @Override
    public void fusioncore$setFreezeLocked(boolean locked) {
        this.fusioncore$freezeLocked = locked;
    }

    @Override
    public boolean fusioncore$fixedPose() {
        return this.fusioncore$fixedPose;
    }

    @Override
    public void fusioncore$setFixedPose(boolean fixed) {
        this.fusioncore$fixedPose = fixed;
    }

    @Override
    public void fusioncore$setLevel(Level level) {
        this.setLevel(level);
    }

    @Unique
    private UUID fusioncore$levelUid() {
        Level level = this.level();
        return level instanceof WorldBridge bridge && bridge.bridge$getWorld() != null ? bridge.bridge$getWorld().getUID() : null;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void fusioncore$savePaper(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.fusioncore$origin != null) {
            UUID world = this.fusioncore$originWorld != null ? this.fusioncore$originWorld : this.fusioncore$levelUid();
            if (world != null) {
                tag.putUUID("Paper.OriginWorld", world);
            }
            ListTag origin = new ListTag();
            origin.add(DoubleTag.valueOf(this.fusioncore$origin.getX()));
            origin.add(DoubleTag.valueOf(this.fusioncore$origin.getY()));
            origin.add(DoubleTag.valueOf(this.fusioncore$origin.getZ()));
            tag.put("Paper.Origin", origin);
        }
        if (this.fusioncore$fromMobSpawner) {
            tag.putBoolean("Paper.FromMobSpawner", true);
        }
        if (this.fusioncore$spawnReason != null) {
            tag.putString("Paper.SpawnReason", this.fusioncore$spawnReason.name());
        }
        if (this.fusioncore$freezeLocked) {
            tag.putBoolean("Paper.FreezeLock", true);
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void fusioncore$loadPaper(CompoundTag tag, CallbackInfo ci) {
        ListTag origin = tag.getList("Paper.Origin", Tag.TAG_DOUBLE);
        if (!origin.isEmpty()) {
            this.fusioncore$originWorld = tag.hasUUID("Paper.OriginWorld") ? tag.getUUID("Paper.OriginWorld") : this.fusioncore$levelUid();
            this.fusioncore$origin = new org.bukkit.util.Vector(origin.getDouble(0), origin.getDouble(1), origin.getDouble(2));
        }
        this.fusioncore$fromMobSpawner = tag.getBoolean("Paper.FromMobSpawner");
        if (tag.contains("Paper.SpawnReason")) {
            try {
                this.fusioncore$spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(tag.getString("Paper.SpawnReason"));
            } catch (IllegalArgumentException ignored) {
                // an unknown reason (saved by a newer server) falls back as if none was saved
            }
        }
        if (this.fusioncore$spawnReason == null) {
            Entity self = (Entity) (Object) this;
            if (this.fusioncore$fromMobSpawner) {
                this.fusioncore$spawnReason = CreatureSpawnEvent.SpawnReason.SPAWNER;
            } else if (self instanceof Mob mob && (self instanceof net.minecraft.world.entity.animal.Animal || self instanceof net.minecraft.world.entity.animal.AbstractFish)
                && !mob.removeWhenFarAway(0.0) && !tag.getBoolean("PersistenceRequired")) {
                this.fusioncore$spawnReason = CreatureSpawnEvent.SpawnReason.NATURAL;
            }
        }
        if (this.fusioncore$spawnReason == null) {
            this.fusioncore$spawnReason = CreatureSpawnEvent.SpawnReason.DEFAULT;
        }
        if (tag.contains("Paper.FreezeLock")) {
            this.fusioncore$freezeLocked = tag.getBoolean("Paper.FreezeLock");
        }
    }

    // Paper patch 0866: a fixed pose ignores every pose change except the API's own
    @Inject(method = "setPose", cancellable = true, at = @At("HEAD"))
    private void fusioncore$fixedPose(Pose pose, CallbackInfo ci) {
        if (this.fusioncore$fixedPose) {
            ci.cancel();
        }
    }

    // Paper patch 0655: a burning entity does not thaw while its freeze ticks are locked
    @ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getTicksFrozen()I"))
    private int fusioncore$freezeLockThaw(int ticksFrozen) {
        return this.fusioncore$freezeLocked ? 0 : ticksFrozen;
    }

    // Paper patch 0118: ArmorStand#setCanMove(false) stops all movement
    @Inject(method = "move", cancellable = true, at = @At("HEAD"))
    private void fusioncore$armorStandCanMove(net.minecraft.world.entity.MoverType type, net.minecraft.world.phys.Vec3 movement, CallbackInfo ci) {
        if ((Object) this instanceof io.izzel.arclight.common.bridge.core.world.entity.ArmorStandPaperBridge stand && !stand.fusioncore$canMove()) {
            ci.cancel();
        }
    }

    // Paper patch 1064 "Void damage configuration API": per world on/off and threshold (World#setVoidDamage*)
    @Inject(method = "checkBelowWorld", cancellable = true, at = @At("HEAD"))
    private void fusioncore$voidDamage(CallbackInfo ci) {
        if (this.level() instanceof io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge world) {
            ci.cancel();
            if (world.fusioncore$voidDamageEnabled()
                && ((Entity) (Object) this).getY() < this.level().getMinBuildHeight() + world.fusioncore$voidDamageMinBuildHeightOffset()) {
                this.onBelowWorld();
            }
        }
    }

    // Fusion Core: an entity's own shear drops are held back while Paper's drops of the shear event replace them (FusionCoreShearing)
    @Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", cancellable = true, at = @At("HEAD"))
    private void fusioncore$holdBackShearDrop(net.minecraft.world.item.ItemStack stack, float offsetY,
                                            CallbackInfoReturnable<net.minecraft.world.entity.item.ItemEntity> cir) {
        if (io.izzel.arclight.common.mod.util.FusionCoreShearing.holdBack((Entity) (Object) this)) {
            cir.setReturnValue(null);
        }
    }
}
