package com.mcreater.amclcore.model.oauth

import com.google.gson.annotations.SerializedName
import com.mcreater.amclcore.annotations.RequestModel
import java.util.*

@RequestModel
data class MinecraftRequestModel(
    var username: UUID? = null,
    var roles: List<Any>? = null,
    var metadata: Map<String, Any>? = null,
    @SerializedName("access_token") var accessToken: String? = null,
    @SerializedName("expires_in") var expiresIn: Int = 0,
    @SerializedName("token_type") var tokenType: String? = null,
)
