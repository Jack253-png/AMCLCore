package com.mcreater.amclcore.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mcreater.amclcore.game.GameRepository;
import com.mcreater.amclcore.util.JsonUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class GameRepositoryAdapter extends TypeAdapter<GameRepository> {
    public static final GameRepositoryAdapter INSTANCE = new GameRepositoryAdapter();

    private GameRepositoryAdapter() {
    }

    public void write(JsonWriter out, GameRepository value) throws IOException {
        out.beginObject()
                .name("path").value(
                        Optional.ofNullable(value)
                                .map(GameRepository::getPath)
                                .map(Path::toString).orElse(null)
                )
                .name("name").value(
                        Optional.ofNullable(value)
                                .map(GameRepository::getName)
                                .orElse("")
                )
                .endObject();
    }

    public GameRepository read(JsonReader in) throws IOException {
        JsonUtil.JsonToMapProcessor processor = new JsonUtil.JsonToMapProcessor(in);
        while (processor.processable()) processor.process();
        JsonUtil.MappedJson mappedJson = processor.getProcessedContent();

        String path = mappedJson.tryGetString("path");
        if (path == null) return null;
        String name = mappedJson.tryGetString("name");

        return GameRepository.of(path, name).orElse(null);
    }
}
