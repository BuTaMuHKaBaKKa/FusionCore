package io.izzel.arclight.common.mod.util;

import com.google.common.base.Preconditions;
import io.izzel.arclight.common.bridge.core.world.item.alchemy.PotionBrewingBridge;
import io.papermc.paper.potion.PotionMix;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.attribute.CraftAttribute;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v.entity.CraftMushroomCow;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Fusion Core: bodies of Paper's potion API (patches 0656, 0669, 0674, 0976). */
public final class FusionCorePotions {

    private FusionCorePotions() {
    }

    // Custom potion mixes (Paper patch 0669)

    /** Paper's {@code PaperPotionMix}: the mix with its choices turned into stack tests. */
    public record CustomMix(ItemStack result, Predicate<ItemStack> input, Predicate<ItemStack> ingredient) {

        public CustomMix(PotionMix mix) {
            this(CraftItemStack.asNMSCopy(mix.getResult()), convert(mix.getInput()), convert(mix.getIngredient()));
        }
    }

    /** Matches recipe choices without building a vanilla {@code Ingredient}. */
    static Predicate<ItemStack> convert(RecipeChoice choice) {
        Preconditions.checkArgument(choice != null, "RecipeChoice cannot be null");
        if (choice instanceof RecipeChoice.MaterialChoice materials) {
            Set<net.minecraft.world.item.Item> items = materials.getChoices().stream()
                .map(org.bukkit.craftbukkit.v.util.CraftMagicNumbers::getItem).collect(Collectors.toUnmodifiableSet());
            return stack -> !stack.isEmpty() && items.contains(stack.getItem());
        }
        if (choice instanceof RecipeChoice.ExactChoice exact) {
            RecipeChoice copy = exact.clone();
            return stack -> !stack.isEmpty() && copy.test(CraftItemStack.asCraftMirror(stack));
        }
        RecipeChoice copy = choice.clone();
        return stack -> copy.test(CraftItemStack.asBukkitCopy(stack));
    }

    private static PotionBrewingBridge brewing() {
        MinecraftServer server = ((CraftServer) org.bukkit.Bukkit.getServer()).getServer();
        return (PotionBrewingBridge) server.potionBrewing();
    }

    public static void addPotionMix(PotionMix mix) {
        Preconditions.checkArgument(mix != null, "PotionMix cannot be null");
        brewing().bridge$addPotionMix(mix);
    }

    public static void removePotionMix(NamespacedKey key) {
        brewing().bridge$removePotionMix(key);
    }

    /**
     * Paper rebuilds the brewing registry, dropping every plugin mix. NeoForge builds it once per server and
     * never again (mods register their recipes into it through an event), so only the plugin mixes are dropped.
     */
    public static void resetPotionMixes() {
        brewing().bridge$clearPotionMixes();
    }

    // PotionEffectType (Paper patch 0656)

    private static MobEffect handle(PotionEffectType type) {
        while (type instanceof PotionEffectTypeWrapper wrapper) {
            type = wrapper.getType();
        }
        return CraftPotionEffectType.bukkitToMinecraft(type);
    }

    public static Map<Attribute, AttributeModifier> effectAttributes(PotionEffectType type) {
        // rebuilt on every call: a mod may change the effect's modifiers; amplifier 0 gives the base amounts
        Map<Attribute, AttributeModifier> map = new HashMap<>();
        handle(type).createModifiers(0, (attribute, modifier) ->
            map.put(CraftAttribute.minecraftHolderToBukkit(attribute), CraftAttributeInstance.convert(modifier)));
        return Map.copyOf(map);
    }

    public static double attributeModifierAmount(PotionEffectType type, Attribute attribute, int effectAmplifier) {
        Preconditions.checkArgument(effectAmplifier >= 0, "effectAmplifier must be greater than or equal to 0");
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> nmsAttribute = CraftAttribute.bukkitToMinecraftHolder(attribute);
        double[] amount = {Double.NaN};
        handle(type).createModifiers(effectAmplifier, (holder, modifier) -> {
            if (holder.value() == nmsAttribute.value()) {
                amount[0] = modifier.amount();
            }
        });
        Preconditions.checkArgument(!Double.isNaN(amount[0]), attribute + " is not present on " + type.getKey());
        return amount[0];
    }

