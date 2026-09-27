package io.izzel.arclight.common.mod.mixins;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Map;

/** Fusion Core: swaps in the Paper superclass of Bukkit classes whose superclass Paper changed. */
public class SuperclassProcessor implements MixinProcessor {

    /** Target (internal name) → {old superclass, Paper's superclass}. */
    private static final Map<String, String[]> SUPERCLASSES = Map.of(
        "org/bukkit/event/inventory/PrepareAnvilEvent",
        new String[]{"org/bukkit/event/inventory/PrepareInventoryResultEvent", "com/destroystokyo/paper/event/inventory/PrepareResultEvent"},
        "org/bukkit/event/inventory/PrepareSmithingEvent",
        new String[]{"org/bukkit/event/inventory/PrepareInventoryResultEvent", "com/destroystokyo/paper/event/inventory/PrepareResultEvent"},
        "org/bukkit/event/inventory/PrepareGrindstoneEvent",
        new String[]{"org/bukkit/event/inventory/PrepareInventoryResultEvent", "com/destroystokyo/paper/event/inventory/PrepareGrindstoneEvent"}
    );

    @Override
    public void accept(String className, ClassNode classNode, IMixinInfo mixinInfo) {
        String[] change = SUPERCLASSES.get(classNode.name);
        if (change == null || !change[0].equals(classNode.superName)) {
            return;
        }
        classNode.superName = change[1];
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL && call.owner.equals(change[0])) {
                    call.owner = change[1];
                }
            }
        }
    }
}
