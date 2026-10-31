package io.izzel.arclight.common.mod.util.remapper.patcher;

import io.izzel.arclight.common.mod.util.FusionCoreBukkit;
import io.izzel.arclight.common.mod.util.FusionCorePaperCalls;
import io.izzel.arclight.common.mod.util.remapper.ArclightRemapConfig;
import io.izzel.arclight.common.mod.util.remapper.ClassLoaderRemapper;
import io.izzel.arclight.common.mod.util.remapper.PluginTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Fusion Core: redirects plugin calls to Paper API that cannot exist on Bukkit types to {@link FusionCorePaperCalls}
 * and the static holders, keeping the stack shape.
 */
public class PaperCallTransformer implements PluginTransformer {

    private static final String CALLS = Type.getInternalName(FusionCorePaperCalls.class);
    private static final String COMPONENT = "Lnet/kyori/adventure/text/Component;";
    private static final String BOOK_META = "org/bukkit/inventory/meta/BookMeta";
    private static final String BOOK_META_BUILDER = "org/bukkit/inventory/meta/BookMeta$BookMetaBuilder";
    /**
     * Interface calls whose Paper descriptor returns {@code BookMeta} while the method that exists returns
     * Adventure's {@code Book} (see {@code BookMetaMixin}): owner + name + descriptor -> target in FusionCorePaperCalls.
     */
    private static final java.util.Map<String, String> BOOK_CALLS = java.util.Map.of(
        BOOK_META + ".title(" + COMPONENT + ")L" + BOOK_META + ";", "bookTitle",
        BOOK_META + ".author(" + COMPONENT + ")L" + BOOK_META + ";", "bookAuthor",
        BOOK_META + ".pages(Ljava/util/List;)L" + BOOK_META + ";", "bookPages",
        BOOK_META + ".pages([" + COMPONENT + ")L" + BOOK_META + ";", "bookPagesArray",
        BOOK_META_BUILDER + ".build()L" + BOOK_META + ";", "buildBook");
    /**
     * Paper's statics on Bukkit classes, declared under the same names and descriptors in a Fusion Core
     * holder class: Bukkit class -> holder. A mixin cannot add public static methods.
     */
    private static final java.util.Map<Class<?>, Class<?>> STATIC_HOLDERS = java.util.Map.of(
        org.bukkit.Bukkit.class, FusionCoreBukkit.class,
        org.bukkit.command.Command.class, io.izzel.arclight.common.mod.util.FusionCoreCommandStatics.class,
        org.bukkit.inventory.ItemStack.class, io.izzel.arclight.common.mod.util.FusionCoreItemStackStatics.class,
        org.bukkit.block.Block.class, io.izzel.arclight.common.mod.util.FusionCoreBlockStatics.class,
        org.bukkit.WorldCreator.class, io.izzel.arclight.common.mod.util.FusionCoreWorldCreatorStatics.class,
        org.bukkit.inventory.RecipeChoice.class, io.izzel.arclight.common.mod.util.FusionCoreRecipeChoiceStatics.class,
        org.bukkit.UnsafeValues.class, io.izzel.arclight.common.mod.util.FusionCoreUnsafeValuesStatics.class,
        org.bukkit.Chunk.class, io.izzel.arclight.common.mod.util.FusionCoreChunkStatics.class,
        org.bukkit.plugin.EventExecutor.class, io.izzel.arclight.common.mod.util.FusionCoreEventExecutorStatics.class,
        org.bukkit.event.vehicle.VehicleCollisionEvent.class, io.izzel.arclight.common.mod.util.FusionCoreVehicleCollisionEventStatics.class);
    /** Bukkit class (internal name) -> name + descriptor -> holder (internal name) */
    private static final java.util.Map<String, java.util.Map<String, String>> STATIC_METHODS = new java.util.HashMap<>();

