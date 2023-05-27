package com.mcreater.amclcore.model.game;

import com.mcreater.amclcore.model.game.arguments.GameArgumentsModel;
import com.mcreater.amclcore.model.game.assets.GameAssetIndexModel;
import com.mcreater.amclcore.model.game.lib.GameCoreLibDownloadModel;
import com.mcreater.amclcore.model.game.lib.GameDependedLibModel;
import com.mcreater.amclcore.model.game.lib.GameJavaComponentModel;
import com.mcreater.amclcore.model.game.logging.GameLoggingModel;
import com.mcreater.amclcore.util.date.GMTDate;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameManifestJsonModel {
    private String name;
    private GameArgumentsModel arguments;
    private String minecraftArguments;

    private GameAssetIndexModel assetIndex;
    private String assets;
    private int complianceLevel;

    private GameCoreLibDownloadModel downloads;
    private String id;
    private GameJavaComponentModel javaVersion;
    private List<GameDependedLibModel> libraries;

    private GameLoggingModel logging;
    private String mainClass;
    private String clientVersion;
    private String type;
    private GMTDate time;
    private GMTDate releaseTime;
    private int minimumLauncherVersion;
}
