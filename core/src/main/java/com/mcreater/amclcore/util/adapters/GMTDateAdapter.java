package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.util.date.GMTDate;

import java.io.IOException;

public class GMTDateAdapter extends TypeAdapter<GMTDate> {
    public static final GMTDateAdapter INSTANCE = new GMTDateAdapter();

    private GMTDateAdapter() {
    }

    public void write(JsonWriter out, GMTDate value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(
                value.getRaw()
        );
    }

    public GMTDate read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return new GMTDate(in.nextString());
    }
}
