package com.mcreater.amclcore.api.mihoyo.genshin.model

import com.google.gson.annotations.SerializedName

data class GenshinGachaDataExtraConfigModel(
    @SerializedName("reliquary_fst_attr") var reliquaryFstAttr: GenshinGachaDataExtraConfigItemModel? = null,
    @SerializedName("reliquary_sec_attr") var reliquarySecAttr: List<GenshinGachaDataLevelModel>? = null,
    @SerializedName("reliquary_type_define") var reliquaryTypeDefine: List<GenshinGachaDataLevelModel>? = null
)