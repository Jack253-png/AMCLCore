package com.mcreater.amclcore.api.mihoyo.genshin.model

import com.google.gson.annotations.SerializedName

data class GenshinGachaDataSetModel(
    var id: Int = 0,
    var name: String? = null,
    var icon: String? = null,
    var level: Int = 0,
    @SerializedName("attr_id") var attrId: Int = 0,
    @SerializedName("cat_id") var catId: Int = 0,
    @SerializedName("wiki_url") var wikiUrl: String? = null,
    @SerializedName("default_tag") var defaultTag: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("extra_config") var extraConfig: GenshinGachaDataExtraConfigModel? = null
)
