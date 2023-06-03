package com.mcreater.amclcore.model.game.lib;

import com.mcreater.amclcore.util.hash.Sha1String;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameCoreLibDownloadFileModel {
    private Sha1String sha1;
    private long size;
    private MinecraftMirroredResourceURL url;
}
