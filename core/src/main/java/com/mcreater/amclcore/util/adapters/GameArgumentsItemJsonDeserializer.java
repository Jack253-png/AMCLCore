package com.mcreater.amclcore.util.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mcreater.amclcore.model.game.arguments.GameArgumentsModel;
import com.mcreater.amclcore.model.game.rule.GameRuleModel;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class GameArgumentsItemJsonDeserializer implements JsonDeserializer<GameArgumentsModel.GameArgumentsItem> {
    public static final GameArgumentsItemJsonDeserializer INSTANCE = new GameArgumentsItemJsonDeserializer();

    private GameArgumentsItemJsonDeserializer() {
    }

    public GameArgumentsModel.GameArgumentsItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) return GameArgumentsModel.GameArgumentsItem.builder()
                .value(Collections.singletonList(json.getAsString()))
                .build();

        JsonArray rules = json.getAsJsonObject().get("rules").getAsJsonArray();
        JsonElement value = json.getAsJsonObject().get("value");

        List<GameRuleModel> rulesMap = context.deserialize(
                rules,
                TypeToken.getParameterized(List.class, GameRuleModel.class).getType()
        );
        List<String> values = new Vector<>();
        if (value.isJsonArray()) {
            value.getAsJsonArray().forEach(jsonElement -> values.add(jsonElement.getAsString()));
        } else if (value.isJsonPrimitive()) {
            values.add(value.getAsString());
        }

        return GameArgumentsModel.GameArgumentsItem.builder()
                .rules(rulesMap)
                .value(values)
                .build();
    }
}
