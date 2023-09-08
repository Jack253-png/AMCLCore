package com.mcreater.amclcore.api.mihoyo.genshin.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GenshinGachaDataAvatarModel {
    private int id;
    private String name;
    private String icon;
    private int element;
    private int level;
    @SerializedName("weapon_cat_id")
    private int weaponCatId;
    @SerializedName("set_list")
    private List<Object> setList;
    @SerializedName("wiki_url")
    private String wikiUrl;
    @SerializedName("first_attr")
    private List<Object> firstAttr;
    @SerializedName("secondary_attr")
    private List<Object> secondaryAttr;
    @SerializedName("secondary_attr_name")
    private List<Object> secondaryAttrName;
    @SerializedName("head_icon")
    private String headIcon;
    @SerializedName("pc_icon")
    private String pcIcon;
}
