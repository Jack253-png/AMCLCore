package com.mcreater.amclcore.model.game.lib;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameCoreLibDownloadModel {
    private GameCoreLibDownloadFileModel client;
    @SerializedName("client_mappings")
    private GameCoreLibDownloadFileModel clientMappings;
    private GameCoreLibDownloadFileModel server;
    @SerializedName("server_mappings")
    private GameCoreLibDownloadFileModel serverMappings;
}
