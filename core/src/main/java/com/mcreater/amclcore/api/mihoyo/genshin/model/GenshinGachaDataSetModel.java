package com.mcreater.amclcore.api.mihoyo.genshin.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GenshinGachaDataSetModel {
    private int id;
    private String name;
    private String icon;
    private int level;
    @SerializedName("attr_id")
    private int attrId;
    @SerializedName("cat_id")
    private int catId;
    @SerializedName("wiki_url")
    private String wikiUrl;
    @SerializedName("default_tag")
    private List<GenshinGachaDataLevelModel> defaultTag;
    @SerializedName("extra_config")
    private GenshinGachaDataExtraConfigModel extraConfig;
}
