package io.izzel.arclight.common.mod.util;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.izzel.arclight.common.mod.server.ArclightServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBarViewer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import net.kyori.adventure.text.serializer.json.LegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.CraftServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Fusion Core: the server side of Paper's Adventure providers (patch 0010), installed into the launcher-side bridge. */
public final class FusionCoreAdventureProviders {

    private static final Logger LOGGER = LogManager.getLogger("Fusion Core");
    private static final String BRIDGE = "io.izzel.arclight.boot.adventure.FusionCoreAdventureBridge";

    private FusionCoreAdventureProviders() {
    }

    public static void install() {
        try {
            Class.forName(BRIDGE).getMethod("install", ComponentFlattener.class, LegacyHoverEventSerializer.class, Function.class,
                    BiFunction.class, BiFunction.class, BiFunction.class)
                .invoke(null, FusionCoreAdventure.FLATTENER, NBTLegacyHoverEventSerializer.INSTANCE,
                    (Function<BossBar, Iterable<? extends BossBarViewer>>) FusionCoreAudiences::viewers,
                    (BiFunction<ClickCallback<Audience>, ClickCallback.Options, ClickEvent>) CALLBACK_MANAGER::create,
                    (BiFunction<Key, DataComponentValue.TagSerializable, GsonDataComponentValue>) FusionCoreAdventureProviders::tagToGson,
                    (BiFunction<Key, GsonDataComponentValue, DataComponentValue.TagSerializable>) FusionCoreAdventureProviders::gsonToTag);
        } catch (ReflectiveOperationException | LinkageError e) {
            LOGGER.warn("Adventure providers are not available, plain text and legacy serializers keep Adventure's defaults", e);
        }
    }

    // click callbacks: Paper's ClickCallbackProviderImpl.CallbackManager and CallbackCommand (patch 0017)

    public static final CallbackManager CALLBACK_MANAGER = new CallbackManager();

    public static void registerCommands(CraftServer server) {
        server.getCommandMap().register("callback", "Paper", new CallbackCommand());
    }

    public static final class CallbackManager {
        private final Map<UUID, StoredCallback> callbacks = new HashMap<>();
        private final Queue<StoredCallback> queue = new ConcurrentLinkedQueue<>();

        private CallbackManager() {
        }

        private ClickEvent create(ClickCallback<Audience> callback, ClickCallback.Options options) {
            return ClickEvent.runCommand("/paper:callback " + this.addCallback(callback, options));
        }

        public UUID addCallback(ClickCallback<Audience> callback, ClickCallback.Options options) {
            UUID id = UUID.randomUUID();
            this.queue.add(new StoredCallback(callback, options, id));
            return id;
        }

        /** Main thread, every tick. */
        public void handleQueue(int currentTick) {
            if (currentTick % 100 == 0) {
                this.callbacks.values().removeIf(callback -> !callback.valid());
            }
            StoredCallback callback;
            while ((callback = this.queue.poll()) != null) {
                this.callbacks.put(callback.id, callback);
            }
        }

        public void runCallback(Audience audience, UUID id) {
            StoredCallback callback = this.callbacks.get(id);
            if (callback != null && callback.valid()) {
                callback.takeUse();
                callback.callback.accept(audience);
            }
        }
    }

    private static final class StoredCallback {
        private final long startedAt = System.nanoTime();
        private final ClickCallback<Audience> callback;
        private final long lifetime;
        private final UUID id;
        private int remainingUses;

        private StoredCallback(ClickCallback<Audience> callback, ClickCallback.Options options, UUID id) {
            this.callback = callback;
            this.lifetime = options.lifetime().toNanos();
            this.remainingUses = options.uses();
            this.id = id;
        }

        private void takeUse() {
            if (this.remainingUses != ClickCallback.UNLIMITED_USES) {
                this.remainingUses--;
            }
        }

        private boolean valid() {
            boolean hasRemainingUses = this.remainingUses == ClickCallback.UNLIMITED_USES || this.remainingUses > 0;
            return hasRemainingUses && System.nanoTime() - this.startedAt < this.lifetime;
        }
    }

    private static final class CallbackCommand extends Command {
        private CallbackCommand() {
            super("callback");
            this.description = "ClickEvent callback";
            this.usageMessage = "/callback <uuid>";
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (args.length != 1) {
                return false;
            }
            UUID id;
            try {
                id = UUID.fromString(args[0]);
            } catch (IllegalArgumentException ignored) {
                return false;
            }
            // CommandSender is an Audience through CommandSenderMixin, which javac does not see
            CALLBACK_MANAGER.runCallback((Audience) (Object) sender, id);
            return true;
        }
    }

    // data component values: Paper's DataComponentValueConverterProviderImpl converts its own value type to and from
    // JSON and SNBT; Fusion Core's item hovers are JSON already, so SNBT values from plugins convert to JSON and back

