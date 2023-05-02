package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class MinecraftMirroredResourceURLAdapter extends TypeAdapter<MinecraftMirroredResourceURL> {
    public static final MinecraftMirroredResourceURLAdapter INSTANCE = new MinecraftMirroredResourceURLAdapter();

    private MinecraftMirroredResourceURLAdapter() {
    }

    public void write(JsonWriter out, MinecraftMirroredResourceURL value) throws IOException {
        out.value(
                Optional.ofNullable(value)
                        .map(MinecraftMirroredResourceURL::getRawUrl)
                        .map(URL::toString)
                        .orElse(null)
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
