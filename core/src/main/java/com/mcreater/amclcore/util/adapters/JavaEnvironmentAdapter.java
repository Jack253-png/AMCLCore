package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.java.JavaEnvironment;

import java.io.File;
import java.io.IOException;

public class JavaEnvironmentAdapter extends TypeAdapter<JavaEnvironment> {
    public static final JavaEnvironmentAdapter INSTANCE = new JavaEnvironmentAdapter();

    private JavaEnvironmentAdapter() {
    }

    public void write(JsonWriter out, JavaEnvironment value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(
                value.getExecutable().toString()
        );
    }

    public JavaEnvironment read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return JavaEnvironment.create(new File(in.nextString()));
    }
}
