package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.util.date.GMTDate;

import java.io.IOException;
import java.util.Optional;

public class GMTDateAdapter extends TypeAdapter<GMTDate> {
    public static final GMTDateAdapter INSTANCE = new GMTDateAdapter();

    private GMTDateAdapter() {
    }

    public void write(JsonWriter out, GMTDate value) throws IOException {
        out.value(
                Optional.ofNullable(value)
                        .map(GMTDate::getRaw)
                        .orElse(null)
        );
    }

    public GMTDate read(JsonReader in) throws IOException {
        return new GMTDate(in.nextString());
    }
}
