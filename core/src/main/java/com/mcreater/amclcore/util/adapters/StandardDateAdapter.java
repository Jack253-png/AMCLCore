package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.util.date.StandardDate;

import java.io.IOException;

public class StandardDateAdapter extends TypeAdapter<StandardDate> {
    public static final StandardDateAdapter INSTANCE = new StandardDateAdapter();

    private StandardDateAdapter() {
    }

    public void write(JsonWriter out, StandardDate value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(
                value.getRawDate()
        );
    }

    public StandardDate read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return new StandardDate(in.nextString());
    }
}
