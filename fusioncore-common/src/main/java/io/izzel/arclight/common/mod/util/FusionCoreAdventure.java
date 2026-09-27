package io.izzel.arclight.common.mod.util;

import io.izzel.arclight.common.mod.server.ArclightServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.core.HolderLookup;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Fusion Core: Adventure components to legacy strings for {@code CommandSenderMixin}. Players get RGB codes, the console
 * named colours only. {@link #asVanilla} and {@link #asAdventure} convert without loss.
 */
public final class FusionCoreAdventure {

    private static final java.util.regex.Pattern LOCALIZATION_PATTERN = java.util.regex.Pattern.compile("%(?:(\\d+)\\$)?s");

    /** Paper's {@code PaperAdventure.FLATTENER}: translatable components become server-language text. */
    public static final net.kyori.adventure.text.flattener.ComponentFlattener FLATTENER = net.kyori.adventure.text.flattener.ComponentFlattener.basic().toBuilder()
        .complexMapper(net.kyori.adventure.text.TranslatableComponent.class, (translatable, consumer) -> {
            if (!net.minecraft.locale.Language.getInstance().has(translatable.key())) {
                for (final net.kyori.adventure.translation.Translator source : net.kyori.adventure.translation.GlobalTranslator.translator().sources()) {
                    if (source instanceof net.kyori.adventure.translation.TranslationRegistry registry && registry.contains(translatable.key())) {
                        consumer.accept(net.kyori.adventure.translation.GlobalTranslator.render(translatable, java.util.Locale.US));
                        return;
                    }
                }
            }
            final String fallback = translatable.fallback();
            final String translated = net.minecraft.locale.Language.getInstance().getOrDefault(translatable.key(), fallback != null ? fallback : translatable.key());
            final java.util.regex.Matcher matcher = LOCALIZATION_PATTERN.matcher(translated);
            final java.util.List<net.kyori.adventure.text.TranslationArgument> args = translatable.arguments();
            int argPosition = 0;
            int lastIdx = 0;
            while (matcher.find()) {
                if (lastIdx < matcher.start()) {
                    consumer.accept(Component.text(translated.substring(lastIdx, matcher.start())));
                }
                lastIdx = matcher.end();
                final String argIdx = matcher.group(1);
                if (argIdx != null) {
                    try {
                        final int idx = Integer.parseInt(argIdx) - 1;
                        if (idx < args.size()) {
                            consumer.accept(args.get(idx).asComponent());
                        }
                    } catch (final NumberFormatException ignored) {
                    }
                } else {
                    final int idx = argPosition++;
                    if (idx < args.size()) {
                        consumer.accept(args.get(idx).asComponent());
                    }
                }
            }
            if (lastIdx < translated.length()) {
                consumer.accept(Component.text(translated.substring(lastIdx)));
            }
        })
        .build();

    private static final LegacyComponentSerializer NAMED_COLOURS = LegacyComponentSerializer.builder()
        .character(LegacyComponentSerializer.SECTION_CHAR)
        .flattener(FLATTENER)
        .build();

    private static final LegacyComponentSerializer RGB_COLOURS = LegacyComponentSerializer.builder()
        .character(LegacyComponentSerializer.SECTION_CHAR)
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .flattener(FLATTENER)
        .build();

    /** Paper's {@code PaperAdventure.asPlain}: plain text with vanilla translations resolved. */
    public static String asPlain(Component component) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.builder().flattener(FLATTENER).build().serialize(component);
    }

    private FusionCoreAdventure() {
    }

    public static String toLegacy(CommandSender target, Component message) {
        return (target instanceof Player ? RGB_COLOURS : NAMED_COLOURS).serialize(message);
    }

    /**
     * For Paper setters on Bukkit types that only store legacy text (join/quit messages, sign
     * lines): keeps RGB as {@code §x}, which CraftBukkit parses back. Click and hover are lost.
     */
    public static String toLegacyRgb(Component message) {
        return RGB_COLOURS.serialize(message);
    }

    /** Console output: {@code §x} RGB codes become the nearest named colours. */
    public static String forConsole(String legacy) {
        if (legacy == null || legacy.indexOf(LegacyComponentSerializer.SECTION_CHAR + "x") < 0
            && legacy.indexOf(LegacyComponentSerializer.SECTION_CHAR + "X") < 0) {
            return legacy;
        }
        return NAMED_COLOURS.serialize(RGB_COLOURS.deserialize(legacy));
    }

    /** The reverse of {@link #toLegacyRgb}, for the matching Paper getters. */
    public static Component fromLegacy(String legacy) {
        return RGB_COLOURS.deserialize(legacy);
    }

    /**
     * Adventure to vanilla, keeping colours, click, hover and insertion. Goes through the JSON
     * text format both sides understand; Paper maps directly with its own codecs instead.
     */
    public static net.minecraft.network.chat.Component asVanilla(Component component, HolderLookup.Provider registries) {
        return net.minecraft.network.chat.Component.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component), registries);
    }

    public static Component asAdventure(net.minecraft.network.chat.Component component, HolderLookup.Provider registries) {
        return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(component, registries));
    }

    // Fusion Core: the same conversions against the server's registries, for API without a player at hand
    // (Paper's PaperAdventure#asVanilla/asAdventure). Null stays null, lists are converted element-wise.

    public static net.minecraft.network.chat.Component asVanilla(Component component) {
        return component == null ? null : asVanilla(component, ArclightServer.getMinecraftServer().registryAccess());
    }

    public static net.minecraft.network.chat.Component asVanillaNullToEmpty(Component component) {
        return component == null ? net.minecraft.network.chat.CommonComponents.EMPTY : asVanilla(component);
    }

    public static Component asAdventure(net.minecraft.network.chat.Component component) {
        return component == null ? Component.empty() : asAdventure(component, ArclightServer.getMinecraftServer().registryAccess());
    }

    /** Adventure style to vanilla, through an empty text carrying it (Paper maps it with its style codec). */
    public static net.minecraft.network.chat.Style asVanilla(net.kyori.adventure.text.format.Style style) {
        return asVanilla(Component.text("", style)).getStyle();
    }

    public static net.kyori.adventure.text.format.Style asAdventure(net.minecraft.network.chat.Style style) {
        return asAdventure(net.minecraft.network.chat.Component.empty().withStyle(style)).style();
    }

    public static java.util.List<net.minecraft.network.chat.Component> asVanilla(java.util.List<? extends Component> components) {
        java.util.List<net.minecraft.network.chat.Component> out = new java.util.ArrayList<>(components.size());
        for (Component c : components) out.add(asVanilla(c));
        return out;
    }

    public static java.util.List<Component> asAdventure(java.util.List<? extends net.minecraft.network.chat.Component> components) {
        java.util.List<Component> out = new java.util.ArrayList<>(components.size());
        for (net.minecraft.network.chat.Component c : components) out.add(asAdventure(c));
        return out;
    }
}