    public static org.bukkit.potion.PotionEffectType$Category effectCategory(PotionEffectType type) {
        return switch (handle(type).getCategory()) {
            case BENEFICIAL -> org.bukkit.potion.PotionEffectType$Category.BENEFICIAL;
            case HARMFUL -> org.bukkit.potion.PotionEffectType$Category.HARMFUL;
            case NEUTRAL -> org.bukkit.potion.PotionEffectType$Category.NEUTRAL;
        };
    }

    // Suspicious stew entries (Paper patch 0976)

    public static List<SuspiciousEffectEntry> stewEffects(CraftMushroomCow cow) {
        SuspiciousStewEffects effects = cow.getHandle().stewEffects;
        if (effects == null) {
            return List.of();
        }
        List<SuspiciousEffectEntry> entries = new ArrayList<>(effects.effects().size());
        for (SuspiciousStewEffects.Entry effect : effects.effects()) {
            entries.add(SuspiciousEffectEntry.create(CraftPotionEffectType.minecraftHolderToBukkit(effect.effect()), effect.duration()));
        }
        return Collections.unmodifiableList(entries);
    }

    public static void setStewEffects(CraftMushroomCow cow, List<SuspiciousEffectEntry> effects) {
        if (effects.isEmpty()) {
            cow.getHandle().stewEffects = null;
            return;
        }
        List<SuspiciousStewEffects.Entry> entries = new ArrayList<>(effects.size());
        for (SuspiciousEffectEntry effect : effects) {
            entries.add(new SuspiciousStewEffects.Entry(CraftPotionEffectType.bukkitToMinecraftHolder(effect.effect()), effect.duration()));
        }
        cow.getHandle().stewEffects = new SuspiciousStewEffects(entries);
    }

    // ---- ThrownPotion#getPotionMeta/setPotionMeta/setItem (Paper patch 0674). Paper reads and writes the item's
    // components as a splash potion's meta whatever the item is; Spigot's meta helpers go by the item's own type, so the
    // components are carried over a splash potion stack and back.

    private static net.minecraft.world.item.ItemStack asSplashPotion(net.minecraft.world.item.ItemStack item) {
        return new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPLASH_POTION.builtInRegistryHolder(), 1, item.getComponentsPatch());
    }

    public static org.bukkit.inventory.meta.PotionMeta getPotionMeta(net.minecraft.world.entity.projectile.ThrownPotion potion) {
        return (org.bukkit.inventory.meta.PotionMeta) CraftItemStack.getItemMeta(asSplashPotion(potion.getItem()));
    }

    public static void setPotionMeta(net.minecraft.world.entity.projectile.ThrownPotion potion, org.bukkit.inventory.meta.PotionMeta meta) {
        net.minecraft.world.item.ItemStack item = potion.getItem();
        net.minecraft.world.item.ItemStack splash = asSplashPotion(item);
        CraftItemStack.setItemMeta(splash, meta);
        potion.setItem(new net.minecraft.world.item.ItemStack(item.getItemHolder(), item.getCount(), splash.getComponentsPatch()));
    }

    /** Paper's {@code CraftThrownPotion#setItem}: any item; a non-potion item keeps the current potion meta. */
    public static void setItem(net.minecraft.world.entity.projectile.ThrownPotion potion, org.bukkit.inventory.ItemStack item) {
        com.google.common.base.Preconditions.checkArgument(item != null, "ItemStack cannot be null");
        org.bukkit.inventory.meta.PotionMeta meta = (item.getType() == org.bukkit.Material.LINGERING_POTION || item.getType() == org.bukkit.Material.SPLASH_POTION) ? null : getPotionMeta(potion);
        potion.setItem(CraftItemStack.asNMSCopy(item));
        if (meta != null) {
            setPotionMeta(potion, meta);
        }
    }
}
