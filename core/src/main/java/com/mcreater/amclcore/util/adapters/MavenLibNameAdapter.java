package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.util.maven.MavenLibName;

import java.io.IOException;

public class MavenLibNameAdapter extends TypeAdapter<MavenLibName> {
    public static final MavenLibNameAdapter INSTANCE = new MavenLibNameAdapter();

    private MavenLibNameAdapter() {
    }

    public void write(JsonWriter out, MavenLibName value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value.getName());
    }

    public MavenLibName read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String s = in.nextString();
        if (s.contains(":")) return MavenLibName.of(s);
        else return MavenLibName.of(null, s, null, null);
    }
}
