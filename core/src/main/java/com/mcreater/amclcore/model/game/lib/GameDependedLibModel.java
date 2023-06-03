package com.mcreater.amclcore.model.game.lib;

import com.mcreater.amclcore.model.game.rule.GameRuleFeatureModel;
import com.mcreater.amclcore.model.game.rule.GameRuleModel;
import com.mcreater.amclcore.util.maven.MavenLibName;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Builder
@Data
public class GameDependedLibModel {
    private GameDependedLibDownloadModel downloads;
    private GameDependedLibExtractModel extract;
    private MavenLibName name;
    private Map<String, String> natives;
    private List<GameRuleModel> rules;
    private MinecraftMirroredResourceURL url;
    private Boolean clientreq;
    private Boolean serverreq;

    public boolean valid(GameRuleFeatureModel data) {
        if (rules == null) return true;
        else {
            AtomicBoolean isValid = new AtomicBoolean(false);
            rules.forEach(gameRuleModel -> {
                if (gameRuleModel.valid(data)) isValid.set(gameRuleModel.getAction() == GameRuleModel.Action.ALLOW);
            });
            return isValid.get();
        }
    }

    public boolean valid() {
        return valid(null);
    }

    public boolean isNormalLib() {
        return this.getName().getPlatform() == null;
    }

    public boolean hasNatives() {
        return (this.natives != null && this.downloads != null && this.downloads.getClassifiers() != null) || !isNormalLib();
    }

    public Path getJarPath() {
        if (getDownloads() != null && getDownloads().getArtifact() != null)
            return Paths.get(getDownloads().getArtifact().getPath());
        else return getName().toPath();
    }
}
