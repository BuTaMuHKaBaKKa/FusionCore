package io.izzel.arclight.boot.adventure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Fusion Core: the server's {@code GsonComponentSerializer} also reads the text format of Minecraft 1.21.5 and later.
 *
 * <p>Plugins that shade a newer Adventure (LuckPerms 5.5 ships 4.21) hand their text to the server's Adventure as JSON,
 * written for the newest game version by default: {@code click_event} with {@code url}/{@code command}/{@code page}/
 * {@code path} and {@code hover_event} with its fields inline. The Adventure of 1.21.1 (4.17) knows only {@code clickEvent}
 * and {@code hoverEvent}, so it dropped click and hover and kept the rest (a link stayed underlined but did nothing).
 * Before reading, the newer keys are rewritten to the 1.21.1 form; JSON already in that form is read unchanged.</p>
 */
final class ModernJsonGsonSerializer implements GsonComponentSerializer {

    private final GsonComponentSerializer delegate;

    ModernJsonGsonSerializer(GsonComponentSerializer delegate) {
        this.delegate = delegate;
    }

    @Override
    public Gson serializer() {
        return this.delegate.serializer();
    }

    @Override
    public UnaryOperator<GsonBuilder> populator() {
        return this.delegate.populator();
    }

    @Override
    public Component deserializeFromTree(JsonElement input) {
        return this.delegate.deserializeFromTree(toLegacy(input));
    }

    @Override
    public JsonElement serializeToTree(Component component) {
        return this.delegate.serializeToTree(component);
    }

    @Override
    public Component deserialize(String input) {
        JsonElement tree;
        try {
            tree = JsonParser.parseString(input);
        } catch (RuntimeException notJson) {
            return this.delegate.deserialize(input); // the same error as before
        }
        return this.delegate.deserializeFromTree(toLegacy(tree));
    }

    @Override
    public String serialize(Component component) {
        return this.delegate.serialize(component);
    }

    @Override
    public Builder toBuilder() {
        return this.delegate.toBuilder();
    }

    /** A copy of the text with 1.21.5 click and hover events in the 1.21.1 form; the input when nothing needs it. */
    static JsonElement toLegacy(JsonElement element) {
        if (element == null || !needsRewrite(element)) {
            return element;
        }
        return rewrite(element.deepCopy());
    }

    private static boolean needsRewrite(JsonElement element) {
        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                if (needsRewrite(child)) return true;
            }
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("click_event") || object.has("hover_event")) return true;
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                if (needsRewrite(entry.getValue())) return true;
            }
        }
        return false;
    }

    private static JsonElement rewrite(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                array.set(i, rewrite(array.get(i)));
            }
            return array;
        }
        if (!element.isJsonObject()) {
            return element;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement click = object.remove("click_event");
        if (click != null && click.isJsonObject() && !object.has("clickEvent")) {
            JsonObject legacy = clickEvent(click.getAsJsonObject());
            if (legacy != null) object.add("clickEvent", legacy);
        }
        JsonElement hover = object.remove("hover_event");
        if (hover != null && hover.isJsonObject() && !object.has("hoverEvent")) {
            JsonObject legacy = hoverEvent(hover.getAsJsonObject());
            if (legacy != null) object.add("hoverEvent", legacy);
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            entry.setValue(rewrite(entry.getValue()));
        }
        return object;
    }

    // 1.21.5: open_url {url}, run_command / suggest_command {command}, change_page {page: int}, copy_to_clipboard {value},
    // open_file {path}; 1.21.1: {action, value: string}. Actions 1.21.1 does not have (show_dialog, custom) are dropped.
    private static JsonObject clickEvent(JsonObject modern) {
        String action = string(modern.get("action"));
        if (action == null) return null;
        String key = switch (action) {
            case "open_url" -> "url";
            case "run_command", "suggest_command" -> "command";
            case "change_page" -> "page";
            case "copy_to_clipboard" -> "value";
            case "open_file" -> "path";
            default -> null;
        };
        if (key == null) return null;
        JsonElement value = modern.has(key) ? modern.get(key) : modern.get("value");
        String text = string(value);
        if (text == null) return null;
        JsonObject legacy = new JsonObject();
        legacy.addProperty("action", action);
        legacy.addProperty("value", text);
        return legacy;
    }

    // 1.21.5: show_text {value}, show_item {id, count, components}, show_entity {id: type, uuid, name};
    // 1.21.1: {action, contents} with show_item {id, count, components} and show_entity {type, id: uuid, name}
    private static JsonObject hoverEvent(JsonObject modern) {
        String action = string(modern.get("action"));
        if (action == null) return null;
        JsonObject legacy = new JsonObject();
        legacy.addProperty("action", action);
        switch (action) {
            case "show_text" -> {
                JsonElement value = modern.has("value") ? modern.get("value") : modern.get("contents");
                if (value == null) return null;
                legacy.add("contents", rewrite(value));
            }
            case "show_item" -> {
                if (modern.has("contents")) {
                    legacy.add("contents", modern.get("contents"));
                } else {
                    JsonObject contents = new JsonObject();
                    copy(modern, "id", contents, "id");
                    copy(modern, "count", contents, "count");
                    copy(modern, "components", contents, "components");
                    legacy.add("contents", contents);
                }
            }
            case "show_entity" -> {
                if (modern.has("contents")) {
                    legacy.add("contents", rewrite(modern.get("contents")));
                } else {
                    JsonObject contents = new JsonObject();
                    copy(modern, "id", contents, "type");
                    copy(modern, "uuid", contents, "id");
                    if (modern.has("name")) contents.add("name", rewrite(modern.get("name")));
                    legacy.add("contents", contents);
                }
            }
            default -> {
                return null;
            }
        }
        return legacy;
    }

    private static void copy(JsonObject from, String key, JsonObject to, String as) {
        JsonElement value = from.get(key);
        if (value != null) to.add(as, value);
    }

    private static String string(JsonElement element) {
        if (element instanceof JsonPrimitive primitive) {
            return primitive.getAsString();
        }
        return null;
    }
}
