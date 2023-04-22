package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel;

import java.io.IOException;

public class StateAdapter extends TypeAdapter<MinecraftProfileRequestModel.State> {
    public static final StateAdapter INSTANCE = new StateAdapter();

    private StateAdapter() {
    }

    public void write(JsonWriter out, MinecraftProfileRequestModel.State value) throws IOException {
        out.value(value.toString().toLowerCase());
    }

    public MinecraftProfileRequestModel.State read(JsonReader in) throws IOException {
        return MinecraftProfileRequestModel.State.parse(in.nextString().toUpperCase());
    }
}
