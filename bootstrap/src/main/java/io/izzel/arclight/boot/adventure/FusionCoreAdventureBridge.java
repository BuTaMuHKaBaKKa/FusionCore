package io.izzel.arclight.boot.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBarImplementation;
import net.kyori.adventure.bossbar.BossBarViewer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.DataComponentValueConverterRegistry;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.Codec;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Fusion Core: Paper's Adventure service providers (patch 0010). Adventure loads them from the launcher layer,
 * which cannot see the server, so they live here and forward to what the server passes to {@link #install}.
 */
public final class FusionCoreAdventureBridge {

    private static volatile ComponentFlattener flattener;
    private static volatile LegacyHoverEventSerializer legacyHover;
    private static volatile Function<BossBar, Iterable<? extends BossBarViewer>> bossBarViewers;
    private static volatile BiFunction<ClickCallback<Audience>, ClickCallback.Options, ClickEvent> clickCallbacks;
    private static volatile BiFunction<Key, DataComponentValue.TagSerializable, GsonDataComponentValue> tagToGson;
    private static volatile BiFunction<Key, GsonDataComponentValue, DataComponentValue.TagSerializable> gsonToTag;

    private FusionCoreAdventureBridge() {
    }

    /** Called by the server (by reflection: the game layer does not compile against this jar). */
    public static void install(ComponentFlattener flattener, LegacyHoverEventSerializer legacyHover,
                               Function<BossBar, Iterable<? extends BossBarViewer>> bossBarViewers,
                               BiFunction<ClickCallback<Audience>, ClickCallback.Options, ClickEvent> clickCallbacks,
                               BiFunction<Key, DataComponentValue.TagSerializable, GsonDataComponentValue> tagToGson,
                               BiFunction<Key, GsonDataComponentValue, DataComponentValue.TagSerializable> gsonToTag) {
        FusionCoreAdventureBridge.flattener = flattener;
        FusionCoreAdventureBridge.legacyHover = legacyHover;
        FusionCoreAdventureBridge.bossBarViewers = bossBarViewers;
        FusionCoreAdventureBridge.clickCallbacks = clickCallbacks;
        FusionCoreAdventureBridge.tagToGson = tagToGson;
        FusionCoreAdventureBridge.gsonToTag = gsonToTag;
    }

    /** Paper's {@code PaperAdventure.FLATTENER} once the server is up, Adventure's basic flattener before. */
    static final ComponentFlattener FLATTENER = new ComponentFlattener() {
        private ComponentFlattener current() {
            ComponentFlattener current = flattener;
            return current == null ? ComponentFlattener.basic() : current;
        }

        @Override
        public void flatten(Component input, FlattenerListener listener) {
            this.current().flatten(input, listener);
        }

        @Override
        public Builder toBuilder() {
            return this.current().toBuilder();
        }
    };

    /** Paper's {@code NBTLegacyHoverEventSerializer}: the pre-1.16 {@code value} of item and entity hovers as SNBT. */
    static final LegacyHoverEventSerializer LEGACY_HOVER = new LegacyHoverEventSerializer() {
        private LegacyHoverEventSerializer current() throws IOException {
            LegacyHoverEventSerializer current = legacyHover;
            if (current == null) {
                throw new IOException("The server has not started yet");
            }
            return current;
        }

        @Override
        public HoverEvent.ShowItem deserializeShowItem(Component input) throws IOException {
            return this.current().deserializeShowItem(input);
        }

        @Override
        public Component serializeShowItem(HoverEvent.ShowItem input) throws IOException {
            return this.current().serializeShowItem(input);
        }

        @Override
        public HoverEvent.ShowEntity deserializeShowEntity(Component input, Codec.Decoder<Component, String, ? extends RuntimeException> componentCodec) throws IOException {
            return this.current().deserializeShowEntity(input, componentCodec);
        }

        @Override
        public Component serializeShowEntity(HoverEvent.ShowEntity input, Codec.Encoder<Component, String, ? extends RuntimeException> componentCodec) throws IOException {
            return this.current().serializeShowEntity(input, componentCodec);
        }
    };

    public static final class PlainText implements PlainTextComponentSerializer.Provider {
        @Override
        public PlainTextComponentSerializer plainTextSimple() {
            return PlainTextComponentSerializer.builder().flattener(FLATTENER).build();
        }

        @Override
        public Consumer<PlainTextComponentSerializer.Builder> plainText() {
            return builder -> builder.flattener(FLATTENER);
        }
    }

    public static final class Legacy implements LegacyComponentSerializer.Provider {
        @Override
        public LegacyComponentSerializer legacyAmpersand() {
            return LegacyComponentSerializer.builder()
                .flattener(FLATTENER)
                .character(LegacyComponentSerializer.AMPERSAND_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();
        }

        @Override
        public LegacyComponentSerializer legacySection() {
            return LegacyComponentSerializer.builder()
                .flattener(FLATTENER)
                .character(LegacyComponentSerializer.SECTION_CHAR)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();
        }

        @Override
        public Consumer<LegacyComponentSerializer.Builder> legacy() {
            return builder -> builder.flattener(FLATTENER);
        }
    }

    public static final class Gson implements GsonComponentSerializer.Provider {
        @Override
        // both also read the click and hover events of 1.21.5+ JSON (plugins with a newer Adventure), see ModernJsonGsonSerializer
        public GsonComponentSerializer gson() {
            return new ModernJsonGsonSerializer(GsonComponentSerializer.builder().legacyHoverEventSerializer(LEGACY_HOVER).build());
        }

        @Override
        public GsonComponentSerializer gsonLegacy() {
            return new ModernJsonGsonSerializer(GsonComponentSerializer.builder().legacyHoverEventSerializer(LEGACY_HOVER).downsampleColors().build());
        }

        @Override
        public Consumer<GsonComponentSerializer.Builder> builder() {
            return builder -> builder.legacyHoverEventSerializer(LEGACY_HOVER);
        }
    }

    public static final class Mini implements MiniMessage.Provider {
        @Override
        public MiniMessage miniMessage() {
            return MiniMessage.builder().build();
        }

        @Override
        public Consumer<MiniMessage.Builder> builder() {
            return builder -> {
            };
        }
    }

    public static final class Logger implements ComponentLoggerProvider {
        private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.builder().flattener(FLATTENER).build();

        @Override
        public ComponentLogger logger(LoggerHelper helper, String name) {
            return helper.delegating(LoggerFactory.getLogger(name), PLAIN::serialize);
        }
    }

    public static final class BossBars implements BossBarImplementation.Provider {
        @Override
        public BossBarImplementation create(BossBar bar) {
            return new BossBarImplementation() {
                @Override
                public Iterable<? extends BossBarViewer> viewers() {
                    Function<BossBar, Iterable<? extends BossBarViewer>> viewers = bossBarViewers;
                    return viewers == null ? Set.of() : viewers.apply(bar);
                }
            };
        }
    }

    public static final class ClickCallbacks implements ClickCallback.Provider {
        @Override
        public ClickEvent create(ClickCallback<Audience> callback, ClickCallback.Options options) {
            BiFunction<ClickCallback<Audience>, ClickCallback.Options, ClickEvent> callbacks = clickCallbacks;
            if (callbacks == null) {
                throw new IllegalStateException("The server has not started yet");
            }
            return callbacks.apply(callback, options);
        }
    }

    public static final class DataComponentValues implements DataComponentValueConverterRegistry.Provider {
        private static final Key ID = Key.key("adventure", "platform/fusioncore");

        @Override
        public Key id() {
            return ID;
        }

        @Override
        public Iterable<DataComponentValueConverterRegistry.Conversion<?, ?>> conversions() {
            return List.of(
                DataComponentValueConverterRegistry.Conversion.convert(DataComponentValue.TagSerializable.class, GsonDataComponentValue.class,
                    (key, value) -> ready(tagToGson).apply(key, value)),
                DataComponentValueConverterRegistry.Conversion.convert(GsonDataComponentValue.class, DataComponentValue.TagSerializable.class,
                    (key, value) -> ready(gsonToTag).apply(key, value))
            );
        }

        private static <T> T ready(T function) {
            if (function == null) {
                throw new IllegalStateException("The server has not started yet");
            }
            return function;
        }
    }
}
