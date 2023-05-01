package com.mcreater.amclcore.game;

import com.mcreater.amclcore.concurrent.task.AbstractAction;
import com.mcreater.amclcore.i18n.Text;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.mcreater.amclcore.i18n.I18NManager.translatable;
import static java.util.Objects.requireNonNull;

public class GameRepository {
    @Getter
    private final Path path;
    @Getter
    @Setter
    private String name;

    private GameRepository(Path path, String name) {
        this.path = path;
        this.name = name;
    }

    private List<GameInstance> instances = new Vector<>();

    public final void init() throws IOException {
        Files.createDirectories(path);
        Files.createDirectories(getAssetsDirectory());
        Files.createDirectories(getLibrariesDirectory());
        Files.createDirectories(getVersionsDirectory());
    }

    public final AbstractAction updateAsync() {
        return new AbstractAction() {
            protected void execute() {
                updateInstances();
            }

            protected Text getTaskName() {
                return translatable("core.game.repository.task.update.text");
            }
        };
    }

    public List<GameInstance> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    final Path getAssetsDirectory() {
        return path.resolve("assets");
    }

    final Path getLibrariesDirectory() {
        return path.resolve("libraries");
    }

    final Path getVersionsDirectory() {
        return path.resolve("versions");
    }

    private void updateInstances() {
        instances.clear();
        Arrays.stream(requireNonNull(getVersionsDirectory().toFile().listFiles(File::isDirectory)))
                .map(file ->
                        GameManifestJson.find(
                                GameRepository.this,
                                file.toPath(),
                                file.getName()
                        )
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(GameManifestJson::toInstance)
                .forEach(instances::add);
    }

    public static Optional<GameRepository> of(String path) {
        return of(new File(path));
    }

    public static Optional<GameRepository> of(File path) {
        return of(path.toPath());
    }

    public static Optional<GameRepository> of(Path path) {
        return of(path, path.toString());
    }

    public static Optional<GameRepository> of(String path, String name) {
        return of(new File(path), name);
    }

    public static Optional<GameRepository> of(File path, String name) {
        return of(path.toPath(), name);
    }

    public static Optional<GameRepository> of(Path path, String name) {
        return Optional.ofNullable(path.toFile().isDirectory() ? new GameRepository(path, name) : null);
    }
}
