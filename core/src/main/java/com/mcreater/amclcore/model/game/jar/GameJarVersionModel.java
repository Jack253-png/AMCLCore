package com.mcreater.amclcore.model.game.jar;

import com.google.gson.annotations.SerializedName;
import com.mcreater.amclcore.util.date.GMTDate;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class GameJarVersionModel {
    private String id;
    private String name;
    @SerializedName("release_target")
    private String releaseTarget;
    @SerializedName("world_version")
    private int worldVersion;
    @SerializedName("series_id")
    private String seriesId;
    @SerializedName("protocol_version")
    private int protocolVersion;
    @SerializedName("pack_version")
    private Map<String, Integer> packVersion;
    @SerializedName("build_time")
    private GMTDate buildTime;
    @SerializedName("java_component")
    private String javaComponent;
    @SerializedName("java_version")
    private String javaVersion;
    private boolean stable;
}
