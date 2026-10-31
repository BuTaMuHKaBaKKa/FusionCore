package ca.spottedleaf.moonrise.patches.chunk_system.level.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Fusion Core: the part of Paper's entity lookup (Moonrise chunk system) that plugins reach through {@code Level#getEntities()}.
 * The core keeps the vanilla entity manager; plugins that reference this class get a view of the level's own entity getter
 * (see {@code FusionCoreEntityLookup}).
 */
public abstract class EntityLookup implements LevelEntityGetter<Entity> {

    public final Level world;

    protected EntityLookup(Level world) {
        this.world = world;
    }

    /** Adds an entity to the world without firing spawn events; {@code false} if it is already there */
    public abstract boolean addNewEntity(Entity entity);

    public boolean hasEntity(UUID uuid) {
        return this.get(uuid) != null;
    }

    public int getEntityCount() {
        int count = 0;
        for (Entity ignored : this.getAll()) {
            count++;
        }
        return count;
    }

    public Entity[] getAllCopy() {
        List<Entity> list = new ArrayList<>();
        this.getAll().forEach(list::add);
        return list.toArray(new Entity[0]);
    }

    public void getEntities(Entity except, AABB box, List<Entity> into, Predicate<? super Entity> predicate) {
        this.get(box, entity -> {
            if (entity != except && (predicate == null || predicate.test(entity))) {
                into.add(entity);
            }
        });
    }

    public void getEntitiesWithoutDragonParts(Entity except, AABB box, List<Entity> into, Predicate<? super Entity> predicate) {
        this.getEntities(except, box, into, predicate);
    }
}
