package com.mcreater.amclcore.model.game.logging;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GameClientLoggingModel {
    private String argument;
    private GameClientLoggingFileModel file;
    private String type;
}
