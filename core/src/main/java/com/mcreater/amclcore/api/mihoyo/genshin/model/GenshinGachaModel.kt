package com.mcreater.amclcore.api.mihoyo.genshin.model

import com.mcreater.amclcore.annotations.RequestModel

@RequestModel
data class GenshinGachaModel(
    var retcode: Int = 0,
    var message: String? = null,
    var data: GenshinGachaDataModel? = null
)
