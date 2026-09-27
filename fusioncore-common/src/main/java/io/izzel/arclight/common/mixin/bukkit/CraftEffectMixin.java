package io.izzel.arclight.common.mixin.bukkit;

import com.google.common.base.Preconditions;
import net.minecraft.world.level.block.Block;
import org.bukkit.Effect;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v.CraftEffect;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fusion Core: level-event data for Paper's extra {@link Effect} constants and {@code STEP_SOUND} with {@code BlockData}
 * (Paper patch "Bandaid fix for Effect"). Spigot's switch never sees Paper's constants: they are handled first.
 */
@Mixin(value = CraftEffect.class, remap = false)
public abstract class CraftEffectMixin {

    @Inject(method = "getDataValue", at = @At("HEAD"), cancellable = true)
    private static <T> void fusioncore$paperEffects(Effect effect, T data, CallbackInfoReturnable<Integer> cir) {
        Integer value = switch (effect.name()) {
            case "PARTICLES_SCULK_CHARGE", "TRIAL_SPAWNER_DETECT_PLAYER", "BEE_GROWTH", "TURTLE_EGG_PLACEMENT", "SMASH_ATTACK",
                 "TRIAL_SPAWNER_DETECT_PLAYER_OMINOUS" -> (Integer) data;
            case "TRIAL_SPAWNER_SPAWN", "TRIAL_SPAWNER_SPAWN_MOB_AT", "VAULT_ACTIVATE", "VAULT_DEACTIVATE", "TRIAL_SPAWNER_BECOME_OMINOUS",
                 "TRIAL_SPAWNER_SPAWN_ITEM" -> ((Boolean) data) ? 1 : 0;
            case "SHOOT_WHITE_SMOKE" -> {
                final BlockFace face = (BlockFace) data;
                Preconditions.checkArgument(face.isCartesian(), face + " isn't cartesian");
                yield CraftBlock.blockFaceToNotch(face).get3DDataValue();
            }
            case "PARTICLES_AND_SOUND_BRUSH_BLOCK_COMPLETE" -> Block.getId(((CraftBlockData) data).getState());
            case "STEP_SOUND" -> data instanceof CraftBlockData blockData ? Block.getId(blockData.getState()) : null;
            case "WITHER_SPAWNED", "ENDER_DRAGON_DEATH", "END_PORTAL_CREATED_IN_OVERWORLD", "SOUND_STOP_JUKEBOX_SONG", "CRAFTER_CRAFT",
                 "CRAFTER_FAIL", "PARTICLES_SCULK_SHRIEK", "PARTICLES_EGG_CRACK", "GUST_DUST", "TRIAL_SPAWNER_EJECT_ITEM", "VAULT_EJECT_ITEM",
                 "SPAWN_COBWEB", "SOUND_WITH_CHARGE_SHOT" -> 0;
            default -> null;
        };
        if (value != null) {
            cir.setReturnValue(value);
        }
    }
}
