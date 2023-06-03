package com.mcreater.amclcore.model.game.lib;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameCoreLibDownloadModel {
    private GameCoreLibDownloadFileModel client;
    private GameCoreLibDownloadFileModel client_mappings;
    private GameCoreLibDownloadFileModel server;
    private GameCoreLibDownloadFileModel server_mappings;
}
