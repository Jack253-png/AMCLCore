package com.mcreater.amclcore.game;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GameRepository {
    @Getter
    private final File path;

    public static GameRepository of(String path) {
        return new GameRepository(new File(path));
    }

    public static GameRepository of(File path) {
        return new GameRepository(path);
    }
}
