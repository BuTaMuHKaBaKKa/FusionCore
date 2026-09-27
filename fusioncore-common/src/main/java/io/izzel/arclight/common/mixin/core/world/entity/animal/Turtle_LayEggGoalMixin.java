package io.izzel.arclight.common.mixin.core.world.entity.animal;

import io.izzel.arclight.common.bridge.core.world.entity.animal.TurtleBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.entity.animal.Turtle$TurtleLayEggGoal")
public abstract class Turtle_LayEggGoalMixin extends MoveToBlockGoal {

    @Shadow @Final private Turtle turtle;

    public Turtle_LayEggGoalMixin(PathfinderMob creature, double speedIn, int length) {
        super(creature, speedIn, length);
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public void tick() {
        super.tick();
        BlockPos blockpos = this.turtle.blockPosition();
        if (!this.turtle.isInWater() && this.isReachedTarget()) {
            if (((TurtleBridge) this.turtle).bridge$getDigging() < 1) {
                // Fusion Core: Paper's TurtleStartDiggingEvent (patch 0254)
                ((TurtleBridge) this.turtle).bridge$setDigging(io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.turtleStartDigging(this.turtle, this.blockPos));
            } else if (((TurtleBridge) this.turtle).bridge$getDigging() > this.adjustedTickDelay(200)) { // Fusion Core: vanilla's delay
                Level world = this.turtle.level();
                // Fusion Core: vanilla's laying sound, lost in Arclight's copy
                world.playSound(null, blockpos, net.minecraft.sounds.SoundEvents.TURTLE_LAY_EGG, net.minecraft.sounds.SoundSource.BLOCKS, 0.3F, 0.9F + world.random.nextFloat() * 0.2F);
                // Fusion Core: Paper's TurtleLayEggEvent (patch 0254); one egg count for the event and the block, as in Paper
                int eggs = io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.turtleLayEgg(this.turtle, this.blockPos.above(), this.turtle.getRandom().nextInt(4) + 1);
                if (eggs > 0 && CraftEventFactory.callEntityChangeBlockEvent(this.turtle, this.blockPos.above(), (Blocks.TURTLE_EGG.defaultBlockState()).setValue(TurtleEggBlock.EGGS, eggs))) {
                    BlockPos blockpos1 = this.blockPos.above();
                    BlockState blockstate = Blocks.TURTLE_EGG.defaultBlockState().setValue(TurtleEggBlock.EGGS, eggs);
                    world.setBlock(blockpos1, blockstate, 3);
                    world.gameEvent(GameEvent.BLOCK_PLACE, blockpos1, GameEvent.Context.of(this.turtle, blockstate));
                }
                ((TurtleBridge) this.turtle).bridge$setHasEgg(false);
                ((TurtleBridge) this.turtle).bridge$setDigging(false);
                this.turtle.setInLoveTime(600);
            }

            if (this.turtle.isLayingEgg()) {
                ((TurtleBridge) this.turtle).bridge$setDigging(((TurtleBridge) this.turtle).bridge$getDigging() + 1);
            }
        }

    }
}
