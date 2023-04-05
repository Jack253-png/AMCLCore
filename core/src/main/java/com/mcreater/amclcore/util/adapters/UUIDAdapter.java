package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

public class UUIDAdapter extends TypeAdapter<UUID> {
    public static UUIDAdapter INSTANCE = new UUIDAdapter();

    private UUIDAdapter() {
    }

    public void write(JsonWriter out, UUID value) throws IOException {
        out.value(value.toString());
    }

    public UUID read(JsonReader in) throws IOException {
        return UUID.fromString(in.nextString());
    }
}
