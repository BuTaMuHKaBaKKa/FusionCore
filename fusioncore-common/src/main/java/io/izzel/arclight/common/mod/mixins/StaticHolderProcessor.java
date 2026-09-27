package io.izzel.arclight.common.mod.mixins;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Fusion Core: adds Paper's static methods to Bukkit classes as delegates to the static holders, for plugins that look
 * them up by reflection. Mirrors {@code PaperCallTransformer.STATIC_HOLDERS}.
 */
public class StaticHolderProcessor implements MixinProcessor {

    private static final String UTIL = "io/izzel/arclight/common/mod/util/";

    /** Bukkit class (internal name) → holder (internal name); keep in step with PaperCallTransformer.STATIC_HOLDERS. */
    private static final Map<String, String> HOLDERS = Map.of(
        "org/bukkit/Bukkit", UTIL + "FusionCoreBukkit",
        "org/bukkit/command/Command", UTIL + "FusionCoreCommandStatics",
        "org/bukkit/inventory/ItemStack", UTIL + "FusionCoreItemStackStatics",
        "org/bukkit/block/Block", UTIL + "FusionCoreBlockStatics",
        "org/bukkit/WorldCreator", UTIL + "FusionCoreWorldCreatorStatics",
        "org/bukkit/inventory/RecipeChoice", UTIL + "FusionCoreRecipeChoiceStatics",
        "org/bukkit/UnsafeValues", UTIL + "FusionCoreUnsafeValuesStatics",
        "org/bukkit/Chunk", UTIL + "FusionCoreChunkStatics",
        "org/bukkit/event/vehicle/VehicleCollisionEvent", UTIL + "FusionCoreVehicleCollisionEventStatics"
    );

    @Override
    public void accept(String className, ClassNode classNode, IMixinInfo mixinInfo) {
        String holder = HOLDERS.get(classNode.name);
        if (holder == null) {
            return;
        }
        ClassNode holderNode = read(holder);
        if (holderNode == null) {
            return;
        }
        Set<String> present = new HashSet<>();
        for (MethodNode method : classNode.methods) {
            present.add(method.name + method.desc);
        }
        boolean isInterface = (classNode.access & Opcodes.ACC_INTERFACE) != 0;
        for (MethodNode method : holderNode.methods) {
            if ((method.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) != (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
                || method.name.startsWith("<") || !present.add(method.name + method.desc)) {
                continue;
            }
            MethodNode delegate = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | (method.access & Opcodes.ACC_VARARGS),
                method.name, method.desc, method.signature, method.exceptions == null ? null : method.exceptions.toArray(new String[0]));
            int slot = 0;
            for (Type arg : Type.getArgumentTypes(method.desc)) {
                delegate.instructions.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), slot));
                slot += arg.getSize();
            }
            delegate.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, holder, method.name, method.desc, false));
            delegate.instructions.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(Opcodes.IRETURN)));
            delegate.maxLocals = slot;
            delegate.maxStack = Math.max(slot, Type.getReturnType(method.desc).getSize());
            if (isInterface && classNode.version < Opcodes.V1_8) {
                continue; // static interface methods need class file 52
            }
            classNode.methods.add(delegate);
        }
    }

    private static ClassNode read(String internalName) {
        try (InputStream in = StaticHolderProcessor.class.getClassLoader().getResourceAsStream(internalName + ".class")) {
            if (in == null) {
                return null;
            }
            ClassNode node = new ClassNode();
            new ClassReader(in).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        } catch (IOException e) {
            return null;
        }
    }
}
