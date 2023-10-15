package com.mcreater.amclcore.model.oauth

import com.google.gson.annotations.SerializedName
import com.mcreater.amclcore.annotations.OAuthLoginModel
import com.mcreater.amclcore.annotations.RequestModel

@RequestModel
@OAuthLoginModel
data class TokenResponseModel(
    @SerializedName("token_type") var tokenType: String? = null,
    @SerializedName("expires_in") var expiresIn: Int = 0,
    @SerializedName("ext_expires_in") var extExpiresIn: Int = 0,
    var scope: String? = null,
    @SerializedName("access_token") var accessToken: String? = null,
    @SerializedName("refresh_token") var refreshToken: String? = null,
    var error: String? = null,
    @SerializedName("error_description") var errorDescription: String? = null,
    @SerializedName("correlation_id") var correlationId: String? = null
)
