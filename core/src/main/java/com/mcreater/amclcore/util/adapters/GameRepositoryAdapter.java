package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.game.GameRepository;
import com.mcreater.amclcore.util.JsonUtil;

import java.io.IOException;

public class GameRepositoryAdapter extends TypeAdapter<GameRepository> {
    public static final GameRepositoryAdapter INSTANCE = new GameRepositoryAdapter();

    private GameRepositoryAdapter() {
    }

    public void write(JsonWriter out, GameRepository value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject()
                .name("path").value(
                        value.getPath().toString()
                )
                .name("name").value(
                        value.getName()
                )
                .endObject();
    }

    public GameRepository read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        JsonUtil.JsonToMapProcessor processor = new JsonUtil.JsonToMapProcessor(in);
        while (processor.processable()) processor.process();
        JsonUtil.MappedJson mappedJson = processor.getProcessedContent();

        String path = mappedJson.tryGetString("path");
        if (path == null) return null;
        String name = mappedJson.tryGetString("name");

        return GameRepository.of(path, name).orElse(null);
    }
}
