package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

import static com.mcreater.amclcore.util.StringUtil.*;

public class UUIDAdapter extends TypeAdapter<UUID> {
    public static UUIDAdapter INSTANCE = new UUIDAdapter();

    private UUIDAdapter() {
    }

    public void write(JsonWriter out, UUID value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(
                value.toString()
        );
    }

    public UUID read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String uuid = in.nextString();
        if (checkUUID(uuid)) {
            if (isLineUUID(uuid)) return UUID.fromString(uuid);
            else return toLineUUID(uuid);
        } else return null;
    }
}
