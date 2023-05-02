package com.mcreater.amclcore.model.game.assets;

import com.mcreater.amclcore.util.hash.Sha1String;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameAssetIndexModel {
    private String id;
    private Sha1String sha1;
    private long size;
    private long totalSize;
    private MinecraftMirroredResourceURL url;
}
