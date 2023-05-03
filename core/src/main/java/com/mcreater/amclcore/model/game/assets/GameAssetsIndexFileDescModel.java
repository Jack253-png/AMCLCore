package com.mcreater.amclcore.model.game.assets;

import com.mcreater.amclcore.util.hash.Sha1String;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GameAssetsIndexFileDescModel {
    private Sha1String hash;
    private long size;
}
