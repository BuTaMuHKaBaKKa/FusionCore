package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.WorldCreatorBridge;
import net.kyori.adventure.util.TriState;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: Paper's world key and {@code keepSpawnLoaded} on {@link WorldCreator} (patches 0500, 0692). */
@Mixin(value = WorldCreator.class, remap = false)
public abstract class WorldCreatorMixin implements WorldCreatorBridge {

    // @formatter:off
    @Shadow public abstract String name();
    @Shadow public abstract WorldCreator keepSpawnInMemory(boolean keepSpawnInMemory);
    // @formatter:on

    @Unique private NamespacedKey fusioncore$key;
    @Unique private TriState fusioncore$keepSpawnLoaded = TriState.NOT_SET;

    @Override
    public void fusioncore$setKey(NamespacedKey key) {
        this.fusioncore$key = key;
    }

    /** Paper: the dimension key of the world to create. */
    public NamespacedKey key() {
        if (this.fusioncore$key == null) {
            this.fusioncore$key = fusioncore$defaultKey(this.name());
        }
        return this.fusioncore$key;
    }

    /** Paper's {@code WorldCreator#getWorldKey}. */
    @Unique
    private static NamespacedKey fusioncore$defaultKey(String name) {
        String levelName = org.bukkit.Bukkit.getServer() == null ? null
            : ((org.bukkit.craftbukkit.v.CraftServer) org.bukkit.Bukkit.getServer()).getServer().getProperties().levelName;
        if (name.equals(levelName)) {
            return NamespacedKey.minecraft("overworld");
        } else if (name.equals(levelName + "_nether")) {
            return NamespacedKey.minecraft("the_nether");
        } else if (name.equals(levelName + "_the_end")) {
            return NamespacedKey.minecraft("the_end");
        }
        return NamespacedKey.minecraft(name.toLowerCase(java.util.Locale.ENGLISH).replace(" ", "_"));
    }

    public TriState keepSpawnLoaded() {
        return this.fusioncore$keepSpawnLoaded;
    }

    @Override
    public TriState fusioncore$keepSpawnLoaded() {
        return this.fusioncore$keepSpawnLoaded;
    }

    @Override
    public NamespacedKey fusioncore$key() {
        return this.key();
    }

    public WorldCreator keepSpawnLoaded(TriState keepSpawnLoaded) {
        com.google.common.base.Preconditions.checkArgument(keepSpawnLoaded != null, "keepSpawnLoaded");
        this.keepSpawnInMemory(keepSpawnLoaded != TriState.FALSE);
        this.fusioncore$keepSpawnLoaded = keepSpawnLoaded;
        return (WorldCreator) (Object) this;
    }

    @Inject(method = "keepSpawnInMemory(Z)Lorg/bukkit/WorldCreator;", at = @At("RETURN"))
    private void fusioncore$keepSpawnInMemory(boolean keepSpawnInMemory, CallbackInfoReturnable<WorldCreator> cir) {
        this.fusioncore$keepSpawnLoaded = TriState.byBoolean(keepSpawnInMemory);
    }

    @Inject(method = "copy(Lorg/bukkit/WorldCreator;)Lorg/bukkit/WorldCreator;", at = @At("RETURN"))
    private void fusioncore$copyCreator(WorldCreator creator, CallbackInfoReturnable<WorldCreator> cir) {
        this.fusioncore$keepSpawnLoaded = ((WorldCreatorBridge) creator).fusioncore$keepSpawnLoaded();
    }

    @Inject(method = "copy(Lorg/bukkit/World;)Lorg/bukkit/WorldCreator;", at = @At("RETURN"))
    private void fusioncore$copyWorld(World world, CallbackInfoReturnable<WorldCreator> cir) {
        this.fusioncore$keepSpawnLoaded = TriState.byBoolean(world.getKeepSpawnInMemory());
    }
}
