package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;

import java.io.IOException;

public class MinecraftMirroredResourceURLAdapter extends TypeAdapter<MinecraftMirroredResourceURL> {
    public static final MinecraftMirroredResourceURLAdapter INSTANCE = new MinecraftMirroredResourceURLAdapter();

    private MinecraftMirroredResourceURLAdapter() {
    }

    public void write(JsonWriter out, MinecraftMirroredResourceURL value) throws IOException {
        if (value == null) return;
        out.value(
                value.getRawUrl().toString()
        );
    }

    public MinecraftMirroredResourceURL read(JsonReader in) throws IOException {
        try {
            return MinecraftMirroredResourceURL.create(in.nextString());
        } catch (Exception e) {
            return null;
        }
    }
}
