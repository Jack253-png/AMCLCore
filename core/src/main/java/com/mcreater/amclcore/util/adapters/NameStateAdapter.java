package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangeableRequestModel;

import java.io.IOException;

public class NameStateAdapter extends TypeAdapter<MinecraftNameChangeableRequestModel.State> {
    public static final NameStateAdapter INSTANCE = new NameStateAdapter();

    private NameStateAdapter() {
    }

    public void write(JsonWriter out, MinecraftNameChangeableRequestModel.State value) throws IOException {
        if (value == null) return;
        out.value(
                value.toString()
        );
    }

    public MinecraftNameChangeableRequestModel.State read(JsonReader in) throws IOException {
        return MinecraftNameChangeableRequestModel.State.parse(in.nextString().toUpperCase());
    }
}
