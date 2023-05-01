package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel;

import java.io.IOException;
import java.util.Optional;

public class StateAdapter extends TypeAdapter<MinecraftProfileRequestModel.State> {
    public static final StateAdapter INSTANCE = new StateAdapter();

    private StateAdapter() {
    }

    public void write(JsonWriter out, MinecraftProfileRequestModel.State value) throws IOException {
        out.value(
                Optional.ofNullable(value)
                        .map(Enum::toString)
                        .orElse(null)
        );
    }

    public MinecraftProfileRequestModel.State read(JsonReader in) throws IOException {
        return MinecraftProfileRequestModel.State.parse(in.nextString().toUpperCase());
    }
}
