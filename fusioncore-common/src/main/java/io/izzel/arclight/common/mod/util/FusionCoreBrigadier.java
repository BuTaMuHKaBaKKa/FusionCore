package io.izzel.arclight.common.mod.util;

import com.google.common.collect.MapMaker;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import java.util.Map;

/** Fusion Core: Paper's {@code CommandNode#clientNode} (patch 0276), kept in a weak identity map. */
public final class FusionCoreBrigadier {

    private static final Map<CommandNode<?>, CommandNode<?>> CLIENT_NODES = new MapMaker().weakKeys().makeMap();

    private FusionCoreBrigadier() {
    }

    public static void setClientNode(CommandNode<?> node, CommandNode<?> clientNode) {
        if (clientNode == null) {
            CLIENT_NODES.remove(node);
        } else {
            CLIENT_NODES.put(node, clientNode);
        }
    }

    /**
     * The root as clients see it: the same root when none of its children has a client node, otherwise a copy whose
     * children are swapped for their client nodes (only root literals have them).
     */
    public static <S> CommandNode<S> clientRoot(CommandNode<S> root) {
        if (CLIENT_NODES.isEmpty()) {
            return root;
        }
        boolean swapped = false;
        for (CommandNode<S> child : root.getChildren()) {
            if (CLIENT_NODES.containsKey(child)) {
                swapped = true;
                break;
            }
        }
        if (!swapped) {
            return root;
        }
        RootCommandNode<S> copy = new RootCommandNode<>();
        for (CommandNode<S> child : root.getChildren()) {
            copy.addChild(clientNode(child));
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    public static <S> CommandNode<S> clientNode(CommandNode<S> node) {
        if (CLIENT_NODES.isEmpty()) {
            return node;
        }
        CommandNode<?> clientNode = CLIENT_NODES.get(node);
        return clientNode == null ? node : (CommandNode<S>) clientNode;
    }
}
