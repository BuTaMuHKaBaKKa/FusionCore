package io.izzel.arclight.common.mod.util.remapper.patcher;

import io.izzel.arclight.common.mod.util.FusionCoreNmsAccess;
import io.izzel.arclight.common.mod.util.remapper.ArclightRemapConfig;
import io.izzel.arclight.common.mod.util.remapper.ClassLoaderRemapper;
import io.izzel.arclight.common.mod.util.remapper.PluginTransformer;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fusion Core: turns plugin access to non-public Minecraft fields (public in Spigot) into {@code invokedynamic} calls
 * bound by {@link FusionCoreNmsAccess}.
 */
public class NmsFieldAccessTransformer implements PluginTransformer {

    private static final Handle BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(FusionCoreNmsAccess.class), "bootstrap",
        MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, Class.class, String.class)
            .toMethodDescriptorString(), false);

    /** owner.name -> needs the bridge */
    private static final Map<String, Boolean> INACCESSIBLE = new ConcurrentHashMap<>();

    @Override
    public void handleClass(ClassNode node, ClassLoaderRemapper remapper, ArclightRemapConfig config) {
        if ((node.version & 0xFFFF) < Opcodes.V1_7) {
            return; // no invokedynamic before class file 51
        }
        for (MethodNode method : node.methods) {
            if (method.instructions == null) {
                continue;
            }
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn instanceof FieldInsnNode field && field.owner.startsWith("net/minecraft/") && !field.owner.equals(node.name)
                    && inaccessible(field.owner, field.name)) {
                    method.instructions.set(insn, bridge(field));
                }
            }
        }
    }

    private static boolean inaccessible(String owner, String name) {
        return INACCESSIBLE.computeIfAbsent(owner + '.' + name, key -> {
            try {
                Class<?> cl = Class.forName(owner.replace('/', '.'), false, NmsFieldAccessTransformer.class.getClassLoader());
                Field field = find(cl, name);
                return field != null && !(Modifier.isPublic(field.getModifiers()) && Modifier.isPublic(field.getDeclaringClass().getModifiers()));
            } catch (Throwable e) {
                return false; // unknown: leave the instruction as it is
            }
        });
    }

    private static Field find(Class<?> owner, String name) {
        for (Class<?> c = owner; c != null; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getName().equals(name)) {
                    return f;
                }
            }
        }
        return null;
    }

    private static InvokeDynamicInsnNode bridge(FieldInsnNode field) {
        Type owner = Type.getObjectType(field.owner);
        String kind;
        String desc;
        switch (field.getOpcode()) {
            case Opcodes.GETFIELD -> {
                kind = FusionCoreNmsAccess.GET;
                desc = "(" + owner.getDescriptor() + ")" + field.desc;
            }
            case Opcodes.PUTFIELD -> {
                kind = FusionCoreNmsAccess.PUT;
                desc = "(" + owner.getDescriptor() + field.desc + ")V";
            }
            case Opcodes.GETSTATIC -> {
                kind = FusionCoreNmsAccess.GET_STATIC;
                desc = "()" + field.desc;
            }
            default -> {
                kind = FusionCoreNmsAccess.PUT_STATIC;
                desc = "(" + field.desc + ")V";
            }
        }
        return new InvokeDynamicInsnNode(kind, desc, BOOTSTRAP, owner, field.name);
    }
}
