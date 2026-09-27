package io.izzel.arclight.common.mod.server.entity.ai;

import com.destroystokyo.paper.entity.Pathfinder;
import com.google.common.base.Preconditions;
import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import io.izzel.arclight.common.mod.util.FusionCoreMobs;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Fusion Core: Paper's {@code PaperPathfinder} (patch 0244); follows the handle when CraftBukkit swaps it. */
public class PaperPathfinder implements Pathfinder {

    private final Mob mob;

    public PaperPathfinder(Mob mob) {
        this.mob = mob;
    }

    private net.minecraft.world.entity.Mob handle() {
        return FusionCoreMobs.mob(this.mob);
    }

    @Override
    public @NotNull Mob getEntity() {
        return this.mob;
    }

    @Override
    public void stopPathfinding() {
        handle().getNavigation().stop();
    }

    @Override
    public boolean hasPath() {
        Path path = handle().getNavigation().getPath();
        return path != null && !path.isDone();
    }

    @Override
    public @Nullable PathResult getCurrentPath() {
        Path path = handle().getNavigation().getPath();
        return path != null && !path.isDone() ? new PaperPathResult(path) : null;
    }

    @Override
    public @Nullable PathResult findPath(@NotNull Location loc) {
        Preconditions.checkArgument(loc != null, "Location can not be null");
        Path path = handle().getNavigation().createPath(loc.getX(), loc.getY(), loc.getZ(), 0);
        return path != null ? new PaperPathResult(path) : null;
    }

    @Override
    public @Nullable PathResult findPath(@NotNull LivingEntity target) {
        Preconditions.checkArgument(target != null, "Target can not be null");
        Path path = handle().getNavigation().createPath(FusionCoreEntities.living(target), 0);
        return path != null ? new PaperPathResult(path) : null;
    }

    @Override
    public boolean moveTo(@NotNull PathResult path, double speed) {
        Preconditions.checkArgument(path != null, "PathResult can not be null");
        return handle().getNavigation().moveTo(((PaperPathResult) path).path, speed);
    }

    @Override
    public boolean canOpenDoors() {
        return handle().getNavigation().getNodeEvaluator().canOpenDoors();
    }

    @Override
    public void setCanOpenDoors(boolean canOpenDoors) {
        handle().getNavigation().getNodeEvaluator().setCanOpenDoors(canOpenDoors);
    }

    @Override
    public boolean canPassDoors() {
        return handle().getNavigation().getNodeEvaluator().canPassDoors();
    }

    @Override
    public void setCanPassDoors(boolean canPassDoors) {
        handle().getNavigation().getNodeEvaluator().setCanPassDoors(canPassDoors);
    }

    @Override
    public boolean canFloat() {
        return handle().getNavigation().getNodeEvaluator().canFloat();
    }

    @Override
    public void setCanFloat(boolean canFloat) {
        handle().getNavigation().getNodeEvaluator().setCanFloat(canFloat);
    }

    private Location toLoc(Node point) {
        return new Location(this.mob.getWorld(), point.x, point.y, point.z);
    }

    public class PaperPathResult implements PathResult {

        private final Path path;

        PaperPathResult(Path path) {
            this.path = path;
        }

        @Override
        public @Nullable Location getFinalPoint() {
            Node point = this.path.getEndNode();
            return point != null ? toLoc(point) : null;
        }

        @Override
        public boolean canReachFinalPoint() {
            return this.path.canReach();
        }

        @Override
        public @NotNull List<Location> getPoints() {
            List<Location> points = new ArrayList<>();
            for (int i = 0; i < this.path.getNodeCount(); i++) {
                points.add(toLoc(this.path.getNode(i)));
            }
            return points;
        }

        @Override
        public int getNextPointIndex() {
            return this.path.getNextNodeIndex();
        }

        @Override
        public @Nullable Location getNextPoint() {
            if (this.path.getNextNodeIndex() >= this.path.getNodeCount()) {
                return null;
            }
            return toLoc(this.path.getNode(this.path.getNextNodeIndex()));
        }
    }
}
