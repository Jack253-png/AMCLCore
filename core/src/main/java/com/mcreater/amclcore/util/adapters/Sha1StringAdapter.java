package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.util.hash.Sha1String;

import java.io.IOException;
import java.util.Optional;

public class Sha1StringAdapter extends TypeAdapter<Sha1String> {
    public static final Sha1StringAdapter INSTANCE = new Sha1StringAdapter();

    private Sha1StringAdapter() {
    }

    public void write(JsonWriter out, Sha1String value) throws IOException {
        out.value(
                Optional.ofNullable(value)
                        .map(Sha1String::getRaw)
                        .orElse(null)
        );
    }

    public Sha1String read(JsonReader in) throws IOException {
        return Sha1String.create(in.nextString());
    }
}
