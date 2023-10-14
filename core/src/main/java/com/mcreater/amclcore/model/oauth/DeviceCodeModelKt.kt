package com.mcreater.amclcore.model.oauth

import com.google.gson.annotations.SerializedName
import com.mcreater.amclcore.annotations.OAuthLoginModel
import com.mcreater.amclcore.annotations.RequestModel

@OAuthLoginModel
@RequestModel
data class DeviceCodeModelKt(
    @SerializedName("user_code") var userCode: String? = null,
    @SerializedName("device_code") var deviceCode: String? = null,
    @SerializedName("verification_uri") var verificationUri: String? = null,
    @SerializedName("expires_in") var expiresIn: Int = 0,
    var interval: Int = 0
)