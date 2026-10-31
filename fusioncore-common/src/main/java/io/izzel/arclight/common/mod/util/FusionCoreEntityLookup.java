package io.izzel.arclight.common.mod.util;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Paper's {@link EntityLookup} over the level's own entity getter. Plugins written for Paper check
 * {@code level.getEntities() instanceof EntityLookup}; in classes that reference Moonrise, {@code PaperCallTransformer} sends that call
 * here. The level's getter itself is left alone, since mods wrap it with their own (Sable).
 */
public final class FusionCoreEntityLookup extends EntityLookup {

    private FusionCoreEntityLookup(Level level) {
        super(level);
    }

    /** Target of the rewritten {@code Level#getEntities()} call */
    public static LevelEntityGetter<Entity> of(Level level) {
        return new FusionCoreEntityLookup(level);
    }

    private LevelEntityGetter<Entity> delegate() {
        return this.world.getEntities();
    }

    @Override
    public boolean addNewEntity(Entity entity) {
        return this.world instanceof ServerLevel level && level.entityManager.addNewEntity(entity);
    }

    @Override
    public @Nullable Entity get(int id) {
        return this.delegate().get(id);
    }

    @Override
    public @Nullable Entity get(UUID uuid) {
        return this.delegate().get(uuid);
    }

    @Override
    public Iterable<Entity> getAll() {
        return this.delegate().getAll();
    }

    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AbortableIterationConsumer<U> consumer) {
        this.delegate().get(test, consumer);
    }

    @Override
    public void get(AABB box, Consumer<Entity> consumer) {
        this.delegate().get(box, consumer);
    }

    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AABB box, AbortableIterationConsumer<U> consumer) {
        this.delegate().get(test, box, consumer);
    }
}
