package com.mcreater.amclcore.model.game.assets;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class GameAssetsIndexFileModel {
    private Map<String, GameAssetsIndexFileDescModel> objects;
}
