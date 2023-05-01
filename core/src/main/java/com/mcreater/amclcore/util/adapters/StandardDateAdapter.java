package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.util.date.StandardDate;

import java.io.IOException;
import java.util.Optional;

public class StandardDateAdapter extends TypeAdapter<StandardDate> {
    public static final StandardDateAdapter INSTANCE = new StandardDateAdapter();

    private StandardDateAdapter() {
    }

    public void write(JsonWriter out, StandardDate value) throws IOException {
        out.value(
                Optional.ofNullable(value)
                        .map(StandardDate::getRawDate)
                        .orElse(null)
        );
    }

    public StandardDate read(JsonReader in) throws IOException {
        return new StandardDate(in.nextString());
    }
}
