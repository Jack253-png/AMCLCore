package com.mcreater.amclcore.util.adapters;

import com.google.gson.*;
import com.mcreater.amclcore.model.game.arguments.GameArgumentsModel;

import java.lang.reflect.Type;

public class GameArgumentsItemJsonSerializer implements JsonSerializer<GameArgumentsModel.GameArgumentsItem> {
    public static final GameArgumentsItemJsonSerializer INSTANCE = new GameArgumentsItemJsonSerializer();

    private GameArgumentsItemJsonSerializer() {
    }

    public JsonElement serialize(GameArgumentsModel.GameArgumentsItem src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.getRules() == null) {
            if (src.getValue().size() == 1) return new JsonPrimitive(src.getValue().get(0));
            else {
                JsonArray array = new JsonArray();
                src.getValue().forEach(array::add);
                return array;
            }
        } else {
            JsonObject object = new JsonObject();
            object.add("rules", context.serialize(src.getRules()));
            if (src.getValue().size() == 1) object.add("value", new JsonPrimitive(src.getValue().get(0)));
            else {
                JsonArray array = new JsonArray();
                src.getValue().forEach(array::add);
                object.add("value", array);
            }
            return object;
        }
    }
}
