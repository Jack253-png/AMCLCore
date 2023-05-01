package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel;

import java.io.IOException;
import java.util.Optional;

public class VariantAdapter extends TypeAdapter<MinecraftProfileRequestModel.Variant> {
    public static final VariantAdapter INSTANCE = new VariantAdapter();

    private VariantAdapter() {
    }

    public void write(JsonWriter out, MinecraftProfileRequestModel.Variant value) throws IOException {
        out.value(
                Optional.ofNullable(value)
                        .map(Enum::toString)
                        .orElse(null)
        );
    }

    public MinecraftProfileRequestModel.Variant read(JsonReader in) throws IOException {
        return MinecraftProfileRequestModel.Variant.parse(in.nextString().toUpperCase());
    }
}
