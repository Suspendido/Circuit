package xyz.kayaaa.xenon.shared.tools.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonNull()) return null;
        if (!jsonElement.isJsonPrimitive()) return null;

        JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
        if (!primitive.isString()) return null;

        String[] components = primitive.getAsString().split("-");
        if (components.length != 5) return null;

        return UUID.fromString(primitive.getAsString());
    }

    @Override
    public JsonElement serialize(UUID uuid, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(uuid.toString());
    }

}
