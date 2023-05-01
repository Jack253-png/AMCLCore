package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.model.game.rule.GameRuleModel;

import java.io.IOException;
import java.util.Optional;

public class GameRuleStateAdapter extends TypeAdapter<GameRuleModel.Action> {
    public static final GameRuleStateAdapter INSTANCE = new GameRuleStateAdapter();

    private GameRuleStateAdapter() {
    }

    public void write(JsonWriter out, GameRuleModel.Action value) throws IOException {
        out.value(
                Optional.ofNullable(value)
                        .map(GameRuleModel.Action::toString)
                        .map(String::toLowerCase)
                        .orElse(null)
        );
    }

    public GameRuleModel.Action read(JsonReader in) throws IOException {
        return GameRuleModel.Action.parse(in.nextString());
    }
}