    private static final Codec<Tag, String, CommandSyntaxException, RuntimeException> NBT_CODEC =
        Codec.codec(string -> new TagParser(new StringReader(string)).readValue(), Tag::toString);

    private static <T> RegistryOps<T> ops(DynamicOps<T> delegate) {
        return ArclightServer.getMinecraftServer().registryAccess().createSerializationContext(delegate);
    }

    private static DataComponentType<?> type(Key key) {
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath(key.namespace(), key.value()));
        if (type == null) {
            throw new IllegalArgumentException("Unknown data component type: " + key);
        }
        return type;
    }

    private static GsonDataComponentValue tagToGson(Key key, DataComponentValue.TagSerializable value) {
        try {
            return GsonDataComponentValue.gsonDataComponentValue(reencode(type(key), NbtOps.INSTANCE, value.asBinaryTag().get(NBT_CODEC), JsonOps.INSTANCE));
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static DataComponentValue.TagSerializable gsonToTag(Key key, GsonDataComponentValue value) {
        Tag tag = reencode(type(key), JsonOps.INSTANCE, value.element(), NbtOps.INSTANCE);
        return () -> BinaryTagHolder.encode(tag, NBT_CODEC);
    }

    private static <T, A, B> B reencode(DataComponentType<T> type, DynamicOps<A> from, A input, DynamicOps<B> to) {
        T value = type.codecOrThrow().parse(ops(from), input).getOrThrow(IllegalArgumentException::new);
        return type.codecOrThrow().encodeStart(ops(to), value).getOrThrow(IllegalArgumentException::new);
    }

    /** Paper's {@code NBTLegacyHoverEventSerializer}: the pre-1.16 {@code value} of item and entity hovers as SNBT. */
    private static final class NBTLegacyHoverEventSerializer implements LegacyHoverEventSerializer {
        private static final NBTLegacyHoverEventSerializer INSTANCE = new NBTLegacyHoverEventSerializer();
        private static final Codec<CompoundTag, String, CommandSyntaxException, RuntimeException> SNBT_CODEC = Codec.codec(TagParser::parseTag, Tag::toString);
        private static final String ITEM_TYPE = "id";
        private static final String ITEM_COUNT = "Count";
        private static final String ITEM_TAG = "tag";
        private static final String ENTITY_NAME = "name";
        private static final String ENTITY_TYPE = "type";
        private static final String ENTITY_ID = "id";

        @Override
        public HoverEvent.ShowItem deserializeShowItem(Component input) throws IOException {
            String raw = PlainTextComponentSerializer.plainText().serialize(input);
            try {
                CompoundTag contents = SNBT_CODEC.decode(raw);
                CompoundTag tag = contents.getCompound(ITEM_TAG);
                return HoverEvent.ShowItem.showItem(
                    Key.key(contents.getString(ITEM_TYPE)),
                    contents.contains(ITEM_COUNT) ? contents.getByte(ITEM_COUNT) : 1,
                    tag.isEmpty() ? null : BinaryTagHolder.encode(tag, SNBT_CODEC)
                );
            } catch (CommandSyntaxException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public HoverEvent.ShowEntity deserializeShowEntity(Component input, Codec.Decoder<Component, String, ? extends RuntimeException> componentCodec) throws IOException {
            String raw = PlainTextComponentSerializer.plainText().serialize(input);
            try {
                CompoundTag contents = SNBT_CODEC.decode(raw);
                return HoverEvent.ShowEntity.showEntity(
                    Key.key(contents.getString(ENTITY_TYPE)),
                    UUID.fromString(contents.getString(ENTITY_ID)),
                    componentCodec.decode(contents.getString(ENTITY_NAME))
                );
            } catch (CommandSyntaxException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public Component serializeShowItem(HoverEvent.ShowItem input) throws IOException {
            CompoundTag tag = new CompoundTag();
            tag.putString(ITEM_TYPE, input.item().asString());
            tag.putByte(ITEM_COUNT, (byte) input.count());
            if (input.nbt() != null) {
                try {
                    tag.put(ITEM_TAG, input.nbt().get(SNBT_CODEC));
                } catch (CommandSyntaxException ex) {
                    throw new IOException(ex);
                }
            }
            return Component.text(SNBT_CODEC.encode(tag));
        }

        @Override
        public Component serializeShowEntity(HoverEvent.ShowEntity input, Codec.Encoder<Component, String, ? extends RuntimeException> componentCodec) {
            CompoundTag tag = new CompoundTag();
            tag.putString(ENTITY_ID, input.id().toString());
            tag.putString(ENTITY_TYPE, input.type().asString());
            if (input.name() != null) {
                tag.putString(ENTITY_NAME, componentCodec.encode(input.name()));
            }
            return Component.text(SNBT_CODEC.encode(tag));
        }
    }
}
