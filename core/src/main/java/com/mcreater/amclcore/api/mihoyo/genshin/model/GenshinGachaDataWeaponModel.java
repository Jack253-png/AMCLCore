package com.mcreater.amclcore.api.mihoyo.genshin.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenshinGachaDataWeaponModel {
    private int id;
    private String name;
    private String icon;
    private int level;
    @SerializedName("cat_id")
    private int catId;
    @SerializedName("wiki_url")
    private String wikiUrl;
}