    static {
        STATIC_HOLDERS.forEach((owner, holder) -> {
            java.util.Map<String, String> methods = new java.util.HashMap<>();
            for (Method m : holder.getDeclaredMethods()) {
                if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
                    methods.put(m.getName() + Type.getMethodDescriptor(m), Type.getInternalName(holder));
                }
            }
            // never take over a method the Bukkit class has itself (a private one a mixin added is not callable from a plugin)
            for (Method m : owner.getDeclaredMethods()) {
                if (!Modifier.isPrivate(m.getModifiers())) {
                    methods.remove(m.getName() + Type.getMethodDescriptor(m));
                }
            }
            STATIC_METHODS.put(Type.getInternalName(owner), methods);
        });
    }

    /**
     * Paper's constructors of Bukkit classes that Spigot lacks: owner + "." + descriptor -> factory in
     * {@code FusionCoreEvents} named "new" + simple class name, with the constructor's parameters.
     */
    private static final java.util.Map<String, String[]> CONSTRUCTORS = new java.util.HashMap<>();

    static {
        Class<?> holder = io.izzel.arclight.common.mod.util.FusionCoreEvents.class;
        for (Method m : holder.getDeclaredMethods()) {
            if (!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers())
                || !m.getName().equals("new" + m.getReturnType().getSimpleName())) {
                continue;
            }
            String ctor = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getArgumentTypes(m));
            boolean exists = false;
            for (var c : m.getReturnType().getDeclaredConstructors()) {
                exists |= Type.getConstructorDescriptor(c).equals(ctor);
            }
            if (!exists) {
                CONSTRUCTORS.put(Type.getInternalName(m.getReturnType()) + "." + ctor,
                    new String[]{Type.getInternalName(holder), m.getName(), Type.getMethodDescriptor(m)});
            }
        }
    }

    /**
     * Paper's enum constants that Spigot's enums lack: owner + "." + name -> getter in {@code FusionCoreEnumConstants}
     * named {@code <SimpleName>$<NAME>}, used only while the enum has no such field.
     */
    private static final java.util.Map<String, String[]> ENUM_CONSTANTS = new java.util.HashMap<>();

    static {
        Class<?> holder = io.izzel.arclight.common.mod.util.FusionCoreEnumConstants.class;
        for (Method m : holder.getDeclaredMethods()) {
            int split = m.getName().indexOf('$');
            var of = m.getAnnotation(io.izzel.arclight.common.mod.util.FusionCoreEnumConstants.Of.class);
            Class<?> owner = of != null ? of.value() : m.getReturnType();
            if (!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers()) || split < 0
                || !m.getName().substring(0, split).equals(owner.getSimpleName())) {
                continue;
            }
            String field = m.getName().substring(split + 1);
            boolean exists = false;
            for (var f : owner.getDeclaredFields()) {
                exists |= f.getName().equals(field);
            }
            if (!exists) {
                ENUM_CONSTANTS.put(Type.getInternalName(owner) + "." + field,
                    new String[]{Type.getInternalName(holder), m.getName(), Type.getMethodDescriptor(m)});
            }
        }
    }

    /**
     * Turns {@code NEW; DUP; args; INVOKESPECIAL <init>} into {@code args; INVOKESTATIC factory}; leaves the code alone when
     * that is not safe.
     */
    private static void rewriteConstructor(MethodNode mn, MethodInsnNode init, String[] factory) {
        int depth = 0;
        TypeInsnNode found = null;
        for (AbstractInsnNode p = init.getPrevious(); p != null; p = p.getPrevious()) {
            if (p instanceof FrameNode) {
                return;
            }
            if (p instanceof MethodInsnNode m && m.getOpcode() == Opcodes.INVOKESPECIAL && m.name.equals("<init>") && m.owner.equals(init.owner)) {
                depth++;
            } else if (p instanceof TypeInsnNode t && t.getOpcode() == Opcodes.NEW && t.desc.equals(init.owner)) {
                if (depth == 0) {
                    found = t;
                    break;
                }
                depth--;
            }
        }
        if (found == null) {
            return;
        }
        AbstractInsnNode dup = found.getNext();
        while (dup != null && dup.getOpcode() < 0) {
            dup = dup.getNext();
        }
        if (dup == null || dup.getOpcode() != Opcodes.DUP) {
            return;
        }
        mn.instructions.remove(found);
        mn.instructions.remove(dup);
        mn.instructions.set(init, new MethodInsnNode(Opcodes.INVOKESTATIC, factory[0], factory[1], factory[2], false));
    }

    private static final String MOONRISE = "ca/spottedleaf/moonrise/";
    private static final String ENTITY_GETTER_DESC = "()Lnet/minecraft/world/level/entity/LevelEntityGetter;";

    /** Whether the class names Paper's Moonrise chunk system anywhere in its code */
    private static boolean usesMoonrise(ClassNode node) {
        for (var mn : node.methods) {
            for (var insn : mn.instructions) {
                if (insn instanceof org.objectweb.asm.tree.TypeInsnNode type && type.desc.startsWith(MOONRISE)
                    || insn instanceof MethodInsnNode method && method.owner.startsWith(MOONRISE)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void handleClass(ClassNode node, ClassLoaderRemapper remapper, ArclightRemapConfig config) {
        boolean moonrise = usesMoonrise(node);
        for (var mn : node.methods) {
            for (var insn : mn.instructions.toArray()) {
                if (insn instanceof MethodInsnNode init && init.getOpcode() == Opcodes.INVOKESPECIAL && init.name.equals("<init>")) {
                    String[] factory = CONSTRUCTORS.get(init.owner + "." + init.desc);
                    if (factory != null) {
                        rewriteConstructor(mn, init, factory);
                    }
                }
            }
            for (var insn : mn.instructions.toArray()) {
                if (insn instanceof org.objectweb.asm.tree.FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC) {
                    String[] getter = ENUM_CONSTANTS.get(field.owner + "." + field.name);
                    if (getter != null) {
                        mn.instructions.set(field, new MethodInsnNode(Opcodes.INVOKESTATIC, getter[0], getter[1], getter[2], false));
                    }
                }
            }
            for (var insn : mn.instructions) {
                if (!(insn instanceof MethodInsnNode method)) {
                    continue;
                }
                if (method.getOpcode() == Opcodes.INVOKEINTERFACE
                    && method.owner.equals("org/bukkit/advancement/Advancement")
                    && method.name.equals("getDisplay")
                    && method.desc.equals("()Lio/papermc/paper/advancement/AdvancementDisplay;")) {
                    redirect(method, "advancementDisplay",
                        "(Lorg/bukkit/advancement/Advancement;)Lio/papermc/paper/advancement/AdvancementDisplay;");
                } else if (method.getOpcode() == Opcodes.INVOKEINTERFACE
                    && (method.owner.equals("org/bukkit/OfflinePlayer") || method.owner.equals("org/bukkit/entity/Player"))
                    && method.name.equals("getPlayerProfile")
                    && method.desc.equals("()Lcom/destroystokyo/paper/profile/PlayerProfile;")) {
                    redirect(method, "playerProfile",
                        "(Lorg/bukkit/OfflinePlayer;)Lcom/destroystokyo/paper/profile/PlayerProfile;");
                } else if (method.getOpcode() == Opcodes.INVOKESTATIC
                    && method.owner.equals("org/bukkit/Bukkit")
                    && method.name.equals("broadcast")
                    && (method.desc.equals("(" + COMPONENT + ")I")
                        || method.desc.equals("(" + COMPONENT + "Ljava/lang/String;)I"))) {
                    redirect(method, "broadcast", method.desc);
                } else if (method.getOpcode() == Opcodes.INVOKEINTERFACE
                    && BOOK_CALLS.containsKey(method.owner + "." + method.name + method.desc)) {
                    Type type = Type.getMethodType(method.desc);
                    redirect(method, BOOK_CALLS.get(method.owner + "." + method.name + method.desc),
                        "(L" + method.owner + ";" + method.desc.substring(1, method.desc.indexOf(')')) + ")" + type.getReturnType().getDescriptor());
                } else if (method.getOpcode() == Opcodes.INVOKESTATIC
                    && STATIC_METHODS.containsKey(method.owner)
                    && STATIC_METHODS.get(method.owner).containsKey(method.name + method.desc)) {
                    method.owner = STATIC_METHODS.get(method.owner).get(method.name + method.desc);
                    method.itf = false;
                } else if (moonrise && method.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && method.owner.startsWith("net/minecraft/")
                    && method.name.equals("getEntities")
                    && method.desc.equals(ENTITY_GETTER_DESC)) {
                    // Paper's entity lookup: `level.getEntities() instanceof EntityLookup` (GSit spawning its seats)
                    method.setOpcode(Opcodes.INVOKESTATIC);
                    method.owner = Type.getInternalName(io.izzel.arclight.common.mod.util.FusionCoreEntityLookup.class);
                    method.name = "of";
                    method.desc = "(Lnet/minecraft/world/level/Level;)" + ENTITY_GETTER_DESC.substring(2);
                    method.itf = false;
                }
            }
        }
    }

    private static void redirect(MethodInsnNode method, String name, String desc) {
        method.setOpcode(Opcodes.INVOKESTATIC);
        method.owner = CALLS;
        method.name = name;
        method.desc = desc;
        method.itf = false;
    }
}
