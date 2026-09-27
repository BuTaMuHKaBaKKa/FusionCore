package io.izzel.arclight.neoforge.mod.event;

import io.izzel.arclight.neoforge.mod.machines.MachineOwners;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;

// Fusion Core: the player (or a mod's fake player, such as Create's deployer acting as its owner) who places a mod's
// machine becomes its owner; runs last, so only placements nobody cancelled are recorded
public class MachineOwnerRecorder {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event instanceof BlockEvent.EntityMultiPlaceEvent multi) {
            if (multi.getEntity() instanceof Player player && multi.getLevel() instanceof Level level) {
                for (BlockSnapshot snapshot : multi.getReplacedBlockSnapshots()) {
                    MachineOwners.recordPlacement(level.getBlockEntity(snapshot.getPos()), player.getGameProfile());
                }
            }
            return;
        }
        if (event.getEntity() instanceof Player player && event.getLevel() instanceof Level level) {
            MachineOwners.recordPlacement(level.getBlockEntity(event.getPos()), player.getGameProfile());
        }
    }
}
