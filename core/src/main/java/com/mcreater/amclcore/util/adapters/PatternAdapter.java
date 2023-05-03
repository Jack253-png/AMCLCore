package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.regex.Pattern;

public class PatternAdapter extends TypeAdapter<Pattern> {
    public static final PatternAdapter INSTANCE = new PatternAdapter();

    private PatternAdapter() {
    }

    public void write(JsonWriter out, Pattern value) throws IOException {
        if (value == null) return;
        out.value(
                value.pattern()
        );
    }

    public Pattern read(JsonReader in) throws IOException {
        return Pattern.compile(in.nextString());
    }
}
