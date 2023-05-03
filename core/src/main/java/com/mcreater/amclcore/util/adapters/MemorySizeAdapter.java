package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.java.MemorySize;

import java.io.IOException;

public class MemorySizeAdapter extends TypeAdapter<MemorySize> {
    public static final MemorySizeAdapter INSTANCE = new MemorySizeAdapter();

    private MemorySizeAdapter() {
    }

    public void write(JsonWriter out, MemorySize value) throws IOException {
        if (value == null) return;
        out.value(value.toString());
    }

    public MemorySize read(JsonReader in) throws IOException {
        return MemorySize.create(in.nextString());
    }
}
