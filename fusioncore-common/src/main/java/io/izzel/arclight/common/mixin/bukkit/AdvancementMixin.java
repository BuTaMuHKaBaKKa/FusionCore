package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Adventure API on {@link org.bukkit.advancement.Advancement} (Paper patch 0006/0010 "Adventure"). */
@Mixin(value = org.bukkit.advancement.Advancement.class, remap = false)
public interface AdvancementMixin {

    /** Paper: the advancement's chat name, bracketed with its description as hover, as vanilla announces it. */
    default net.kyori.adventure.text.Component displayName() {
        return io.izzel.arclight.common.mod.util.FusionCoreAdventure.asAdventure(net.minecraft.advancements.Advancement.name(
            ((org.bukkit.craftbukkit.v.advancement.CraftAdvancement) (Object) this).getHandle()));
    }

    // Fusion Core: Paper's advancement tree (patch 0586)

    default org.bukkit.advancement.Advancement getParent() {
        net.minecraft.advancements.AdvancementHolder holder = ((org.bukkit.craftbukkit.v.advancement.CraftAdvancement) (Object) this).getHandle();
        return holder.value().parent()
            .map(io.izzel.arclight.common.mod.server.ArclightServer.getMinecraftServer().getAdvancements()::get)
            .map(parent -> ((io.izzel.arclight.common.bridge.core.advancements.AdvancementHolderBridge) (Object) parent).bridge$getBukkit())
            .orElse(null);
    }

    default java.util.Collection<org.bukkit.advancement.Advancement> getChildren() {
        net.minecraft.advancements.AdvancementHolder holder = ((org.bukkit.craftbukkit.v.advancement.CraftAdvancement) (Object) this).getHandle();
        com.google.common.collect.ImmutableList.Builder<org.bukkit.advancement.Advancement> children = com.google.common.collect.ImmutableList.builder();
        net.minecraft.advancements.AdvancementNode node = io.izzel.arclight.common.mod.server.ArclightServer.getMinecraftServer().getAdvancements().tree().get(holder);
        if (node != null) {
            for (net.minecraft.advancements.AdvancementNode child : node.children()) {
                children.add(((io.izzel.arclight.common.bridge.core.advancements.AdvancementHolderBridge) (Object) child.holder()).bridge$getBukkit());
            }
        }
        return children.build();
    }

    default org.bukkit.advancement.Advancement getRoot() {
        net.minecraft.advancements.AdvancementHolder holder = ((org.bukkit.craftbukkit.v.advancement.CraftAdvancement) (Object) this).getHandle();
        net.minecraft.advancements.AdvancementNode node = io.izzel.arclight.common.mod.server.ArclightServer.getMinecraftServer().getAdvancements().tree().get(holder);
        return ((io.izzel.arclight.common.bridge.core.advancements.AdvancementHolderBridge) (Object) java.util.Objects.requireNonNull(node,
            "could not find internal advancement node for advancement " + holder.id()).root().holder()).bridge$getBukkit();
    }
}
