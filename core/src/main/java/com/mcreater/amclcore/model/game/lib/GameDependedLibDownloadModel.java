package com.mcreater.amclcore.model.game.lib;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class GameDependedLibDownloadModel {
    private GameDependedLibDownloadArtifactModel artifact;
    private Map<String, GameDependedLibDownloadArtifactModel> classifiers;
}
