package com.mcreater.amclcore.model.game.lib;

import com.mcreater.amclcore.model.game.rule.GameRuleModel;
import com.mcreater.amclcore.util.maven.MavenLibName;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class GameDependedLibModel {
    private GameDependedLibDownloadModel downloads;
    private Map<String, GameDependedLibDownloadArtifactModel> classifiers;
    private GameDependedLibExtractModel extract;
    private MavenLibName name;
    private Map<String, String> natives;
    private List<GameRuleModel> rules;
    private MinecraftMirroredResourceURL url;
    private Boolean clientreq;
    private Boolean serverreq;
}
