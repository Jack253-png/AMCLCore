package com.mcreater.amclcore.game;

import com.mcreater.amclcore.model.game.GameManifestJsonModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static com.mcreater.amclcore.util.JsonUtil.GSON_PARSER;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GameManifestJson {
    private final GameRepository repository;
    private final Path versionPath;
    private final String name;

    public static Optional<GameManifestJson> find(GameRepository repository, Path versionPath, String name) {
        return Optional.ofNullable(
                versionPath.resolve(name + ".json").toFile().exists() ?
                        new GameManifestJson(repository, versionPath, name) : null
        );
    }

    public static GameManifestJson create(GameRepository repository, String name) {
        return new GameManifestJson(repository, repository.getPath().resolve(name), name);
    }

    public GameInstance toInstance() {
        return GameInstance.createInstance(this);
    }

    public Path getJsonPath() {
        return versionPath.resolve(name + ".json");
    }

    public GameManifestJsonModel readManifest() throws FileNotFoundException {
        return GSON_PARSER.fromJson(GSON_PARSER.toJson(readManifestRaw()), GameManifestJsonModel.class);
    }

    public Map<?, ?> readManifestRaw() throws FileNotFoundException {
        return GSON_PARSER.fromJson(new FileReader(getJsonPath().toFile()), Map.class);
    }
}
