package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.ArclightServer;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.entity.TeleportFlag;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.RelativeMovement;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Fusion Core: Paper's teleport flags (patch 0721), passed to the Craft class through {@link #PENDING}. */
public final class FusionCoreTeleport {

    private static final ThreadLocal<Set<TeleportFlag>> PENDING = new ThreadLocal<>();
    // relative flags for the next PlayerTeleportEvent built by the core, see PlayerTeleportEventMixin
    private static final ThreadLocal<Set<TeleportFlag.Relative>> EVENT_RELATIVE = new ThreadLocal<>();

    private FusionCoreTeleport() {
    }

    public static boolean teleport(Entity entity, Location location, TeleportCause cause, TeleportFlag... flags) {
        PENDING.set(flags.length == 0 ? Set.of() : Set.copyOf(Arrays.asList(flags)));
        try {
            return entity.teleport(location, cause);
        } finally {
            PENDING.remove();
        }
    }

    /** Flags of the teleport that is starting, empty for a plain Spigot teleport. */
    public static Set<TeleportFlag> take() {
        Set<TeleportFlag> flags = PENDING.get();
        PENDING.remove();
        return flags == null ? Set.of() : flags;
    }

    /** Paper refuses to keep passengers or the vehicle across worlds. */
    public static boolean refused(net.minecraft.world.entity.Entity handle, World from, Location to, Set<TeleportFlag> flags) {
        if (to == null || to.getWorld() == null || to.getWorld() == from) {
            return false;
        }
        return flags.contains(TeleportFlag.EntityState.RETAIN_PASSENGERS) && handle.isVehicle()
            || flags.contains(TeleportFlag.EntityState.RETAIN_VEHICLE) && handle.isPassenger();
    }

    /** Paper's {@code teleportAsync}; teleports on the main thread and completes there. */
    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location, TeleportCause cause, TeleportFlag... flags) {
        if (ArclightServer.isPrimaryThread()) {
            return CompletableFuture.completedFuture(teleport(entity, location, cause, flags));
        }
        Location target = location.clone();
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        ArclightServer.executeOnMainThread(() -> {
            try {
                result.complete(teleport(entity, target, cause, flags));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    public static EnumSet<RelativeMovement> toNms(Set<TeleportFlag> flags) {
        EnumSet<RelativeMovement> set = EnumSet.noneOf(RelativeMovement.class);
        for (TeleportFlag flag : flags) {
            if (flag instanceof TeleportFlag.Relative relative) {
                set.add(switch (relative) {
                    case X -> RelativeMovement.X;
                    case Y -> RelativeMovement.Y;
                    case Z -> RelativeMovement.Z;
                    case PITCH -> RelativeMovement.X_ROT;
                    case YAW -> RelativeMovement.Y_ROT;
                });
            }
        }
        return set;
    }

    public static Set<TeleportFlag.Relative> toApi(Set<RelativeMovement> flags) {
        EnumSet<TeleportFlag.Relative> set = EnumSet.noneOf(TeleportFlag.Relative.class);
        for (RelativeMovement flag : flags) {
            set.add(switch (flag) {
                case X -> TeleportFlag.Relative.X;
                case Y -> TeleportFlag.Relative.Y;
                case Z -> TeleportFlag.Relative.Z;
                case X_ROT -> TeleportFlag.Relative.PITCH;
                case Y_ROT -> TeleportFlag.Relative.YAW;
            });
        }
        return Set.copyOf(set);
    }

    public static Set<TeleportFlag.Relative> relativeOf(Set<TeleportFlag> flags) {
        EnumSet<TeleportFlag.Relative> set = EnumSet.noneOf(TeleportFlag.Relative.class);
        for (TeleportFlag flag : flags) {
            if (flag instanceof TeleportFlag.Relative relative) {
                set.add(relative);
            }
        }
        return Set.copyOf(set);
    }

    /** Builds a PlayerTeleportEvent carrying Paper's relative flags. */
    public static PlayerTeleportEvent newEvent(org.bukkit.entity.Player player, Location from, Location to, TeleportCause cause, Set<TeleportFlag.Relative> relative) {
        EVENT_RELATIVE.set(Set.copyOf(relative));
        try {
            return new PlayerTeleportEvent(player, from, to, cause);
        } finally {
            EVENT_RELATIVE.remove();
        }
    }

    public static Set<TeleportFlag.Relative> takeEventRelative() {
        Set<TeleportFlag.Relative> relative = EVENT_RELATIVE.get();
        EVENT_RELATIVE.remove();
        return relative == null ? Set.of() : relative;
    }

    public static EntityAnchorArgument.Anchor toNms(LookAnchor anchor) {
        return switch (anchor) {
            case EYES -> EntityAnchorArgument.Anchor.EYES;
            case FEET -> EntityAnchorArgument.Anchor.FEET;
        };
    }
}
