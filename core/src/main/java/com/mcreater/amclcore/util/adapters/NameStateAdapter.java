package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangeableRequestModel;

import java.io.IOException;

public class NameStateAdapter extends TypeAdapter<MinecraftNameChangeableRequestModel.State> {
    public static final NameStateAdapter INSTANCE = new NameStateAdapter();

    private NameStateAdapter() {
    }

    public void write(JsonWriter out, MinecraftNameChangeableRequestModel.State value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(
                value.toString()
        );
    }

    public MinecraftNameChangeableRequestModel.State read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return MinecraftNameChangeableRequestModel.State.NOT_ALLOWED;
        }
        return MinecraftNameChangeableRequestModel.State.parse(in.nextString().toUpperCase());
    }
}
