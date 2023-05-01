package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.mcreater.amclcore.util.StringUtil.*;

public class UUIDAdapter extends TypeAdapter<UUID> {
    public static UUIDAdapter INSTANCE = new UUIDAdapter();

    private UUIDAdapter() {
    }

    public void write(JsonWriter out, UUID value) throws IOException {
        out.value(
                Optional.ofNullable(value)
                        .map(UUID::toString)
                        .orElse(null)
        );
    }

    public UUID read(JsonReader in) throws IOException {
        String uuid = in.nextString();
        if (checkUUID(uuid)) {
            if (isLineUUID(uuid)) return UUID.fromString(uuid);
            else return toLineUUID(uuid);
        } else return null;
    }
}
