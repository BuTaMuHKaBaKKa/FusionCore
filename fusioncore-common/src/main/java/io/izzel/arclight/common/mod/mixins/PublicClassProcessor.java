package io.izzel.arclight.common.mod.mixins;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Map;
import java.util.Set;

/**
 * Fusion Core: makes Bukkit classes public that Paper made public (e.g. {@code PluginClassLoader}). Compiled against the stubs
 * in {@code src/stubs}.
 */
public class PublicClassProcessor implements MixinProcessor {

    /** Target (internal name) → methods (name + descriptor) Paper made public. */
    private static final Map<String, Set<String>> CLASSES = Map.of(
        "org/bukkit/plugin/java/PluginClassLoader", Set.of("initialize(Lorg/bukkit/plugin/java/JavaPlugin;)V"),
        "org/bukkit/plugin/java/LibraryLoader", Set.of()
    );

    @Override
    public void accept(String className, ClassNode classNode, IMixinInfo mixinInfo) {
        Set<String> methods = CLASSES.get(classNode.name);
        if (methods == null) {
            return;
        }
        classNode.access = (classNode.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
        for (MethodNode method : classNode.methods) {
            if (methods.contains(method.name + method.desc)) {
                method.access = (method.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
            }
        }
    }
}
