package com.mcreater.amclcore.api.mihoyo.genshin.model

data class GenshinGachaDataExtraConfigItemModel(
    var key: Int = 0,
    var value: List<GenshinGachaDataLevelModel>? = null
)
