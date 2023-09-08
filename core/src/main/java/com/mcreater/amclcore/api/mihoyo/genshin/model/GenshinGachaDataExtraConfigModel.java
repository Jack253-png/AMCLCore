package com.mcreater.amclcore.api.mihoyo.genshin.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GenshinGachaDataExtraConfigModel {
    @SerializedName("reliquary_fst_attr")
    private GenshinGachaDataExtraConfigItemModel reliquaryFstAttr;
    @SerializedName("reliquary_sec_attr")
    private List<GenshinGachaDataLevelModel> reliquarySecAttr;
    @SerializedName("reliquary_type_define")
    private List<GenshinGachaDataLevelModel> reliquaryTypeDefine;
}
