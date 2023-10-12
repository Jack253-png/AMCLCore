package com.mcreater.amclcore.api.mihoyo.genshin.model

import com.mcreater.amclcore.annotations.RequestModel

@RequestModel
data class GenshinGachaModel(
    val retcode: Int = 0,
    val message: String? = null,
    val data: GenshinGachaDataModel? = null
)
