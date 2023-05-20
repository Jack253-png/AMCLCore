package com.mcreater.amclcore.model.game.lib;

import com.mcreater.amclcore.util.hash.Sha1String;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

@Builder
@Data
public class GameDependedLibDownloadArtifactModel {
    private String path;
    private Sha1String sha1;
    private long size;
    private MinecraftMirroredResourceURL url;

    public Path getJarPath() {
        return Paths.get(path);
    }
}
