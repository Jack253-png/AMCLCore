package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel;

import java.io.IOException;

public class VariantAdapter extends TypeAdapter<MinecraftProfileRequestModel.Variant> {
    public static final VariantAdapter INSTANCE = new VariantAdapter();

    private VariantAdapter() {
    }

    public void write(JsonWriter out, MinecraftProfileRequestModel.Variant value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(
                value.toString()
        );
    }

    public MinecraftProfileRequestModel.Variant read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return MinecraftProfileRequestModel.Variant.CLASSIC;
        }
        return MinecraftProfileRequestModel.Variant.parse(in.nextString().toUpperCase());
    }
}
