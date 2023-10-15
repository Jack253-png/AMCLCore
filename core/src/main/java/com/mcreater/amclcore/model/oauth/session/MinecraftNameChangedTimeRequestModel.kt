package com.mcreater.amclcore.model.oauth.session

import com.mcreater.amclcore.annotations.RequestModel
import com.mcreater.amclcore.util.date.StandardDate

@RequestModel
data class MinecraftNameChangedTimeRequestModel(
    var changedAt: StandardDate? = null,
    var createdAt: StandardDate? = null,
    var nameChangeAllowed: Boolean = false
)
