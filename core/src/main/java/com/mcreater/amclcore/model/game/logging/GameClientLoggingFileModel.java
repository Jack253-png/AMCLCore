package com.mcreater.amclcore.model.game.logging;

import com.mcreater.amclcore.util.hash.Sha1String;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameClientLoggingFileModel {
    private String id;
    private Sha1String sha1;
    private int size;
    private MinecraftMirroredResourceURL url;
}
