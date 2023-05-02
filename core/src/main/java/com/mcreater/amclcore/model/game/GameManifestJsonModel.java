package com.mcreater.amclcore.model.game;

import com.mcreater.amclcore.model.game.arguments.GameArgumentsModel;
import com.mcreater.amclcore.model.game.logging.GameLoggingModel;
import com.mcreater.amclcore.util.date.GMTDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameManifestJsonModel {
    private GameArgumentsModel arguments;
    private String minecraftArguments;

    private String clientVersion;
    private String type;
    private GMTDate time;
    private GMTDate releaseTime;
    private int minimumLauncherVersion;

    private String mainClass;
    private GameLoggingModel logging;
}
